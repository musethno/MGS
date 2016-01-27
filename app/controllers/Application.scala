package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.{Action, Controller}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.i18n.Lang
import play.api.Logger
import play.api.Play.current
import models._
import views._

import anorm._
import anorm.SqlParser._

import java.io.File
import play.libs.Json;



object Application extends Controller with ChangeLanguage with Secured{

  
  def index = Action{implicit request => 
    var install_flag =  Utils.flag();
    if(install_flag == Some(0)){
      Ok(views.html.public.home(
  		  Exhibition.list(Some(0)).filter((a: Exhibition) => a.status_id)
        ))
    }  
    else{
      Ok("error")
    }
      
  }

  def menu(id: Long) = Action{implicit request => 

    val exhibition = Exhibition.detail(id)

    if(exhibition.isDefined && exhibition.get.status_id){
      Ok(views.html.public.menu(
        exhibition.get,
        Exhibition.list(Some(id))
      ))  
    }
    else{
      Ok("error")
    }

    
  }

  def submenu(id: Long) = Action{implicit request => 
    val exhibition = Exhibition.detail(id)

    if(exhibition.isDefined){
      Ok(views.html.public.submenu(
        exhibition.get
        , Exhibition.list(Some(id))
        , Exhibition.types
        , Utils.formatter
      ))  
    }
    else{
      Ok("error")
    }
  }
  
    def detail(id: Long) = Action {
    val exhibition = Exhibition.detail(id)
    if(exhibition.isDefined){
      Ok(views.html.public.detail(exhibition.get, Exhibition.types, Utils.formatter))
    }
    else{
      Ok("error")
    }

  }

  /*
    returns page by number (if number is associated with id)a
  */
  def detailByNumber = Action{implicit request =>
    searchForm.bindFromRequest.fold(
      errors => Redirect(routes.Application.index)
      , values => {
        val id: Option[Long] = Exhibition.getIdFromNumber(values.number)

        if(id.isDefined){
          Redirect(routes.Application.detail(id.get))
        }
        else{
          Ok(views.html.public.nodetail())
        }
      }
    )
  }

  /*
    returns page by number (if number is associated with id)
    - same as detailByNumber - but not called via a form with POST
  */
  def detailByNumberGET(nr: Long) = Action{

    val id: Option[Long] = Exhibition.getIdFromNumber(nr)

    if(id.isDefined){
      Redirect(routes.Application.detail(id.get))
    }
    else{
      Ok(views.html.public.nodetail())
    }
  }

  def numberExists = Action{implicit request =>
    import play.api.libs.json.Json

    searchForm.bindFromRequest.fold(
      errors  => {
        val r = 
        Json.stringify(Json.obj(
          "status" -> "error"
        ))
        Ok(r)
      },
      values => {
        val id: Option[Long] = Exhibition.getIdFromNumber(values.number)
        val r = 
        Json.stringify(Json.obj(
          "status" -> {if(id.isDefined){"success"}else{"error"}}
        ))
        Ok(r)
      }
    )

  }

  def gallery(id: Long, pic_id: Long) = Action{
    Ok(views.html.public.gallery(Exhibition.Gallery.detail(pic_id)))
  }


  val searchForm = Form(
    mapping(
      "number"    -> longNumber
    )
    (Search.apply)(Search.unapply)
  )

  // -- Authentication
  val loginForm = Form(
  tuple(
    "email" -> text,
    "password" -> text
  ) verifying ("Invalid email or password", result => result match {
    case (email, password) => User.authenticate(email, password).isDefined
  })
  )
  
/**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))//
  }

  /**
   * Handle login form submission.
	* TODO: make nicer: call User authnticte only ONCE!
   */
	def authenticate = Action { implicit request =>
    	loginForm.bindFromRequest.fold(
      		formWithErrors => BadRequest(html.login(formWithErrors)),
      		user => {

            val uo = User.authenticate(user._1, user._2)

            if(uo.isDefined){
              val u = uo.get

              // define redirect URL
              var redUrl:String = routes.Admins.index.toString
              if(request.session.get("url").isDefined){
                request.session.get("url").get
              }

              Redirect(redUrl)
              .withNewSession
              .withSession(
                "email"   -> u.email, 
                "id"      -> u.id.toString,
                "name"    -> u.name
              )
            }
            else{
              Ok("error71")
            }
          }
    	)
	}

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Admins.index)
    .withNewSession
    .flashing(
      "success" -> "You've been logged out"
    )
  }



  /* static pages */
  def impressum = Action {
    Ok(views.html.public.impressum())
  }

}



/**
 * Provide security features
 */

trait Secured {
	
  
  
  	private def username(request: RequestHeader):Option[User] = {

      if(request.session.get("id").isDefined && request.session.get("email").isDefined && request.session.get("name").isDefined){
        Some(
          User(
            Id(request.session.get("id").get.toLong),
            request.session.get("email").get,
            request.session.get("name").get
          )
        )
      }
      else{
        None
      }
	}
  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = {
    Results.Redirect(routes.Application.login)
    .withSession("url" -> request.path.toString) // store current path, so user can be directly redirected to right page once logged in
  }
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(p: play.api.mvc.BodyParser[AnyContent])(f: => User => Request[AnyContent] => Result) = {

    Security.Authenticated(username, onUnauthorized) { user =>
      Action(p){
        request => f(user)(request)
      }
    }
  }


  def IsAuthenticated(f: => User => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action{
        request => {
          f(user)(request)
        }
      }
    }
  }

  def IsAuthenticated(a: Long)(f: => User => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action{
        request => {
          Logger.info(a.toString)
          f(user)(request)
        }
      }
    }
  }

}
