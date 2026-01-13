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

package controllers.organisationDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import connectors.ClaimsValidationConnector
import forms.YesNoFormProvider
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.DeleteConnectedCharitiesScheduleView

import scala.concurrent.{ExecutionContext, Future}

class DeleteConnectedCharitiesScheduleController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: DeleteConnectedCharitiesScheduleView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  claimsValidationConnector: ClaimsValidationConnector
)(using ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider("deleteConnectedCharitiesSchedule.error.required")

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    Ok(view(form))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          if value then {
            request.sessionData.unsubmittedClaimId match {
              case Some(claimId) =>
                claimsValidationConnector
                  .getUploadSummary(claimId)
                  .flatMap { summaryResponse =>
                    summaryResponse.uploads.find(_.validationType == "ConnectedCharities") match {
                      case Some(connectedCharitiesUpload) =>
                        claimsValidationConnector
                          .deleteSchedule(claimId, connectedCharitiesUpload.reference)
                          .map { _ =>
                            // TODO: This redirects to placeholder R2 screen - route to be updated in the future
                            Redirect(
                              controllers.organisationDetails.routes.MakeCharityRepaymentClaimController.onPageLoad
                            )
                          }

                      case None =>
                        Future.failed(
                          new RuntimeException(
                            s"No ConnectedCharities schedule upload found for claimId: $claimId"
                          )
                        )
                    }
                  }

              case None =>
                Future.failed(
                  new RuntimeException(
                    "No unsubmittedClaimId found in session when attempting to delete ConnectedCharities schedule"
                  )
                )
            }
          } else {
            // no deletion, redirect to Add Schedule screen G2
            // TODO: This redirects to placeholder G2 screen - route to be updated in the future
            Future.successful(Redirect(controllers.organisationDetails.routes.AddScheduleController.onPageLoad))
          }
      )
  }
}
