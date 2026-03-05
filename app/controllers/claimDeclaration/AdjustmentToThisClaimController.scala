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
import connectors.ClaimsValidationConnector
import controllers.BaseController
import views.html.AdjustmentToThisClaimView
import controllers.actions.Actions
import forms.AdjustmentToThisClaimFormProvider
import models.{DeclarationDetailsAnswers, GetUploadResultValidatedGiftAid, GiftAidScheduleData, SessionData}
import play.api.data.Form
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AdjustmentToThisClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: AdjustmentToThisClaimView,
  actions: Actions,
  formProvider: AdjustmentToThisClaimFormProvider,
  saveService: SaveService,
  claimsValidationConnector: ClaimsValidationConnector,
  unregulatedDonationsService: UnregulatedDonationsService
)(using ec: ExecutionContext)
    extends BaseController {

  val overPaymentFlag = true  //TODO derive if giftAdd previous overpayment, OtherIncome overpayment

  val form: Form[Option[String]] = formProvider(
    "adjustmentToThisClaim.error.required",
    (350, "adjustmentToThisClaim.error.length"),
    "adjustmentToThisClaim.error.regex",
    overPaymentFlag
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
                  DeclarationDetailsAnswers.setIncludedAnyAdjustmentsInClaimPrompt(value)
                )
                .map(_ =>
                  Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
                ) // TODO - redirect when next page available
          )
      }

  def getGiftAidTotalDonations(data: GiftAidScheduleData): BigDecimal =
    data.prevOverclaimedGiftAid.getOrElse(BigDecimal(0))

  def fetchGiftAidOverPayment(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[BigDecimal] = {
    val sessionData = request.sessionData
    val claimIdOpt  = sessionData.unsubmittedClaimId
    val fileRefOpt  = sessionData.giftAidScheduleFileUploadReference

    (claimIdOpt, fileRefOpt) match {
      case (Some(claimId), Some(fileRef)) =>
        claimsValidationConnector
          .getUploadResult(claimId, fileRef)
          .map {
            case GetUploadResultValidatedGiftAid(_, data) =>
              getGiftAidTotalDonations(data)
            case _                                        =>
              BigDecimal(0)
          }
          .recover { case _ => BigDecimal(0) }

      case _ =>
        Future.successful(BigDecimal(0))
    }
  }
}
