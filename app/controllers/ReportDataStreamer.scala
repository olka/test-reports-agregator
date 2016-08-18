package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import play.api.libs.EventSource
import play.api.libs.json.JsString
import play.api.libs.json.Json._
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import play.api.http.ContentTypes
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class ReportDataStreamer @Inject()(implicit ec: ExecutionContext) extends Controller{

  def timeline() = Action {
    //    val source = Source.tick(FiniteDuration(0,TimeUnit.SECONDS), FiniteDuration(1,TimeUnit.SECONDS), tick = XmlTestResultParser.getJunitTestsMetadata())
    val source = Source(XmlTestResultParser.getJunitTestsMetadata())
    Ok.chunked(source.map { tick =>
      obj("filename" -> JsString(tick.toString())).toString + "\n"}.limit(100))
  }

  def reportStream() = Action {
    Ok.chunked(Source.single(XmlTestResultParser.getJunitTestsMetadataAsString()) via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }
}
