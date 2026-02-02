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

  /** checks if WRN3 confirmation is needed when submitting an answer.
    *
    * WRN3 confirmation is required when:
    *   - User is in CheckMode (going back to change an answer)
    *   - Previous answer was Yes (true)
    *   - New answer is No (false)
    *
    * To prevent accidental data loss when changing from Yes to No.
    *
    * @param mode
    *   Current navigation mode (NormalMode or CheckMode)
    * @param previousAnswer
    *   The user's previous answer (if any)
    * @param newAnswer
    *   The user's new answer
    * @return
    *   true if WRN3 confirmation should be shown
    */
  def needsUpdateConfirmation(
    mode: Mode,
    previousAnswer: Option[Boolean],
    newAnswer: Boolean
  ): Boolean =
    (mode, previousAnswer, newAnswer) match {
      case (CheckMode, Some(true), false) => true // Yes → No in CheckMode
      case _                              => false
    }

  /** Checks if the current request is a WRN3 confirmation check.
    *
    * This is determined by the presence of the hidden field "confirmingUpdate=true" in the form data.
    *
    * @param formData
    *   The form data from the request
    * @return
    *   true if this is a WRN3 confirmation submission
    */
  def isConfirmationSubmission(formData: Option[Map[String, Seq[String]]]): Boolean =
    formData
      .flatMap(_.get("confirmingUpdate"))
      .exists(_.headOption.contains("true"))
}
