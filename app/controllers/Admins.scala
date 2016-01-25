package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import models._
import views._

import anorm._
import anorm.SqlParser._

import java.io.File
import play.libs.Json;


object Admins extends Controller with Secured{

  def index = IsAuthenticated{user => _ =>
    Ok(views.html.admin.index(Exhibition.list(Some(0))))
  }

	def docs = IsAuthenticated{user => _ =>
		Ok(views.html.admin.docs(""))
	}

	def eximport = IsAuthenticated{user => _ =>
		Ok(views.html.admin.eximport(""))
	}

}

object Backups extends Controller with Secured{


	/*
		Returns Database dump
		arg are taken from config file
		bash exec is done in separate .sh file (dumpy.sh), to facilitate editing
	*/
	def dumpDB = IsAuthenticated{user => _ =>
		import scala.sys.process._
		import play.api.Play.current
		val user:String 		= Play.application.configuration.getString("db.default.user").getOrElse("")
		val password:String 	= Play.application.configuration.getString("db.default.password").getOrElse("")
		val db:String 			= "museum"

		val result:String = ("./dumpy.sh").!!
		Ok(result)
	}

	def dumpImages =IsAuthenticated{user => _ =>

		import scala.concurrent._
		import ExecutionContext.Implicits.global		

		// first create zip archive
		import scala.sys.process._
		val a:String = ("./zippy.sh").!!
		Logger.info(a)

		//import ExecutionContext.Implicits.global
		import play.api.libs.iteratee._
		val file = new java.io.File("archive-image.zip")
		val fileContent:Enumerator[Array[Byte]] = Enumerator.fromFile(file)

		SimpleResult(
			header = ResponseHeader(200),
			body = fileContent
		)
	}

	def getFiles =IsAuthenticated{user => _ =>
		Ok("ok")
	}
}
