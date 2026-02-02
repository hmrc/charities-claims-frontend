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

enum TaskStatus {
  case Completed, Incomplete, NotStarted, CannotStartYet
}

final case class TaskItem(
  name: String,
  href: Call,
  status: TaskStatus
) {
  def statusTag(using messages: Messages): Tag = status match {
    case TaskStatus.Completed      => Tag(content = Text(messages("claimsTaskList.status.completed")), classes = "govuk-tag")
    case TaskStatus.Incomplete     =>
      Tag(content = Text(messages("claimsTaskList.status.incomplete")), classes = "govuk-tag govuk-tag--blue")
    case TaskStatus.NotStarted     =>
      Tag(content = Text(messages("claimsTaskList.status.notStarted")), classes = "govuk-tag govuk-tag--grey")
    case TaskStatus.CannotStartYet =>
      Tag(content = Text(messages("claimsTaskList.status.cannotStartYet")), classes = "govuk-tag govuk-tag--grey")
  }

  def isCompleted: Boolean = status == TaskStatus.Completed
}

final case class TaskSection(
  headingKey: String,
  tasks: Seq[TaskItem],
  hintKey: Option[String] = None
)

final case class ClaimsTaskListViewModel(
  sections: Seq[TaskSection],
  deleteClaimUrl: Option[String]
)
