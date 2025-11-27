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

package controllers.sectionone

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.{CheckMode, Mode, NormalMode, SessionData}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.ClaimingGiftAidSmallDonationsView

import scala.concurrent.{ExecutionContext, Future}

class ClaimingGiftAidSmallDonationsController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimingGiftAidSmallDonationsView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("claimingGiftAidSmallDonations.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData() { implicit request =>
    val previousAnswer = SessionData.SectionOne.getClaimingUnderGasds
    Ok(view(form.withDefault(previousAnswer)))
  }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          saveService
            .save(SessionData.SectionOne.setClaimingUnderGasds(value))
            .map { _ =>
              if (mode == CheckMode) {
                Redirect(controllers.routes.CheckYourAnswersController.onPageLoad())
              } else {
                Redirect(controllers.sectionone.routes.ClaimReferenceNumberCheckController.onPageLoad(NormalMode))
              }
            }
      )
  }
}
