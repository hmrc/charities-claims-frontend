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

import models.Mode.*
import services.SaveService
import connectors.ClaimsConnector
import play.api.mvc.*
import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.AccessType.AgentOnly
import views.html.CharitiesReferenceNumberInputView
import controllers.actions.{Actions, GuardAction}
import forms.CharitiesReferenceTextInputFormProvider
import models.{Mode, RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}
import forms.YesNoFormProvider
import models.requests.DataRequest
import views.html.UpdateRepaymentClaimView

class CharitiesReferenceNumberInputController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: CharitiesReferenceNumberInputView,
  updateRepaymentClaimView: UpdateRepaymentClaimView,
  actions: Actions,
  guard: GuardAction,
  formProvider: CharitiesReferenceTextInputFormProvider,
  yesNoFormProvider: YesNoFormProvider,
  claimsConnector: ClaimsConnector,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[String] = formProvider(
    "charitiesReferenceNumber.error.required",
    (7, "charitiesReferenceNumber.error.length"),
    "charitiesReferenceNumber.error.regex"
  )

  val confirmUpdateForm: Form[Boolean] = yesNoFormProvider("updateRepaymentClaim.error.required")

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(guard(predicate = SessionData.isClaimNotSubmitted, access = AgentOnly))
      .async { implicit request =>
        val previousAnswer = RepaymentClaimDetailsAnswers.getHmrcCharitiesReference match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(view(previousAnswer, mode)))
      }

  def onSubmit(mode: Mode = NormalMode): Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(guard(predicate = SessionData.isClaimNotSubmitted, access = AgentOnly))
      .async { implicit request =>
        if (isConfirmingUpdate) {
          handleUpdateConfirmationSubmit(mode)
        } else {
          handleQuestionSubmit(mode)
        }
      }

  def handleQuestionSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    val previousAnswer: Option[String] = RepaymentClaimDetailsAnswers.getHmrcCharitiesReference
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          if (RepaymentClaimDetailsAnswers.getHmrcCharitiesReference.contains(value)) {
            Future.successful(Redirect(navigator(mode)))
          } else {
            claimsConnector.hasUnsubmittedClaim(value).flatMap {
              case true =>
                val formWithErrors = form
                  .fill(value)
                  .withError(s"value", "charitiesReferenceNumber.error.exists", value)
                Future.successful(BadRequest(view(formWithErrors, mode)))

              case false =>
                if needsUpdateConfirmation(mode, previousAnswer, value)
                then {
                  Future.successful(
                    Ok(
                      updateRepaymentClaimView(
                        confirmUpdateForm,
                        routes.CharitiesReferenceNumberInputController.onSubmit(mode),
                        Seq(value)
                      )
                    )
                  )
                } else {
                  saveService
                    .save(RepaymentClaimDetailsAnswers.setHmrcCharitiesReference(value))
                    .map(_ => Redirect(navigator(mode)))
                }
            }
          }
      )

  def handleUpdateConfirmationSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] =
    val value = extractAnswerValue.head

    confirmUpdateForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              updateRepaymentClaimView(
                formWithErrors,
                routes.CharitiesReferenceNumberInputController.onSubmit(mode),
                Seq(value)
              )
            )
          ),
        {
          case true =>
            saveService
              .save(RepaymentClaimDetailsAnswers.setHmrcCharitiesReference(value))
              .map(_ => Redirect(navigator(mode)))

          case false =>
            Future.successful(
              Redirect(routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad)
            )
        }
      )

  def navigator(mode: Mode): Call = mode match {
    case NormalMode =>
      routes.EnterCharityNameController.onPageLoad(mode)
    case CheckMode  =>
      routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
  }

  private def needsUpdateConfirmation(
    mode: Mode,
    previousAnswer: Option[String],
    newAnswer: String
  ): Boolean =
    mode match {
      case CheckMode =>
        previousAnswer.exists { prevAnswer =>
          !isCASCCharityReference(prevAnswer) && isCASCCharityReference(newAnswer)
          || (isCASCCharityReference(prevAnswer) && !isCASCCharityReference(newAnswer))
        }

      case _ => false
    }

  private def isCASCCharityReference(value: String): Boolean =
    value.startsWith("CH") || value.startsWith("CF")

  private def extractAnswerValue(implicit request: DataRequest[AnyContent]): Seq[String] =
    request.body.asFormUrlEncoded
      .flatMap(_.get("value[]"))
      .getOrElse(Seq.empty)
}
