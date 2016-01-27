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

import play.Logger

object Users extends Controller with Secured{
	def list = IsAuthenticated{user => implicit request =>
		Ok(views.html.admin.user.list(User.list))
	}

	 def addinit = Action { implicit request =>
    Ok(views.html.admin.user.edit(myForm, 0))//
  }
	  
	def add = IsAuthenticated{user => _ =>
		Ok(views.html.admin.user.edit(myForm, 0))
	}
		
	def insert = IsAuthenticated{user => implicit request =>
		myForm.bindFromRequest.fold(
			errors => BadRequest(views.html.admin.user.edit(errors, 0)),
			values => {
				User.insertOrUpdate(values)

				
				Redirect(routes.Users.list)
			}
		)
	}

	def edit(id: Long) = IsAuthenticated{user => _ =>
		Ok(views.html.admin.user.edit(myForm.fill(User.edit(id)), id))
	}

	def update(id: Long) = IsAuthenticated{user => implicit request =>
		myForm.bindFromRequest.fold(
			errors => BadRequest(views.html.admin.user.edit(errors, id)),
			values => {
				User.insertOrUpdate(values, id)
				Redirect(routes.Users.list)
			}
		)
	}

	def delete(id: Long) = IsAuthenticated{user => _ =>
		User.delete(id)
		Redirect(routes.Users.list)
	}

	val myForm = {

		import java.util.Date
		
		Form(
		mapping(
			"id"		-> ignored(NotAssigned:Pk[Long]),
			"name"		-> nonEmptyText,
			"email"		-> email
		)
		(User_e.apply)(User_e.unapply)
	)}

	val passwordForm = Form(
		tuple(
			"password"		-> nonEmptyText,
			"password2"		-> nonEmptyText
		) verifying("passwords not identical", result => result match{ 
   		case (p1, p2) => p1 == p2
		})
	)

	def changePassword = IsAuthenticated{user => _ =>
		Ok(views.html.admin.user.changePassword(passwordForm))
	}

	def insertPassword = IsAuthenticated{user => implicit request =>
		passwordForm.bindFromRequest.fold(
			errors => Ok(views.html.admin.user.changePassword(errors)),
			values => {
				User.changePassword(user, values._1)
				Redirect(routes.Users.list)
				.flashing(
    				"success" -> "password.changed.successfully"
  				)
			}
		)
	}
}