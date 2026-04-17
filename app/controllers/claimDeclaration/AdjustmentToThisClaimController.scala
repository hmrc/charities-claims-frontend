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

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.AdjustmentToThisClaimFormProvider
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClaimsService, SaveService, UnregulatedDonationsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.AdjustmentToThisClaimView

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentToThisClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentToThisClaimView,
  actions: Actions,
  formProvider: AdjustmentToThisClaimFormProvider,
  saveService: SaveService,
  claimsService: ClaimsService,
  unregulatedDonationsService: UnregulatedDonationsService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions
      .authAndRefreshDataWithGuard(SessionData.isClaimDetailsComplete)
      .async { implicit request =>
        // derive if there is previous overpayment, then text input is mandatory
        val form: Form[Option[String]] = formProvider(
          "adjustmentToThisClaim.error.required",
          (350, "adjustmentToThisClaim.error.length"),
          "adjustmentToThisClaim.error.regex",
          sessionData.adjustmentForOtherIncomePreviousOverClaimed
            .exists(_ > BigDecimal(0.0)) || sessionData.prevOverclaimedGiftAid.exists(_ > BigDecimal(0.0))
        )
        given HeaderCarrier            = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        if RepaymentClaimDetailsAnswers.getClaimingGiftAid.contains(true) then
          if request.sessionData.unregulatedWarningBypassed then
            // user just saw WRN5 and chose to continue — consume the bypass flag and show the form
            val updatedSession = request.sessionData.copy(unregulatedWarningBypassed = false)
            val previousAnswer = request.sessionData.includedAnyAdjustmentsInClaimPrompt
            saveService.save(updatedSession).map { _ =>
              Ok(view(form.withDefault(Some(previousAnswer))))
            }
          else
            unregulatedDonationsService.checkUnregulatedLimit.flatMap {
              case Some(_) =>
                val updatedSession = request.sessionData.copy(unregulatedLimitExceeded = true)
                saveService.save(updatedSession).map { _ =>
                  Redirect(controllers.routes.RegisterCharityWithARegulatorController.onPageLoad)
                }

              case None =>
                val previousAnswer = request.sessionData.includedAnyAdjustmentsInClaimPrompt
                Future.successful(Ok(view(form.withDefault(Some(previousAnswer)))))
            }
        else {
          val previousAnswer = request.sessionData.includedAnyAdjustmentsInClaimPrompt
          Future.successful(Ok(view(form.withDefault(Some(previousAnswer)))))
        }
      }

  def onSubmit: Action[AnyContent] =
    actions
      .authAndRefreshDataWithGuard(SessionData.isClaimDetailsComplete)
      .async { implicit request =>
        val form: Form[Option[String]] = formProvider(
          "adjustmentToThisClaim.error.required",
          (350, "adjustmentToThisClaim.error.length"),
          "adjustmentToThisClaim.error.regex",
          sessionData.adjustmentForOtherIncomePreviousOverClaimed
            .exists(_ > BigDecimal(0.0)) || sessionData.prevOverclaimedGiftAid.exists(_ > BigDecimal(0.0))
        )
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
            value =>
              for {
                _ <- saveService
                       .save(
                         request.sessionData.copy(includedAnyAdjustmentsInClaimPrompt = value)
                       )
                _ <- claimsService.save
              } yield Redirect(
                routes.ClaimDeclarationController.onPageLoad
              )
          )
      }
}
