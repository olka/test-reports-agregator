package controllers

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.util

import akka.util.ByteString
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.{FileBody, StringBody}
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.http.Writeable
import play.api.mvc.{AnyContentAsMultipartFormData, Codec, MultipartFormData}
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws.WS
import play.api.mvc.MultipartFormData.BadPart
import play.api.mvc.MultipartFormData.FilePart
import play.mvc.Http.HeaderNames


object MultipartFormDataWritable {
  val boundary = "--------ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"

  def formatDataParts(data: Map[String, Seq[String]]) = {
    val dataParts = data.flatMap { case (key, values) =>
      values.map { value =>
        val name = s""""$key""""
        s"--$boundary\r\n${HeaderNames.CONTENT_DISPOSITION}: form-data; name=$name\r\n\r\n$value\r\n"
      }
    }.mkString("")
    Codec.utf_8.encode(dataParts)
  }

  def filePartHeader(file: FilePart[TemporaryFile]) = {
    val name = s""""${file.key}""""
    val filename = s""""${file.filename}""""
    val contentType = file.contentType.map { ct =>
      s"${HeaderNames.CONTENT_TYPE}: $ct\r\n"
    }.getOrElse("")
    Codec.utf_8.encode(s"--$boundary\r\n${HeaderNames.CONTENT_DISPOSITION}: form-data; name=$name; filename=$filename\r\n$contentType\r\n")
  }

  val singleton = Writeable[MultipartFormData[TemporaryFile]](
    transform = { form: MultipartFormData[TemporaryFile] =>
      formatDataParts(form.dataParts) ++
        form.files.flatMap { file =>
          val fileBytes = Files.readAllBytes(Paths.get(file.ref.file.getAbsolutePath))
          filePartHeader(file) ++ fileBytes ++ Codec.utf_8.encode("\r\n")
        } ++
        Codec.utf_8.encode(s"--$boundary--")
    },
    contentType = Some(s"multipart/form-data; boundary=$boundary")
  )

  implicit val anyContentAsMultipartFormWritable: Writeable[AnyContentAsMultipartFormData] = {
    singleton.map(_.mdf)
  }

}


class ApplicationSpec extends PlaySpec with OneAppPerSuite {


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

  "Upload" should {
    "uploadFile returns (File uploaded)" in {
      var lines = util.Arrays.asList("The first line", "The second line")
      Files.write(Paths.get("dataStore/tt"), lines,Charset.forName("UTF-8"))
      val tempFile = TemporaryFile(new java.io.File("dataStore/tt"))
      val part = FilePart[TemporaryFile]("file", "tt2", Some("plain/test"), tempFile)
      val formData = MultipartFormData(dataParts = Map(), files = Seq(part), badParts = Seq())
      val req = FakeRequest(Helpers.POST, "/upload").withMultipartFormDataBody(formData)
      val res = route(app,req)(MultipartFormDataWritable.anyContentAsMultipartFormWritable).get
      status(res) mustBe OK
      contentAsString(res) must include("File has been uploaded")
    }

    "uploadFile returns (Missing file)" in {
      val formData = MultipartFormData(dataParts = Map(), files = Seq[FilePart[TemporaryFile]](), badParts = Seq())
      val req = FakeRequest(Helpers.POST, "/upload").withMultipartFormDataBody(formData)
      val res = route(app,req)(MultipartFormDataWritable.anyContentAsMultipartFormWritable).get
      status(res) mustBe INTERNAL_SERVER_ERROR
      contentAsString(res) must include("File has NOT been uploaded")
    }
  }

  "dataStore directory" should {
    "be clean" in {new File("dataStore").listFiles.foreach(_.delete())}}
}