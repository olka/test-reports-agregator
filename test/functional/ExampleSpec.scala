package functional

import org.scalatest.tags.FirefoxBrowser
import org.scalatestplus.play._

@FirefoxBrowser
class ExampleSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with FirefoxFactory {
    "provide a web driver" in {
      go to (s"http://localhost:$port/test")
      pageSource must include ("host")
      pageSource must include ("uri")
      pageSource must include ("session")
    }
}