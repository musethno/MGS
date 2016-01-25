/**
 * Created by raphael on 10/26/15.
 */

import models.Exhibition
import play.api._
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(
      request.contentType match {
        case _ => InternalServerError(views.html.public.home(Exhibition.list(Some(0)).filter((a: Exhibition) => a.status_id)))
      }
    )
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(
      request.contentType match {
        case _ => NotFound(views.html.public.home(Exhibition.list(Some(0)).filter((a: Exhibition) => a.status_id)))
      }
    )
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(
      request.contentType match {
        case _ => BadRequest(views.html.public.home(Exhibition.list(Some(0)).filter((a: Exhibition) => a.status_id)))
      }
    )
  }

}
