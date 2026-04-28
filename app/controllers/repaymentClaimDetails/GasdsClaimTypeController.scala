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
import controllers.repaymentClaimDetails.routes
import forms.GasdsClaimTypeFormProvider
import models.Mode.*
import models.SessionData.isCASCCharityReference
import models.{GasdsClaimType, Mode, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveService
import views.html.GasdsClaimTypeView

import scala.concurrent.{ExecutionContext, Future}

class GasdsClaimTypeController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: GasdsClaimTypeView,
  actions: Actions,
  guard: GuardAction,
  formProvider: GasdsClaimTypeFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[GasdsClaimType] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().andThen(guard(SessionData.isClaimNotSubmitted)).async { implicit request =>
      given sessionData: SessionData = request.sessionData
      val previousAnswer             = RepaymentClaimDetailsAnswers.getGasdsClaimType

      val cleanedPrevious = previousAnswer.map { ans =>
        if (isCASCCharityReference) ans.copy(communityBuildings = false)
        else ans
      }
      val preparedForm    = cleanedPrevious match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Future.successful(Ok(view(preparedForm, mode, isCASCCharityReference)))
    }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions.authAndGetData().andThen(guard(SessionData.isClaimNotSubmitted)).async { implicit request =>
      given sessionData: SessionData = request.sessionData
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isCASCCharityReference))),
          value =>
            saveService
              .save(RepaymentClaimDetailsAnswers.setGasdsClaimType(value))
              .map(_ => Redirect(nextPage(value, mode)))
        )
    }

  private def nextPage(value: GasdsClaimType, mode: Mode) =
    mode match {
      case NormalMode =>
        if (value.connectedCharity) {
          routes.ClaimingReferenceNumberController.onPageLoad(NormalMode)
        } else {
          routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode)
        }
      case CheckMode  =>
        routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    }
}
