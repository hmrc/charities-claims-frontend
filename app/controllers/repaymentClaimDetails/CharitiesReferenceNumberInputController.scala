/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.repaymentClaimDetails

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.{Actions, GuardAction}
import forms.CharitiesReferenceTextInputFormProvider
import models.Mode.*
import models.{Mode, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
import views.html.CharitiesReferenceNumberInputView

import scala.concurrent.{ExecutionContext, Future}

class CharitiesReferenceNumberInputController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CharitiesReferenceNumberInputView,
  actions: Actions,
  guard: GuardAction,
  formProvider: CharitiesReferenceTextInputFormProvider,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[String] = formProvider(
    "charitiesReferenceNumber.error.required",
    (7, "charitiesReferenceNumber.error.length"),
    "charitiesReferenceNumber.error.regex"
  )

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(guard(SessionData.isClaimNotSubmitted))
      .async { implicit request =>
        val previousAnswer = Some("")
        Future.successful(Ok(view(form.withDefault(previousAnswer), mode)))
      }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(guard(SessionData.isClaimNotSubmitted))
      .async { implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              saveService
                .save(RepaymentClaimDetailsAnswers.setHmrcCharitiesReference(value))
                .map(_ => Redirect(navigator(mode)))
          )
      }

  def navigator(mode: Mode): Call = mode match {
    case NormalMode =>
      routes.EnterCharityNameController.onPageLoad(mode)
    case CheckMode  =>
      routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
  }
}
