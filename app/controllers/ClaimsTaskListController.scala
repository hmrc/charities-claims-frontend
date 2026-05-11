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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.Actions
import models.SessionData
import models.SessionData.isCASCCharityReference
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import viewmodels.{ClaimsTaskListViewModel, TaskItem, TaskSection, TaskStatus}
import views.html.ClaimsTaskListView
import connectors.ClaimsConnector
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class ClaimsTaskListController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimsTaskListView,
  actions: Actions,
  appConfig: FrontendAppConfig,
  claimsConnector: ClaimsConnector
)(using ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions.authAndRefreshData().async { implicit request =>
      request.sessionData.unsubmittedClaimId match {
        case Some(claimId) =>
          claimsConnector
            .updateLastVisitedAt(claimId)
            .map { _ =>
              Ok(view(ClaimsTaskListController.buildViewModel(appConfig.charityRepaymentDashboardUrl)))
            }

        case None =>
          Future.successful(
            Ok(view(ClaimsTaskListController.buildViewModel(appConfig.charityRepaymentDashboardUrl)))
          )
      }
    }
}

object ClaimsTaskListController {

  private def buildViewModel(
    dashboardUrl: String
  )(using request: DataRequest[?], messages: Messages): ClaimsTaskListViewModel = {
    val repaymentClaimDetailsComplete = isRepaymentClaimDetailsComplete
    val charitiesReference            = request.sessionData.charitiesReference

    if (!repaymentClaimDetailsComplete) {
      val repaymentClaimDetailsOnlySection = TaskSection(
        headingKey = "claimsTaskList.section.aboutTheClaim",
        tasks = Seq(buildRepaymentClaimDetailsTask)
      )
      val declarationSection               = TaskSection(
        headingKey = "claimsTaskList.section.declaration",
        tasks = buildDeclarationSection(allSectionsComplete = false, declarationInProgress = false),
        hintKey = Some("claimsTaskList.declaration.warning")
      )

      ClaimsTaskListViewModel(
        sections = Seq(repaymentClaimDetailsOnlySection, declarationSection),
        deleteClaimUrl = None,
        charitiesReference = charitiesReference,
        dashboardUrl = dashboardUrl
      )
    } else {
      val aboutTheClaimTasks   = buildAboutTheClaimSection
      val uploadDocumentsTasks = buildUploadDocumentsSection
      val allTasks             = aboutTheClaimTasks ++ uploadDocumentsTasks
      val allSectionsComplete  = allTasks.forall(_.status == TaskStatus.Completed)

      val aboutTheClaimSection = TaskSection(
        headingKey = "claimsTaskList.section.aboutTheClaim",
        tasks = aboutTheClaimTasks
      )

      val declarationSection = TaskSection(
        headingKey = "claimsTaskList.section.declaration",
        tasks = buildDeclarationSection(
          allSectionsComplete,
          request.sessionData.includedAnyAdjustmentsInClaimPrompt.isDefined
        ),
        hintKey = if (allSectionsComplete) None else Some("claimsTaskList.declaration.warning")
      )

      val sections = if (uploadDocumentsTasks.nonEmpty) {
        val uploadDocumentsSection = TaskSection(
          headingKey = "claimsTaskList.section.uploadDocuments",
          tasks = uploadDocumentsTasks
        )
        Seq(aboutTheClaimSection, uploadDocumentsSection, declarationSection)
      } else {
        Seq(aboutTheClaimSection, declarationSection)
      }

      ClaimsTaskListViewModel(
        sections = sections,
        deleteClaimUrl = Some(organisationDetails.routes.DeleteRepaymentClaimController.onPageLoad.url),
        charitiesReference = charitiesReference,
        dashboardUrl = dashboardUrl
      )
    }
  }

  private def isRepaymentClaimDetailsComplete(using request: DataRequest[?]): Boolean =
    SessionData.isRepaymentClaimDetailsComplete(using request.sessionData)

