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

package controllers.claimDeclaration

import services.{SaveService, UnregulatedDonationsService}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import controllers.BaseController
import views.html.AdjustmentToThisClaimView
import controllers.actions.{Actions, GuardAction}
import forms.AdjustmentToThisClaimFormProvider
import models.{DeclarationDetailsAnswers, SessionData}
import play.api.data.Form
import controllers.claimDeclaration.routes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentToThisClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentToThisClaimView,
  actions: Actions,
  guard: GuardAction,
  formProvider: AdjustmentToThisClaimFormProvider,
  saveService: SaveService,
  unregulatedDonationsService: UnregulatedDonationsService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Option[String]] = formProvider(
    "adjustmentToThisClaim.error.required",
    (350, "adjustmentToThisClaim.error.length"),
    "adjustmentToThisClaim.error.regex",
    optionalFlag = true // TODO - this flag to make the textarea optional or not
  )

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.isClaimDetailsComplete)
      .async { implicit request =>
        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        if (request.sessionData.unregulatedLimitExceeded) {
          // user already saw WRN5 and chose to continue we show the form without re-checking
          val previousAnswer = DeclarationDetailsAnswers.getIncludedAnyAdjustmentsInClaimPrompt
          Future.successful(Ok(view(form.withDefault(Some(previousAnswer)))))
        } else {
          unregulatedDonationsService.checkUnregulatedLimit.flatMap {
            case Some(_) =>
              val updatedSession = request.sessionData.copy(unregulatedLimitExceeded = true)
              saveService.save(updatedSession).map { _ =>
                Redirect(controllers.routes.RegisterCharityWithARegulatorController.onPageLoad)
              }

            case None =>
              val previousAnswer = DeclarationDetailsAnswers.getIncludedAnyAdjustmentsInClaimPrompt
              Future.successful(Ok(view(form.withDefault(Some(previousAnswer)))))
          }
        }
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndGetDataWithGuard(SessionData.isClaimDetailsComplete)
      .async { implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
            value =>
              saveService
                .save(
                  DeclarationDetailsAnswers.setIncludedAnyAdjustmentsInClaimPrompt(Some(value))
                )
                .map(_ =>
                  Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
                ) // TODO - redirect when next page available
          )
      }
}
