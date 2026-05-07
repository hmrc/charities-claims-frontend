/*
 * Copyright 2025 HM Revenue & Customs
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

import services.{ClaimsService, SaveService}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import com.google.inject.Inject
import connectors.ClaimsValidationConnector
import views.html.RepaymentClaimDetailsCheckYourAnswersView
import controllers.actions.{Actions, GuardAction}
import uk.gov.hmrc.http.HeaderCarrier
import models.SessionData
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class RepaymentClaimDetailsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  saveService: SaveService,
  guard: GuardAction,
  claimsService: ClaimsService,
  val controllerComponents: MessagesControllerComponents,
  claimsValidationConnector: ClaimsValidationConnector,
  view: RepaymentClaimDetailsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  def onPageLoad: Action[AnyContent] = actions
    .authAndGetData()
    .andThen(guard(SessionData.isClaimNotSubmitted))
    .async { implicit request =>
      val previousAnswers = request.sessionData.repaymentClaimDetailsAnswers
      Future.successful(Ok(view(previousAnswers, request.isAgent)))
    }

  def onSubmit: Action[AnyContent] = actions
    .authAndGetData()
    .andThen(guard(SessionData.isClaimNotSubmitted))
    .async { implicit request =>
      val checkAnswers =
        request.sessionData.repaymentClaimDetailsAnswers.exists(_.hasRepaymentClaimDetailsCompleteAnswers)
      if checkAnswers
      then
        val newSessionData = SessionData.syncUploadReferencesAndFlagsWithCheckboxes(using request.sessionData)
        for {
          _ <- saveService.save(newSessionData)
          _ <- claimsService.save
          _ <- removeAbandonedUploads(request.sessionData)
        } yield Redirect(controllers.routes.ClaimsTaskListController.onPageLoad)
      else Future.successful(Redirect(routes.RepaymentClaimDetailsIncompleteAnswersController.onPageLoad))
    }

  private def removeAbandonedUploads(
    sessionData: SessionData
  )(using headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    sessionData.unsubmittedClaimId match {
      case Some(claimId) =>
        Future
          .sequence(
            SessionData
              .getAbandonedUploads(using sessionData)
              .map(upload =>
                claimsValidationConnector
                  .deleteSchedule(claimId, upload)
                  .recover(_ => ()) // best effort to delete abandoned uploads
              )
          )
          .map(_ => ())
      case None          =>
        Future.successful(())
    }
}
