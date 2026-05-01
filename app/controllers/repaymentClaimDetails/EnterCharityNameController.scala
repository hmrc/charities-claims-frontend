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
import controllers.BaseController
import controllers.actions.{Actions, GuardAction}
import models.Mode.{CheckMode, NormalMode}
import forms.CharityNameFormProvider
import play.api.data.Form
import models.{Mode, RepaymentClaimDetailsAnswers, SessionData}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.SaveService
import views.html.EnterCharityNameView

import scala.concurrent.{ExecutionContext, Future}

class EnterCharityNameController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  guard: GuardAction,
  formProvider: CharityNameFormProvider,
  view: EnterCharityNameView,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().andThen(guard(SessionData.isClaimNotSubmitted)).async { implicit request =>
      request.isAgent match {
        case true =>
          val previousAnswer = RepaymentClaimDetailsAnswers.getNameOfCharity match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(previousAnswer, mode)))

        case _ =>
          // TODO: Build org view for this screen
          println("access denied")
          Future.successful(Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad))

      }
    }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().andThen(guard(SessionData.isClaimNotSubmitted)).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            saveService
              .save(RepaymentClaimDetailsAnswers.setNameOfCharity(value))
              .map(_ => Redirect(navigator(mode)))
        )
    }

  def navigator(mode: Mode): Call = mode match {
    case NormalMode =>
      routes.RepaymentClaimTypeController.onPageLoad(mode)
    case CheckMode  =>
      routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
  }
}
