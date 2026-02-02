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

package viewmodels.govuk

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{
  TaskList,
  TaskListItem,
  TaskListItemStatus,
  TaskListItemTitle
}
import viewmodels.{TaskItem, TaskSection, TaskStatus}

object tasklist extends TaskListFluency

trait TaskListFluency {

  object TaskListViewModel {

    def apply(section: TaskSection)(using messages: Messages): TaskList =
      TaskList(items = section.tasks.map(task => TaskListItemViewModel(task, section.hintKey)))
  }

  object TaskListItemViewModel {

    def apply(task: TaskItem, hintKey: Option[String] = None)(using messages: Messages): TaskListItem =
      TaskListItem(
        title = TaskListItemTitle(content = Text(task.name)),
        hint = hintKey.map(key => Hint(content = Text(messages(key)))),
        status = TaskListItemStatus(tag = Some(task.statusTag)),
        href = if (task.status != TaskStatus.CannotStartYet) Some(task.href.url) else None
      )
  }

  implicit class FluentTaskList(taskList: TaskList) {

    def withCssClass(className: String): TaskList =
      taskList.copy(classes = s"${taskList.classes} $className")

    def withAttribute(attribute: (String, String)): TaskList =
      taskList.copy(attributes = taskList.attributes + attribute)
  }

  implicit class FluentTaskListItem(item: TaskListItem) {

    def withHint(hint: Hint): TaskListItem =
      item.copy(hint = Some(hint))

    def withHref(href: String): TaskListItem =
      item.copy(href = Some(href))
  }

}
