import org.scalatest.FunSuite

class MainTest extends FunSuite {
  test("main tautology") {
    assert(true)
  }

  test("main equality") {
    val two = 1 + 1
    assert(two == 2)
  }
}
