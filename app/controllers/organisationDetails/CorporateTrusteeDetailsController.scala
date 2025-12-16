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

import models.Mode.*
import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import views.html.CorporateTrusteeDetailsView
import controllers.actions.Actions
import forms.CorporateTrusteeDetailsFormProvider
import models.{CorporateTrusteeDetails, Mode, OrganisationDetailsAnswers}
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}

class CorporateTrusteeDetailsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CorporateTrusteeDetailsView,
  actions: Actions,
  formProvider: CorporateTrusteeDetailsFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form = formProvider(
    "corporateTrusteeDetails.name.error.required",
    "corporateTrusteeDetails.name.error.length",
    "corporateTrusteeDetails.name.error.regex",
    "corporateTrusteeDetails.name.hint",
    "corporateTrusteeDetails.phone.error.required",
    "corporateTrusteeDetails.phone.error.length",
    "corporateTrusteeDetails.phone.error.regex",
    "corporateTrusteeDetails.phone.hint",
    "corporateTrusteeDetails.postCode.error.required",
    "corporateTrusteeDetails.postCode.error.length",
    "corporateTrusteeDetails.postCode.error.regex",
    "corporateTrusteeDetails.postCode.hint"
  )

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if (
      OrganisationDetailsAnswers.getAreYouACorporateTrustee
        .contains(true) && OrganisationDetailsAnswers.getDoYouHaveUKAddress.contains(true)
    ) {
      val previousAnswerName     = OrganisationDetailsAnswers.getNameOfCorporateTrustee
      val previousAnswerPhone    = OrganisationDetailsAnswers.getCorporateTrusteeDaytimeTelephoneNumber
      val previousAnswerPostCode = OrganisationDetailsAnswers.getCorporateTrusteePostcode
      val previousAnswer         =
        CorporateTrusteeDetails(
          name = previousAnswerName,
          phoneNumber = previousAnswerPhone,
          postCode = previousAnswerPostCode
        )
      Future.successful(Ok(view(form.withDefault(Some(previousAnswer)))))
    } else if (
      OrganisationDetailsAnswers.getAreYouACorporateTrustee
        .contains(true) && OrganisationDetailsAnswers.getDoYouHaveUKAddress.contains(false)
    ) {
      val previousAnswerName  = OrganisationDetailsAnswers.getNameOfCorporateTrustee
      val previousAnswerPhone = OrganisationDetailsAnswers.getCorporateTrusteeDaytimeTelephoneNumber
      val previousAnswer      = CorporateTrusteeDetails(name = previousAnswerName, phoneNumber = previousAnswerPhone)
      Future.successful(Ok(view(form.withDefault(Some(previousAnswer)))))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          saveService
            .save(OrganisationDetailsAnswers.setNameOfCorporateTrustee(value.name)
                .setCorporateTrusteeDaytimeTelephoneNumber(value.phoneNumber)
                .setCorporateTrusteePostcode(value.postCode))
            .map { _ =>
              Redirect(routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode))
            }
      )
  }
}
