package controllers

import java.io.File
import java.util.concurrent.TimeUnit

import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import com.google.inject.Inject
import models.TestEntry
import play.api.Logger
import play.api.libs.EventSource
import play.api.libs.Files.TemporaryFile
import play.api.libs.ws._
import play.api.mvc._
import play.api.libs.json.Json._
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class Application @Inject()(wsClient: WSClient)(implicit ec: ExecutionContext) extends Controller {
  private val log: Logger = Logger(this.getClass)

  def index = Action(implicit request => Ok(views.html.index(Utils.getListOfFiles("./dataStore"))))

  def main = Action(implicit request => Ok(views.html.main("Title")(play.twirl.api.Html("Content"))))

  def doUpload(request: Request[MultipartFormData[TemporaryFile]]): Result = {
    request.body.file("file").map { file =>
      val filename = file.filename
      val contentType = file.contentType.get
      val prefix = System.currentTimeMillis().toString.substring(4)
      file.ref.moveTo(new File(s"./dataStore/$prefix$filename"))
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

  //Producer
  def timeline() = Action {
    //XmlTestResultParser.getJunitTestsMetadata()
    val source = Source.tick(FiniteDuration(0,TimeUnit.SECONDS), FiniteDuration(1,TimeUnit.SECONDS), tick = XmlTestResultParser.getJunitTestsMetadata())
    Ok.chunked(source.map { tick =>
      obj(
        "filename" -> JsString(tick.take(3).toString())
      ).toString + "\n"
    }.limit(100))
  }

  //Consumer
  def mixedStream() = Action {
    val keywordSources = Source("1,1,1,2".split(",").toList)
    val responses = keywordSources.flatMapMerge(10, queryToSource)
    Ok.chunked(responses via EventSource.flow)
  }

  private def queryToSource(tt: String) = {
    val request = wsClient.url("http://localhost:9000/timeline")
    streamResponse(request)
      .via(framing)
      .map { byteString =>
        val json = Json.parse(byteString.utf8String)
        val tweetInfo = TestEntry((json \ "title").as[String], (json \ "message").as[String], (json \ "isFailed").as[Boolean])
        Json.toJson(tweetInfo)
      }
  }
  val framing = Framing.delimiter(ByteString("\n"), maximumFrameLength = 100, allowTruncation = true)
  private def streamResponse(request: WSRequest) = Source.fromFuture(request.stream()).flatMapConcat(_.body)

  private def getTestData = {
    import java.util.Random
    val titles = List("1", "2", "3")
    val messages = List("PASSED", "NullPointer", "StackOverflow")
    val rand = new Random()
    TestEntry(titles(rand.nextInt(titles.length)), messages(rand.nextInt(messages.length)), false)
  }

  def mixed() = Action {
//    Ok(views.html.mixed)
    Ok("Mixed")
  }

  def xmlParser = Action(parse.xml) { request =>
    (request.body \\ "name" headOption).map(_.text).map { name =>
      Ok("Hello " + name)
    }.getOrElse {
      BadRequest("Missing parameter [name]")
    }
  }

}
