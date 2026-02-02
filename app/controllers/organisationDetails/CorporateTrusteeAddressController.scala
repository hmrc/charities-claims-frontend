/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.organisationDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.OrganisationDetailsAnswers
import models.Mode
import models.Mode.*
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.CorporateTrusteeAddressView

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Call

class CorporateTrusteeAddressController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CorporateTrusteeAddressView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("corporateTrusteeAddress.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if OrganisationDetailsAnswers.getAreYouACorporateTrustee.contains(true)
    then {
      val previousAnswer = OrganisationDetailsAnswers.getDoYouHaveCorporateTrusteeUKAddress
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else { Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad)) }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswer = OrganisationDetailsAnswers.getDoYouHaveCorporateTrusteeUKAddress

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(OrganisationDetailsAnswers.setDoYouHaveCorporateTrusteeUKAddress(value))
            .map(_ => Redirect(CorporateTrusteeAddressController.nextPage(value, mode, previousAnswer)))
      )
  }
}

object CorporateTrusteeAddressController {

  def nextPage(value: Boolean, mode: Mode, previousAnswer: Option[Boolean]): Call =
    (value, mode, previousAnswer) match {
      // NormalMode
      case (_, NormalMode, _) => routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode)

      // CheckMode
      // No to Yes - need to collect postcode
      case (true, CheckMode, Some(false)) => routes.CorporateTrusteeDetailsController.onPageLoad(CheckMode)

      // unchanged or Yes to No
      case (_, CheckMode, _) => routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
    }
}