  private def buildAboutTheClaimSection(using request: DataRequest[?], messages: Messages): Seq[TaskItem] = {
    val answers         = request.sessionData.repaymentClaimDetailsAnswers
    val isClaimingGasds = answers.exists(a =>
      a.claimingDonationsNotFromCommunityBuilding.contains(true) ||
        a.makingAdjustmentToPreviousClaim.contains(true)
    )

    Seq(
      Some(buildRepaymentClaimDetailsTask),
      Some(
        if request.sessionData.isAgent
        then buildAgentUserOrganisationDetailsTask
        else buildOrganisationDetailsTask
      ),
      Option.when(isClaimingGasds)(buildGasdsDetailsTask)
    ).flatten
  }

  private def buildUploadDocumentsSection(using request: DataRequest[?], messages: Messages): Seq[TaskItem] = {
    val answers                      = request.sessionData.repaymentClaimDetailsAnswers
    val isClaimingGiftAid            = answers.exists(_.claimingGiftAid.contains(true))
    val isClaimingTaxDeducted        = answers.exists(_.claimingTaxDeducted.contains(true))
    val isClaimingCommunityBuildings = answers.exists(_.claimingDonationsCollectedInCommunityBuildings.contains(true))
    val isConnectedToOtherCharities  = answers.exists(_.connectedToAnyOtherCharities.contains(true))

    Seq(
      Option.when(isClaimingGiftAid)(buildGiftAidScheduleTask),
      Option.when(isClaimingTaxDeducted)(buildOtherIncomeScheduleTask),
      Option.when(isClaimingCommunityBuildings)(buildCommunityBuildingsScheduleTask),
      Option.when(isConnectedToOtherCharities)(buildConnectedCharitiesScheduleTask)
    ).flatten
  }

  private def buildDeclarationSection(allSectionsComplete: Boolean, declarationInProgress: Boolean)(using
    messages: Messages
  ): Seq[TaskItem] = {
    val status =
      (allSectionsComplete, declarationInProgress) match {
        case (false, _)    => TaskStatus.CannotStartYet
        case (true, false) => TaskStatus.NotStarted
        case (true, true)  => TaskStatus.InProgress
      }
    Seq(
      TaskItem(
        name = messages("claimsTaskList.task.readDeclaration"),
        href = claimDeclaration.routes.AdjustmentToThisClaimController.onPageLoad,
        status = status
      )
    )
  }

  private def buildRepaymentClaimDetailsTask(using request: DataRequest[?], messages: Messages): TaskItem = {
    val isComplete = SessionData.isRepaymentClaimDetailsComplete(using request.sessionData)
    val status     = if (isComplete) TaskStatus.Completed else TaskStatus.NotStarted
    val href       = if (isComplete) {
      repaymentClaimDetails.routes.RepaymentClaimDetailsCheckYourAnswersController.onPageLoad
    } else {
      repaymentClaimDetails.routes.RepaymentClaimDetailsController.onPageLoad
    }

    TaskItem(
      name = messages("claimsTaskList.task.repaymentClaimDetails"),
      href = href,
      status = status
    )
  }

  private def buildOrganisationDetailsTask(using request: DataRequest[?], messages: Messages): TaskItem = {
    val isCASCCharityRef: Boolean = isCASCCharityReference(using request.sessionData)
    val isComplete                = isOrgDetailsComplete(isCASCCharityRef)
    val status                    = if (isComplete) TaskStatus.Completed else TaskStatus.NotStarted
    val href                      = if (isComplete) {
      organisationDetails.routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
    } else {
      organisationDetails.routes.AboutTheOrganisationController.onPageLoad
    }

    TaskItem(
      name = messages("claimsTaskList.task.organisationDetails"),
      href = href,
      status = status
    )
  }

  private def isOrgDetailsComplete(isCASCCharityRef: Boolean)(using request: DataRequest[?]) =
    if (request.isAgent) {
      request.sessionData.agentUserOrganisationDetailsAnswers
        .exists(_.hasAgentDetailsCompleteAnswers(isCASCCharityRef))
    } else
      request.sessionData.organisationDetailsAnswers
        .exists(_.hasOrganisationDetailsCompleteAnswers(isCASCCharityRef))

