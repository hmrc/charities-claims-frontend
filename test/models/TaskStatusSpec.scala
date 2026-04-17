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

package models

import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import util.BaseSpec
import viewmodels.TaskStatus

class TaskStatusSpec extends BaseSpec {

  given messages: Messages = stubMessages()

  "TaskStatus.toTaskListStatus" - {
    "return completed content when status is Completed" in {
      val result = TaskStatus.Completed.toTaskListStatus

      result.content shouldBe Text(messages("claimsTaskList.status.completed"))
      result.tag     shouldBe None
    }

    "return blue tag when status is Incomplete" in {
      val result = TaskStatus.Incomplete.toTaskListStatus

      result.tag               shouldBe defined
      result.tag.value.classes   should include("govuk-tag--blue")
      result.tag.value.content shouldBe Text(messages("claimsTaskList.status.incomplete"))
    }

    "return blue tag when status is NotStarted" in {
      val result = TaskStatus.NotStarted.toTaskListStatus

      result.tag               shouldBe defined
      result.tag.value.classes   should include("govuk-tag--blue")
      result.tag.value.content shouldBe Text(messages("claimsTaskList.status.notStarted"))
    }

    "return as plain text when status is CannotStartYet" in {
      val result = TaskStatus.CannotStartYet.toTaskListStatus

      result.content shouldBe Text(messages("claimsTaskList.status.cannotStartYet"))
      result.tag     shouldBe None
    }

    "return green tag when status is InProgress" in {
      val result = TaskStatus.InProgress.toTaskListStatus

      result.tag               shouldBe defined
      result.tag.value.classes   should include("govuk-tag--green")
      result.tag.value.content shouldBe Text(messages("claimsTaskList.status.inProgress"))
    }
  }

}
