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
import forms.CorporateTrusteeDetailsFormProvider
import models.{Mode, OrganisationDetailsAnswers}
import models.Mode.*
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.CorporateTrusteeDetailsView
import views.html.CorporateTrusteeDetailsWithOutAddessView

import scala.concurrent.{ExecutionContext, Future}

class CorporateTrusteeDetailsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CorporateTrusteeDetailsView,
  viewWithOutAddress: CorporateTrusteeDetailsWithOutAddessView,
  actions: Actions,
  formProvider: CorporateTrusteeDetailsFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if (OrganisationDetailsAnswers.getAreYouACorporateTrustee.contains(true)) {
      val isUKAddress    = OrganisationDetailsAnswers.getDoYouHaveUKAddress.getOrElse(false)
      val previousAnswer = OrganisationDetailsAnswers.getCorporateTrusteeDetails
      val form           = formProvider(
        isUKAddress,
        "corporateTrusteeDetails.name.error.required",
        "corporateTrusteeDetails.name.error.length",
        "corporateTrusteeDetails.name.error.regex",
        "corporateTrusteeDetails.phone.error.required",
        "corporateTrusteeDetails.phone.error.length",
        "corporateTrusteeDetails.phone.error.regex",
        "corporateTrusteeDetails.postCode.error.required",
        "corporateTrusteeDetails.postCode.error.length",
        "corporateTrusteeDetails.postCode.error.regex"
      )
      if OrganisationDetailsAnswers.getDoYouHaveUKAddress.contains(true) then
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
      else Future.successful(Ok(viewWithOutAddress(form.withDefault(previousAnswer), mode)))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>

    val isUKAddress = OrganisationDetailsAnswers.getDoYouHaveUKAddress.getOrElse(false)
    val form        = formProvider(
      isUKAddress,
      "corporateTrusteeDetails.name.error.required",
      "corporateTrusteeDetails.name.error.length",
      "corporateTrusteeDetails.name.error.regex",
      "corporateTrusteeDetails.phone.error.required",
      "corporateTrusteeDetails.phone.error.length",
      "corporateTrusteeDetails.phone.error.regex",
      "corporateTrusteeDetails.postCode.error.required",
      "corporateTrusteeDetails.postCode.error.length",
      "corporateTrusteeDetails.postCode.error.regex"
    )
    if isUKAddress then {
      Logger(getClass).error(s"**** first: $isUKAddress...")
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            saveService
              .save(OrganisationDetailsAnswers.setCorporateTrusteeDetails(value))
              .map { _ =>
                Redirect(
                  routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode)
                ) // TODO once check your answers has been done
              }
        )
    } else {
      Logger(getClass).error(s"**** second: $isUKAddress...")
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(viewWithOutAddress(formWithErrors, mode))),
          value =>
            saveService
              .save(OrganisationDetailsAnswers.setCorporateTrusteeDetails(value))
              .map { _ =>
                Redirect(
                  routes.CorporateTrusteeDetailsController.onPageLoad(NormalMode)
                ) // TODO once check your answers has been done
              }
        )
    }
  }
}
