package latentdirichlet

import io.circe.Json
import io.circe.parser.parse

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.clustering.{LDA, LocalLDAModel}
import org.apache.spark.ml.feature.{CountVectorizer => TF, IDF}
import org.apache.spark.ml.feature.{RegexTokenizer, StopWordsRemover}
import org.apache.spark.sql.{DataFrame, SparkSession}
import utils.config.{database, host}

class Trainer {
  val url = s"jdbc:postgresql://$host:5432/$database"
  val username = sys.env("CLOUDBERRY_USERNAME")
  val password = sys.env("CLOUDBERRY_PASSWORD")

  // This should be invoked by instantiating a temporary Spark cluster on AWS,
  // and submitting a Spark job; it should not run within the application.
  def train: Json = {
    val spark =
      SparkSession.builder
        .appName("cloudberry")
        .master("local[*]")
        .config("spark.driver.memory", "1G")
        .getOrCreate

    import spark.implicits._

    val header = "[\\r\\n]*[A-Z]+[-\\.\\w]+:.*[\\r\\n]+"

    val documents =
      spark.sparkContext
        .wholeTextFiles("s3a://datapunnet/cloudberry/newsgroups/*")
        .map{case (id, document) => (id, document.replaceAll(header, ""))}
        .toDF("id", "document")

    val tokenizer =
      new RegexTokenizer()
        .setInputCol("document")
        .setOutputCol("tokens")
        .setMinTokenLength(4)
        .setPattern("[^a-zA-Z]+")

    val remover =
      new StopWordsRemover().setInputCol("tokens").setOutputCol("terms")

    val pipeline = new Pipeline().setStages(Array(tokenizer, remover))
    val data = pipeline.fit(documents).transform(documents)

    val termFrequencies =
      new TF().setInputCol("terms")
        .setOutputCol("tf")
        .setVocabSize(10000)
        .setMinTF(1)
        .setMinDF(3)
        .fit(data)

    val tf = termFrequencies.transform(data)

    val idf =
      new IDF().setInputCol("tf").setOutputCol("idf").fit(tf).transform(tf)

    val lda = new LDA().setK(100).setMaxIter(50).setFeaturesCol("idf").fit(idf)

    // Also write topicsMatrix to database instead of rereading model from S3
    lda.write.overwrite.save("s3a://datapunnet/cloudberry/model")

    val vocabulary =
      termFrequencies.vocabulary
        .zipWithIndex
        .toSeq
        .toDF("term", "index")

    vocabulary.write.mode("overwrite")
      .json("s3a://datapunnet/cloudberry/vocabulary")

    populateVocabularyTable(vocabulary)

    // Convert this to a DataFrame("term", "topic", "value")
    val termTopicMatrix =
      LocalLDAModel.load("s3a://datapunnet/cloudberry/model").topicsMatrix

    // Do this in database
    val termTermMatrix =
      termTopicMatrix.multiply(termTopicMatrix.transpose.toDense)

    val rows = termTermMatrix.numRows
    val cols = termTermMatrix.numCols

    val termTable =
      Seq.tabulate(rows * cols)(n => (n % rows, n / cols))
        .map{ case (i, j) => (i, j, termTermMatrix.apply(i, j)) }
        .toDF

    populateTermTopicTable(termTable)

    spark.stop()

    running()
  }

  private def running(): Json = {
    val message: String =
      s"""
         |{
         |  \"response\": \"Running LDA...\"
         |}
       """.stripMargin

    parse(message).getOrElse(Json.Null)
  }

  private def populateTermTopicTable(termTable: DataFrame): Unit = {
    termTable.write
      .mode("overwrite")
      .format("jdbc")
      .option("url", url)
      .option("dbtable", "public.term_matrix")
      .option("user", username)
      .option("password", password)
      .save
  }

  private def populateVocabularyTable(vocabulary: DataFrame): Unit = {
    vocabulary.write
      .mode("overwrite")
      .format("jdbc")
      .option("url", url)
      .option("dbtable", "public.vocabulary")
      .option("user", username)
      .option("password", password)
      .save
  }

  private def createTermTermTable(): Unit = {
    """
      |create table term_term_matrix (
      |  i int,
      |  j int,
      |  value numeric
    """.stripMargin

    () // Run query
  }

  private def populateTermTermTable(): Unit = {
    """
      |insert into term_term_matrix
      |select
      |  m.term,
      |  m_t.term,
      |  sum(m.value * m_t.value)
      |  from term_topic_matrix m
      |  inner join term_topic_matrix m_t
      |    on m.topic = m_t.topic
      |   where m.term >= 0 and m.term < 100
      |   group by m.term, m_t.term
    """.stripMargin

    () // Run query
  }

  private def indexTermTermTable(): Unit = {
    val indexI = "create index term_i_idx on term_term_matrix (i)"
    val indexJ = "create index term_j_idx on term_term_matrix (j)"

    () // Run query
  }
}
