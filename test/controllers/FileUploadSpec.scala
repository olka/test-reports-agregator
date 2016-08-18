package controllers

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.util

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Writeable
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{AnyContentAsMultipartFormData, Codec, MultipartFormData}
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
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
        } ++ Codec.utf_8.encode(s"--$boundary--")
    }, contentType = Some(s"multipart/form-data; boundary=$boundary"))

  implicit val anyContentAsMultipartFormWritable: Writeable[AnyContentAsMultipartFormData] = singleton.map(_.mdf)
}


class FileUploadSpec extends PlaySpec with OneAppPerSuite with FilePreparator {

    "Upload" should {
      "uploadFile returns (File uploaded)" in {
        prepareFiles()
        val tempFile = TemporaryFile(new java.io.File(TestEnv.TMP_FOLDER + TestEnv.TMP_FILE))
        val part = FilePart[TemporaryFile]("file", "Test1.xml", Some("plain/test"), tempFile)
        val formData = MultipartFormData(dataParts = Map(), files = Seq(part), badParts = Seq())
        val req = FakeRequest(Helpers.POST, "/upload").withMultipartFormDataBody(formData)
        val res = route(app, req)(MultipartFormDataWritable.anyContentAsMultipartFormWritable).get
        status(res) mustBe OK
        contentAsString(res) must include("File has been uploaded")
      }

      "uploadFile returns (Missing file)" in {
        val formData = MultipartFormData(dataParts = Map(), files = Seq[FilePart[TemporaryFile]](), badParts = Seq())
        val req = FakeRequest(Helpers.POST, "/upload").withMultipartFormDataBody(formData)
        val res = route(app, req)(MultipartFormDataWritable.anyContentAsMultipartFormWritable).get
        status(res) mustBe INTERNAL_SERVER_ERROR
        contentAsString(res) must include("File has NOT been uploaded")
      }
    }
}
