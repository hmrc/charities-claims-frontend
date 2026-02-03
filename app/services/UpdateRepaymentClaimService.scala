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

package services

import models.Mode
import models.Mode.CheckMode

import javax.inject.{Inject, Singleton}

@Singleton
class UpdateRepaymentClaimService @Inject() () {

  // checks if WRN3 confirmation is needed when submitting an answer
  // WRN3 confirmation is required when:
  // - User is in CheckMode - going back to change an answer
  // - Previous answer was Yes (true)
  // - New answer is No (false)
  // - To prevent accidental data loss when changing from Yes to No
  def needsUpdateConfirmation(
    mode: Mode, // current mode we are in
    previousAnswer: Option[Boolean], // previous answer if any
    newAnswer: Boolean // new answer from user
  ): Boolean =
    (mode, previousAnswer, newAnswer) match {
      case (CheckMode, Some(true), false) => true // return true if in CheckMode and changing from Yes to No - needs WRN3
      case _                              => false
    }

  // checks if the current submission is a WRN3 confirmation submission
  // looks for hidden field "confirmingUpdate" with value "true"
  // returns true if found, false otherwise
  def isConfirmationSubmission(formData: Option[Map[String, Seq[String]]]): Boolean =
    formData
      .flatMap(_.get("confirmingUpdate"))
      .exists(_.headOption.contains("true"))
}
