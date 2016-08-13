package controllers

import play.api.mvc._
import play.api.libs.json.Json._

class Application extends Controller {

  def index = Action(implicit request => Ok(currentApi))

  private def currentApi(implicit request: RequestHeader) = toJson(
    Map[String,String](
        "host" -> (request.host+"."+request.domain),
        "uri"-> request.uri,
        "session"->request.session.toString))
}