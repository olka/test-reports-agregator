package controllers

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play._
import play.api.http.Writeable
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{AnyContentAsMultipartFormData, Codec, MultipartFormData}
import play.api.test.Helpers._
import play.api.test._
import play.mvc.Http.HeaderNames

class ApplicationSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfter with FilePreparator {


  "Application" should {
    "send 404 on a bad request" in {
      val Some(wrongRoute) = route(app, FakeRequest(GET, "/boum"))
      status(wrongRoute) mustBe NOT_FOUND
    }

    "render the test page" in {
      val api = route(app, FakeRequest(GET, "/test")).get

      status(api) mustBe OK
      contentType(api).get mustBe ("application/json")
      contentAsString(api) must include("host")
      contentAsString(api) must include("uri")
      contentAsString(api) must include("session")
    }

    "render the index page" in {
      val index = route(app, FakeRequest(GET, "/")).get

      status(index) mustBe OK
      contentType(index).get mustBe ("text/html")
      contentAsString(index) must include("Uploaded files")
    }
  }

  "Get files list from akka stream" in {
    prepareFiles()
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val api = route(app, FakeRequest(GET, "/timeline")).get

    status(api) mustBe OK
    contentType(api).get mustBe ("text/plain")
    contentAsString(api) must include("Test 1")
    contentAsString(api) must include("Test 2")
    contentAsString(api) must include("failures: 99")
  }

    "dataStore directory" should {
      "be clean" in {
        deleteFiles() must be (true)
      }
  }
}