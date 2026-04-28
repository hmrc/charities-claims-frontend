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

package viewmodels

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.TaskListItemStatus

enum TaskStatus {
  case Completed, Incomplete, NotStarted, CannotStartYet, InProgress

  def toTaskListStatus(using messages: Messages): TaskListItemStatus =
    this match
      case Completed =>
        TaskListItemStatus(content = Text(messages("claimsTaskList.status.completed")))

      case Incomplete =>
        TaskListItemStatus(tag = Some(tag("claimsTaskList.status.incomplete", "govuk-tag--blue")))

      case NotStarted =>
        TaskListItemStatus(tag = Some(tag("claimsTaskList.status.notStarted", "govuk-tag--blue")))

      case InProgress =>
        TaskListItemStatus(tag = Some(tag("claimsTaskList.status.inProgress", "govuk-tag--green")))

      case CannotStartYet =>
        TaskListItemStatus(
          content = Text(messages("claimsTaskList.status.cannotStartYet")),
          classes = "govuk-task-list__status govuk-task-list__status--cannot-start-yet"
        )

  private def tag(messageKey: String, colour: String)(using messages: Messages): Tag =
    Tag(
      content = Text(messages(messageKey)),
      classes = s"govuk-tag $colour"
    )
}

final case class TaskItem(
  name: String,
  href: Call,
  status: TaskStatus
) {
  def isCompleted: Boolean = status == TaskStatus.Completed
}

final case class TaskSection(
  headingKey: String,
  tasks: Seq[TaskItem],
  hintKey: Option[String] = None
)

final case class ClaimsTaskListViewModel(
  sections: Seq[TaskSection],
  deleteClaimUrl: Option[String],
  charitiesReference: String,
  dashboardUrl: String
)
