package latentdirichlet

import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import java.util.Properties
import scala.collection.JavaConverters._
import utils.json.read


object TextProcessor {
  private val StopWords: Array[String] =
    read("src/main/resources/stopwords.json").hcursor
      .get[Array[String]]("stopwords").getOrElse(Array(""))

  private val NlpPipeline: StanfordCoreNLP = {
    val properties = new Properties
    properties.put("annotators", "tokenize, ssplit")
    new StanfordCoreNLP(properties)
  }

  def tokenize(body: String): Array[String] = {
    val pipeline = NlpPipeline
    val text = new Annotation(body)

    pipeline.annotate(text)

    text.get(classOf[SentencesAnnotation])
      .asScala.flatMap(tokenizeSentence)
      .filter(notStopword).filter(notPunctuation).toArray
  }

  private def tokenizeSentence(sentence: CoreMap): Array[String] = {
    sentence.get(classOf[TokensAnnotation])
      .asScala.map(_.word.toLowerCase).toArray
  }

  private def notStopword(word: String): Boolean = !StopWords.contains(word)

  private def notPunctuation(word: String): Boolean = word.matches("[a-z]+")
}
