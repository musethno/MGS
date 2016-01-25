
package controllers
import play.api.mvc.{Action, Controller}
import play.api.i18n.Lang
import play.api.data._
import play.api.data.Forms._
import play.api.Logger
import play.api.Play.current

trait ChangeLanguage extends Controller {

  protected val HOME_URL = "/"
  val localeForm = Form("locale" -> nonEmptyText)

  val updateLocale = Action { implicit request =>
    val referrer = request.headers.get(REFERER).getOrElse(HOME_URL)
    localeForm.bindFromRequest.fold(
      errors => {
        BadRequest(referrer)
      },
      locale => {
        Redirect(referrer).withLang(Lang(locale))
      })
  }
}

