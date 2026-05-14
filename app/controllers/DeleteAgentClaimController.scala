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

package controllers

import services.SaveService
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import com.google.inject.Singleton
import connectors.ClaimsConnector
import controllers.BaseController
import config.FrontendAppConfig
import views.html.DeleteAgentClaimView
import controllers.actions.{AccessType, Actions, GuardAction}
import play.api.Logging
import forms.YesNoFormProvider
import models.{RepaymentClaimDetailsAnswers, SessionData}
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteAgentClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: DeleteAgentClaimView,
  actions: Actions,
  guard: GuardAction,
  formProvider: YesNoFormProvider,
  claimsConnector: ClaimsConnector,
  saveService: SaveService,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends BaseController
    with Logging {

  val form: Form[Boolean] = formProvider("deleteAgentClaim.error.required")

  val onPageLoad: Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(
        guard(
          predicate = SessionData.isClaimNotSubmitted,
          access = AccessType.AgentOnly
        )
      )
      .async { implicit request =>
        val nextUrl =
          if request.getQueryString("claimId").isDefined
          then appConfig.charityRepaymentDashboardUrl
          else routes.ClaimsTaskListController.onPageLoad.url

        request.sessionData.unsubmittedClaimId match {
          case Some(claimId) =>
            val charityName =
              RepaymentClaimDetailsAnswers
                .getNameOfCharity(using request.sessionData)
                .getOrElse("")

            Future.successful(Ok(view(form, claimId, charityName)))

          case None =>
            Future.successful(Redirect(nextUrl))
        }
      }

  val onSubmit: Action[AnyContent] =
    actions
      .authAndGetData()
      .andThen(
        guard(
          predicate = SessionData.isClaimNotSubmitted,
          access = AccessType.AgentOnly
        )
      )
      .async { implicit request =>
        val nextUrl =
          if request.getQueryString("claimId").isDefined
          then appConfig.charityRepaymentDashboardUrl
          else routes.ClaimsTaskListController.onPageLoad.url

        val formData = form.bindFromRequest()

        formData.fold(
          formWithErrors =>
            request.sessionData.unsubmittedClaimId match {
              case Some(claimId) =>
                val charityName =
                  RepaymentClaimDetailsAnswers
                    .getNameOfCharity(using request.sessionData)
                    .getOrElse("")

                Future.successful(BadRequest(view(formWithErrors, claimId, charityName)))

              case None =>
                Future.successful(Redirect(nextUrl))
            },
          {
            case false =>
              Future.successful(Redirect(nextUrl))

            case true =>
              request.sessionData.unsubmittedClaimId match {
                case Some(claimId) =>
                  claimsConnector.deleteClaim(claimId).flatMap {
                    case true =>
                      saveService
                        .save(SessionData(charitiesReference = request.sessionData.charitiesReference))
                        .map(_ => Redirect(appConfig.charityRepaymentDashboardUrl))

                    case false =>
                      Future.failed(new RuntimeException("Failed to delete claim"))
                  }

                case None =>
                  Future.failed(
                    new RuntimeException(
                      "No claimId found to delete agent repayment claim"
                    )
                  )
              }
          }
        )
      }
}
