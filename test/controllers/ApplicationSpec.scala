package controllers

import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

class ApplicationSpec extends PlaySpec with OneAppPerSuite {

  "Application" should {

    "send 404 on a bad request" in {
      val Some(wrongRoute) = route(app,FakeRequest(GET, "/boum"))
      status(wrongRoute) mustBe NOT_FOUND
    }

    "render the index page" in {
      val api = route(app,FakeRequest(GET, "/")).get

      status(api) mustBe OK
      contentType(api).get mustBe ("application/json")
      contentAsString(api) must include ("host")
      contentAsString(api) must include ("uri")
      contentAsString(api) must include ("session")
    }
  }
}
