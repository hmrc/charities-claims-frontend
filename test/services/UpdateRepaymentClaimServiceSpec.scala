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

import models.Mode.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class UpdateRepaymentClaimServiceSpec extends AnyFreeSpec with Matchers {

  private val service = new UpdateRepaymentClaimService()

  "needsUpdateConfirmation" - {

    "should return true when in CheckMode, previous answer was Yes, and new answer is No" in {
      service.needsUpdateConfirmation(CheckMode, Some(true), false) shouldBe true
    }

    "should return false when in NormalMode" in {
      service.needsUpdateConfirmation(NormalMode, Some(true), false) shouldBe false
    }

    "should return false when answer is unchanged or not Yes to No" in {
      service.needsUpdateConfirmation(CheckMode, Some(false), false) shouldBe false
      service.needsUpdateConfirmation(CheckMode, Some(true), true)   shouldBe false
      service.needsUpdateConfirmation(CheckMode, None, false)        shouldBe false
    }
  }

  "isConfirmationSubmission" - {

    "should return true when confirmingUpdate field is present with value 'true'" in {
      val formData = Some(Map("confirmingUpdate" -> Seq("true")))
      service.isConfirmationSubmission(formData) shouldBe true
    }

    "should return false when confirmingUpdate is not present or has wrong value" in {
      service.isConfirmationSubmission(Some(Map("value" -> Seq("true"))))             shouldBe false
      service.isConfirmationSubmission(Some(Map("confirmingUpdate" -> Seq("false")))) shouldBe false
      service.isConfirmationSubmission(None)                                          shouldBe false
    }
  }
}
