package controllers

import java.io.File

import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.api.libs.json.Json._

class Application extends Controller {
  private val log: Logger = Logger(this.getClass)

  def index = Action(implicit request => Ok(views.html.index(Utils.getListOfFiles("./dataStore"))))

  def main = Action(implicit request => Ok(views.html.main("Title")(play.twirl.api.Html("Content"))))

  def report = Action(implicit request => Ok(views.html.report()))

  def doUpload(request: Request[MultipartFormData[TemporaryFile]]): Result = {
    request.body.file("file").map { file =>
      val filename = file.filename
      val contentType = file.contentType.get
      val prefix = System.currentTimeMillis().toString.substring(4)
      file.ref.moveTo(new File(s"./dataStore/($prefix)$filename"))
      log.info(s"$filename : $contentType")
      Ok("File has been uploaded")
    }.getOrElse {
      log.error("Upload failed!")
      Redirect(routes.Application.index)
      InternalServerError("File has NOT been uploaded")
    }
  }

  def uploadFile = Action(parse.multipartFormData) { request =>
    doUpload(request)
  }

  def test = Action(implicit request => Ok(currentApi))

  private def currentApi(implicit request: RequestHeader) = toJson(
    Map[String,String](
        "host" -> (request.host+"."+request.domain),
        "uri"-> request.uri,
        "session"->request.session.toString))
}
