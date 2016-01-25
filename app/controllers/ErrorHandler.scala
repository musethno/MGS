package controllers

/*

// Error Handling in Play 2.4.


import javax.inject.Inject

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._



class ErrorHandler @Inject()(pushover: Pushover) extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      request.contentType match {
        case Some("application/json") =>  Status(statusCode)(Json.obj("ERROR" -> "client.error","DETAIL" -> Json.toJson("A client error occurred. A client error occurred. Please try again."),"message" -> Json.toJson("A client error occurred.")))
        case Some("application/xml") => Status(statusCode)(<error>A client error occurred. Please try again.</error>)
        case _  => Status(statusCode)(views.html.dashboard.error.wrapper("error"))
      }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {

    Logger.error("Internal Server Error in " + request.path,exception)

    Future.successful(

      request.contentType match {
        case Some("application/json") =>  InternalServerError(Json.obj("ERROR" -> "server.error","DETAIL" -> "internal server error","message" -> "Parameter missing or Internal Server Error"))
        case Some("application/xml") => InternalServerError(<serverError>An error occured. Please try again.</serverError>)
        case _  => InternalServerError(views.html.dashboard.error.wrapper("error",true))
      }
    )
  }
}

*/