  private def buildAgentUserOrganisationDetailsTask(using request: DataRequest[?], messages: Messages): TaskItem = {
    val isCASCCharityRef: Boolean = isCASCCharityReference(using request.sessionData)
    val isComplete                = request.sessionData.agentUserOrganisationDetailsAnswers
      .exists(_.hasAgentDetailsCompleteAnswers(isCASCCharityRef))
    val status                    = if (isComplete) TaskStatus.Completed else TaskStatus.NotStarted
    val href                      = if (isComplete) {
      organisationDetails.routes.OrganisationDetailsCheckYourAnswersController.onPageLoad
    } else {
      organisationDetails.routes.AboutTheOrganisationController.onPageLoad
    }

    TaskItem(
      name = messages("claimsTaskList.task.organisationDetails"),
      href = href,
      status = status
    )
  }

  private def buildGasdsDetailsTask(using request: DataRequest[?], messages: Messages): TaskItem = {
    val isComplete = request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
      .exists(_.hasGasdsDonationDetailsCompleteAnswers(request.sessionData.repaymentClaimDetailsAnswers))
    val status     = if (isComplete) TaskStatus.Completed else TaskStatus.NotStarted
    val href       = if (isComplete) {
      giftAidSmallDonationsScheme.routes.GiftAidSmallDonationsSchemeDetailsCheckYourAnswersController.onPageLoad
    } else {
      giftAidSmallDonationsScheme.routes.AboutGiftAidSmallDonationsSchemeController.onPageLoad
    }

    TaskItem(
      name = messages("claimsTaskList.task.gasdsDetails"),
      href = href,
      status = status
    )
  }

  private def buildGiftAidScheduleTask(using request: DataRequest[?], messages: Messages): TaskItem =
    val status =
      if request.sessionData.giftAidScheduleCompleted
      then TaskStatus.Completed
      else if request.sessionData.giftAidScheduleUpscanInitialization.isDefined || request.sessionData.giftAidScheduleFileUploadReference.isDefined
      then TaskStatus.InProgress
      else TaskStatus.NotStarted

    TaskItem(
      name = messages("claimsTaskList.task.giftAidSchedule"),
      href = controllers.giftAidSchedule.routes.AboutGiftAidScheduleController.onPageLoad,
      status = status
    )

  private def buildOtherIncomeScheduleTask(using request: DataRequest[?], messages: Messages): TaskItem =
    val status =
      if request.sessionData.otherIncomeScheduleCompleted
      then TaskStatus.Completed
      else if request.sessionData.otherIncomeScheduleUpscanInitialization.isDefined || request.sessionData.otherIncomeScheduleFileUploadReference.isDefined
      then TaskStatus.InProgress
      else TaskStatus.NotStarted

    TaskItem(
      name = messages("claimsTaskList.task.otherIncomeSchedule"),
      href = controllers.otherIncomeSchedule.routes.AboutOtherIncomeScheduleController.onPageLoad,
      status = status
    )

  private def buildCommunityBuildingsScheduleTask(using request: DataRequest[?], messages: Messages): TaskItem =
    val status =
      if request.sessionData.communityBuildingsScheduleCompleted
      then TaskStatus.Completed
      else if request.sessionData.communityBuildingsScheduleUpscanInitialization.isDefined || request.sessionData.communityBuildingsScheduleFileUploadReference.isDefined
      then TaskStatus.InProgress
      else TaskStatus.NotStarted

    TaskItem(
      name = messages("claimsTaskList.task.communityBuildingsSchedule"),
      href = controllers.communityBuildingsSchedule.routes.AboutCommunityBuildingsScheduleController.onPageLoad,
      status = status
    )

  private def buildConnectedCharitiesScheduleTask(using request: DataRequest[?], messages: Messages): TaskItem =
    val status =
      if request.sessionData.connectedCharitiesScheduleCompleted
      then TaskStatus.Completed
      else if request.sessionData.connectedCharitiesScheduleUpscanInitialization.isDefined || request.sessionData.connectedCharitiesScheduleFileUploadReference.isDefined
      then TaskStatus.InProgress
      else TaskStatus.NotStarted

    TaskItem(
      name = messages("claimsTaskList.task.connectedCharitiesSchedule"),
      href = connectedCharitiesSchedule.routes.AboutConnectedCharitiesScheduleController.onPageLoad,
      status = status
    )
}
