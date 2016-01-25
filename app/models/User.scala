package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import java.util.Date


import play.api.templates.Html

case class User(id: Pk[Long], email: String, name: String)
case class User_e(id: Pk[Long], name: String, email: String)

object User {
	val table:String = "user"


  /**
   * Authenticate a User.
	* todo here : only admin!!
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         SELECT u.id, u.email, first_name
 			FROM """+table+""" u
			WHERE email = {email} AND password = MD5({password})
        """
      )
      .on(
        'email -> email,
        'password -> password
      )
      .as(simple.singleOpt)
    }
  }

	def checkPassword(id: Long, password: String): Option[User] = {
		DB.withConnection { implicit connection =>
	      SQL(
	        """
	         SELECT u.id, u.email, CONCAT(u.first_name, " ", u.surname) as name
	 			FROM """+table+""" u
				WHERE u.id = {id} AND password = MD5({password})
	        """
	      )
	      .on(
	        'id -> id,
	        'password -> password
	      )
	      .as(simple.singleOpt)
		}
	}
	
	
	def changePassword(user: User, password: String){
		DB.withConnection { implicit connection =>
	    	SQL(
	        """
	         	UPDATE """+table+""" SET password=MD5({password}) WHERE id={id}
	        """
	      	)
			.on(
	        	'id -> user.id,
	        	'password -> password
	      	)
			.executeUpdate()
	    }
	}
	
	def edit(id: Long): User_e = {
		DB.withConnection { implicit connection =>
	      SQL(
	        """
	         SELECT u.id, u.email, u.first_name, u.surname, phone
	 			FROM """+table+""" u
				WHERE u.id = {id}
	        """
	      ).on(
	        'id -> id
	      ).as(details single)
	    }
	}

	def list: List[User] = {
		DB.withConnection { implicit connection =>
	      SQL(
	        """
	         SELECT u.id, u.email, u.first_name, u.surname, phone
	 			FROM """+table+""" u
				WHERE 1
	        """
	      ).as(simple *)
	    }
	}

	def insertOrUpdate(user: User_e, id: Long = 0) {

		val query = 
			{if(id>0){"UPDATE"}else{"INSERT INTO"}}+
			" "+table+
			"""
				SET first_name={name}
				, email={email}
			"""+
			{if(id>0){" WHERE id={id}"}else{""}}

		DB.withConnection{implicit connection =>
			SQL(query)
			.on(
				'id 	-> id,
				'name -> user.name,
				'email -> user.email
			)
			.executeUpdate
		}

		if(id==0){
			User.setPassword(Utils.getLastId(User.table).get)
		}
	}

	def setPassword(id: Long){
		val password:String = Utils.generatePassword(8).toString

		DB.withConnection{implicit c =>
			SQL("""
				UPDATE """+table+""" SET password=MD5({password}) WHERE id={id}
			""")
			.on('id -> id, 'password -> password)
			.executeUpdate()
		}

		// get user
		val user = edit(id)

		// send email
		val a:Boolean = try { 
		  import com.typesafe.plugin._
			import views._
			
			val mail = use[MailerPlugin].email
			mail.setSubject(play.api.i18n.Messages("email.header"))
			mail.addRecipient(user.name+" <"+user.email+">","johan@nexys.ch")
			//mail.addRecipient(client.email)
			mail.addFrom("Nexys <support@nexys.ch>")
			mail.send(views.html.emails.account_gen(user, password).toString)

			true
		}
		catch {
		  case _: Throwable => false 
		}
		
	}

	def delete(id: Long) = {
		DB.withConnection { implicit connection =>
	      SQL(
	        """
	         DELETE
	 			FROM """+table+"""
				WHERE id = {id}
	        """
	      )
	      .on('id -> id)
	      .executeUpdate
	    }
	}

	// -- Parsers
	val simple = {
		get[Pk[Long]]("id")~
		get[String]("email") ~
		get[String]("first_name")  map {
			case id~email~name => User(id, email, name)
		}
	}

	val details = {
		get[Pk[Long]]("id")~
		get[String]("first_name") ~
		get[String]("email")  map {
			case id~name~email => User_e(id, name, email)
		}
	}

}
