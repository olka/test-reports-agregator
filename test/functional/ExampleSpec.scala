package functional

import controllers.TestEnv
import org.scalatest.tags.FirefoxBrowser
import org.scalatestplus.play._

@FirefoxBrowser
class ExampleSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with FirefoxFactory {
    "Webdriver: test endpoint" in {
      go to (s"http://localhost:$port/test")
      pageSource must include ("host")
      pageSource must include ("uri")
      pageSource must include ("session")
    }

  "Webdriver: index page" in {
    go to (s"http://localhost:$port/")
    pageSource must include ("Uploaded files")
    pageSource must include (TestEnv.TMP_FILE)
  }

  "Webdriver: main page" in {
    go to (s"http://localhost:$port/main")
    pageTitle must be ("Title")
    pageSource must include ("Content")
  }
}