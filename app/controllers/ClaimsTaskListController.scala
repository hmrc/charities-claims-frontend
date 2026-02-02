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
import controllers.actions.Actions
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import viewmodels.{ClaimsTaskListViewModel, TaskItem, TaskSection, TaskStatus}
import views.html.ClaimsTaskListView

class ClaimsTaskListController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ClaimsTaskListView,
  actions: Actions
) extends BaseController {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    Ok(view(ClaimsTaskListController.buildViewModel))
  }
}

object ClaimsTaskListController {

  def buildViewModel(using request: DataRequest[?], messages: Messages): ClaimsTaskListViewModel = {
    val repaymentClaimDetailsComplete = isRepaymentClaimDetailsComplete

    if (!repaymentClaimDetailsComplete) {
      val repaymentClaimDetailsOnlySection = TaskSection(
        headingKey = "claimsTaskList.section.aboutTheClaim",
        tasks = Seq(buildRepaymentClaimDetailsTask)
      )
      val declarationSection               = TaskSection(
        headingKey = "claimsTaskList.section.declaration",
        tasks = buildDeclarationSection(allSectionsComplete = false),
        hintKey = Some("claimsTaskList.declaration.warning")
      )

      ClaimsTaskListViewModel(
        sections = Seq(repaymentClaimDetailsOnlySection, declarationSection),
        deleteClaimUrl = None
      )
    } else {
      val aboutTheClaimSection = TaskSection(
        headingKey = "claimsTaskList.section.aboutTheClaim",
        tasks = buildAboutTheClaimSection
      )

      val uploadDocumentsSection = TaskSection(
        headingKey = "claimsTaskList.section.uploadDocuments",
        tasks = buildUploadDocumentsSection
      )

      val allSectionsComplete = areAllSectionsComplete
      val declarationSection  = TaskSection(
        headingKey = "claimsTaskList.section.declaration",
        tasks = buildDeclarationSection(allSectionsComplete),
        hintKey = if (allSectionsComplete) None else Some("claimsTaskList.declaration.warning")
      )

      val sections = Seq(
        aboutTheClaimSection,
        uploadDocumentsSection,
        declarationSection
      )

      ClaimsTaskListViewModel(
        sections = sections,
        deleteClaimUrl = Some(organisationDetails.routes.DeleteRepaymentClaimController.onPageLoad.url)
      )
    }
  }

  def isRepaymentClaimDetailsComplete(using request: DataRequest[?]): Boolean =
    request.sessionData.repaymentClaimDetailsAnswersOld.hasCompleteAnswers

  private def buildAboutTheClaimSection(using request: DataRequest[?], messages: Messages): Seq[TaskItem] =
    Seq(
      buildRepaymentClaimDetailsTask,
      buildOrganisationDetailsTask,
      buildGasdsDetailsTask
    )

  private def buildUploadDocumentsSection(using messages: Messages): Seq[TaskItem] =
    Seq(
      buildGiftAidScheduleTask,
      buildOtherIncomeScheduleTask,
      buildCommunityBuildingsScheduleTask,
      buildConnectedCharitiesScheduleTask
    )

  private def buildDeclarationSection(allSectionsComplete: Boolean)(using messages: Messages): Seq[TaskItem] = {
    val status = if (allSectionsComplete) TaskStatus.NotStarted else TaskStatus.CannotStartYet
    Seq(
      TaskItem(
        name = messages("claimsTaskList.task.readDeclaration"),
        href = routes.DeclarationDetailsConfirmationController.onPageLoad,
        status = status
      )
    )
  }

  private def areAllSectionsComplete(using request: DataRequest[?], messages: Messages): Boolean = {
    val aboutTheClaim = buildAboutTheClaimSection
    aboutTheClaim.forall(_.status == TaskStatus.Completed)
  }

  def buildRepaymentClaimDetailsTask(using request: DataRequest[?], messages: Messages): TaskItem = {
    val isComplete = request.sessionData.repaymentClaimDetailsAnswersOld.hasCompleteAnswers
    val status     = if (isComplete) TaskStatus.Completed else TaskStatus.Incomplete
    val href       = if (isComplete) {
      repaymentclaimdetailsold.routes.CheckYourAnswersController.onPageLoad
    } else {
      repaymentclaimdetailsold.routes.ClaimingGiftAidController.onPageLoad(models.Mode.NormalMode)
    }

    TaskItem(
      name = messages("claimsTaskList.task.repaymentClaimDetails"),
      href = href,
      status = status
    )
  }

  private def buildOrganisationDetailsTask(using request: DataRequest[?], messages: Messages): TaskItem = {
    val isComplete = request.sessionData.organisationDetailsAnswers
      .exists(_.hasOrganisationDetailsCompleteAnswers)
    val status     = if (isComplete) TaskStatus.Completed else TaskStatus.Incomplete
    val href       = if (isComplete) {
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

  private def buildGasdsDetailsTask(using messages: Messages): TaskItem =
    TaskItem(
      name = messages("claimsTaskList.task.gasdsDetails"),
      href = routes.PageNotFoundController.onPageLoad,
      status = TaskStatus.Completed
    )

  private def buildGiftAidScheduleTask(using messages: Messages): TaskItem =
    TaskItem(
      name = messages("claimsTaskList.task.giftAidSchedule"),
      href = routes.PageNotFoundController.onPageLoad,
      status = TaskStatus.Completed
    )

  private def buildOtherIncomeScheduleTask(using messages: Messages): TaskItem =
    TaskItem(
      name = messages("claimsTaskList.task.otherIncomeSchedule"),
      href = routes.PageNotFoundController.onPageLoad,
      status = TaskStatus.Completed
    )

  private def buildCommunityBuildingsScheduleTask(using messages: Messages): TaskItem =
    TaskItem(
      name = messages("claimsTaskList.task.communityBuildingsSchedule"),
      href = routes.PageNotFoundController.onPageLoad,
      status = TaskStatus.Completed
    )

  private def buildConnectedCharitiesScheduleTask(using messages: Messages): TaskItem =
    TaskItem(
      name = messages("claimsTaskList.task.connectedCharitiesSchedule"),
      href = routes.PageNotFoundController.onPageLoad,
      status = TaskStatus.Completed
    )
}
