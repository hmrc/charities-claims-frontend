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
import views.html.CorporateTrusteeDetailsInputView
import controllers.actions.Actions
import forms.TextInputFormProvider
import models.{Mode, OrganisationDetailsAnswers}
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}

class CorporateTrusteeDetailsController {
  val controllerComponents: MessagesControllerComponents,
  view: CorporateTrusteeDetailsInputView,
  actions: Actions,
  formProvider: TextInputFormProvider,
  saveService: SaveService
} (using ec: ExecutionContext)
extends BaseController {

  val form: Form[String] = formProvider(
    "claimReferenceNumberInput.error.required",
    (20, "claimReferenceNumberInput.error.length"),
    "claimReferenceNumberInput.error.regex"
  )

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if OrganisationDetailsAnswers.getAreYouACorporateTrustee.contains(true)
    then {
      val previousAnswer = OrganisationDetailsAnswers.getNameOfCorporateTrustee
      Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }