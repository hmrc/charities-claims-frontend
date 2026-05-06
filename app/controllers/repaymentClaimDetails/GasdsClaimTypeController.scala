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
import forms.{GasdsClaimTypeFormProvider, YesNoFormProvider}
import models.Mode.*
import models.SessionData.isCASCCharityReference
import models.requests.DataRequest
import models.{GasdsClaimType, Mode, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SaveService
import views.html.{GasdsClaimTypeView, UpdateRepaymentClaimView}

import scala.concurrent.{ExecutionContext, Future}

class GasdsClaimTypeController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: GasdsClaimTypeView,
  updateRepaymentClaimView: UpdateRepaymentClaimView,
  actions: Actions,
  guard: GuardAction,
  formProvider: GasdsClaimTypeFormProvider,
  yesNoFormProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[GasdsClaimType]       = formProvider()
  val confirmUpdateForm: Form[Boolean] = yesNoFormProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(
        guard(
          SessionData.isClaimNotSubmitted &&
            RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
        )
      )
      .async { implicit request =>
        given sessionData: SessionData = request.sessionData
        val previousAnswer             = RepaymentClaimDetailsAnswers.getGasdsClaimType

        val cleanedPrevious = previousAnswer.map { ans =>
          if (isCASCCharityReference) ans.copy(communityBuildings = false)
          else ans
        }

        val preparedForm = cleanedPrevious.fold(form)(form.fill)

        Future.successful(Ok(view(preparedForm, mode, isCASCCharityReference, request.isAgent)))
      }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(
        guard(
          SessionData.isClaimNotSubmitted &&
            RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true)
        )
      )
      .async { implicit request =>
        if (isConfirmingUpdate) {
          handleUpdateConfirmationSubmit(mode)
        } else {
          handleQuestionSubmit(mode)
        }
      }

  def handleQuestionSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val previousAnswer: Option[GasdsClaimType] = RepaymentClaimDetailsAnswers.getGasdsClaimType

    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, isCASCCharityReference, request.isAgent))),
        gasdsClaimType =>
          if (needsUpdateConfirmation(mode, previousAnswer, gasdsClaimType)) {
            val checkboxValues = formProvider.toSet(gasdsClaimType).toSeq
            Future.successful(
              Ok(
                updateRepaymentClaimView(
                  confirmUpdateForm,
                  routes.GasdsClaimTypeController.onSubmit(mode),
                  checkboxValues
                )
              )
            )
          } else {
            saveAndRedirect(gasdsClaimType, previousAnswer, mode)
          }
      )
  }

  def handleUpdateConfirmationSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val checkboxValues = extractSelectedClaimTypes

    confirmUpdateForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              updateRepaymentClaimView(
                formWithErrors,
                routes.GasdsClaimTypeController.onSubmit(mode),
                checkboxValues
              )
            )
          ),
        {
          case true =>
            val boundGasdsForm = form.bindFromRequest()

            boundGasdsForm.fold(
              _ =>
                Future.successful(
                  Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad)
                ),
              gasdsClaimType => {
                val previousAnswer = RepaymentClaimDetailsAnswers.getGasdsClaimType
                saveAndRedirect(gasdsClaimType, previousAnswer, mode)
              }
            )

          case false =>
            Future.successful(
              Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad)
            )
        }
      )
  }

  private def saveAndRedirect(
    value: GasdsClaimType,
    previous: Option[GasdsClaimType],
    mode: Mode
  )(implicit request: DataRequest[AnyContent]): Future[Result] =
    saveService
      .save(RepaymentClaimDetailsAnswers.setGasdsClaimType(value, previous))
      .map(_ => Redirect(GasdsClaimTypeController.nextPage(value, previous, mode)))

  private def extractSelectedClaimTypes(implicit request: DataRequest[AnyContent]): Seq[String] =
    request.body.asFormUrlEncoded
      .flatMap(_.get("value[]"))
      .getOrElse(Seq.empty)

  private def removed(prev: Boolean, next: Boolean): Boolean =
    prev && !next

  private def needsUpdateConfirmation(
    mode: Mode,
    previousAnswer: Option[GasdsClaimType],
    newAnswer: GasdsClaimType
  ): Boolean =
    mode match {
      case CheckMode =>
        previousAnswer.exists { prev =>
          removed(prev.topUp, newAnswer.topUp) ||
          removed(prev.communityBuildings, newAnswer.communityBuildings) ||
          removed(prev.connectedCharity, newAnswer.connectedCharity)
        }

      case _ => false
    }
}

object GasdsClaimTypeController {

  private def nextPage(
    claimType: GasdsClaimType,
    prevAnswer: Option[GasdsClaimType],
    mode: Mode
  ) = {

    def hasTopUpOrCommunity(claimType: GasdsClaimType): Boolean =
      claimType.topUp || claimType.communityBuildings

    def wasConnectedOnly(prevClaimType: GasdsClaimType): Boolean =
      prevClaimType.connectedCharity && !prevClaimType.topUp && !prevClaimType.communityBuildings

    mode match {

      case NormalMode =>
        if (hasTopUpOrCommunity(claimType))
          routes.ChangePreviousGASDSClaimController.onPageLoad(NormalMode)
        else
          routes.ClaimingReferenceNumberController.onPageLoad(NormalMode)

      case CheckMode =>
        prevAnswer match {

          case None =>
            if (hasTopUpOrCommunity(claimType))
              routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)
            else
              routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad

          case Some(prev) if wasConnectedOnly(prev) && hasTopUpOrCommunity(claimType) =>
            routes.ChangePreviousGASDSClaimController.onPageLoad(CheckMode)

          case _ =>
            routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
        }
    }
  }
}
