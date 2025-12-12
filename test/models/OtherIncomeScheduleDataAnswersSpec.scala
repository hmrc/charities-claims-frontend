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

import util.BaseSpec
import play.api.libs.json.Json

class OtherIncomeScheduleDataAnswersSpec extends BaseSpec {

  "OtherIncomeScheduleDataAnswers" - {
    "be serializable and deserializable" in {
      val otherIncomeScheduleDataAnswers = OtherIncomeScheduleDataAnswers(
        previouslyOverclaimedOtherIncome = Some(1000.00),
        totalGrossPayments = Some(1000.00),
        totalTaxDeducted = Some(1000.00),
        payments = Some(
          Seq(
            Payment(
              paymentItem = 1,
              nameOfPayer = "foobar",
              dateOfPayment = "2025-01-01",
              grossPayment = 1000.00,
              taxDeducted = 500.00
            )
          )
        )
      )

      val json = Json.toJson(otherIncomeScheduleDataAnswers)

      val deserializedOtherIncomeScheduleDataAnswers = json.as[OtherIncomeScheduleDataAnswers]
      deserializedOtherIncomeScheduleDataAnswers shouldBe otherIncomeScheduleDataAnswers
    }

  }

  "be created from OtherIncomeScheduleData" in {
    val otherIncomeScheduleData = OtherIncomeScheduleData(
      previouslyOverclaimedOtherIncome = 1000.00,
      totalGrossPayments = 1000.00,
      totalTaxDeducted = 1000.00,
      payments = Seq(
        Payment(
          paymentItem = 1,
          nameOfPayer = "foobar",
          dateOfPayment = "2025-01-01",
          grossPayment = 1000.00,
          taxDeducted = 500.00
        )
      )
    )

    val otherIncomeScheduleDataAnswers = OtherIncomeScheduleDataAnswers.from(otherIncomeScheduleData)

    otherIncomeScheduleDataAnswers shouldBe OtherIncomeScheduleDataAnswers(
      previouslyOverclaimedOtherIncome = Some(1000.00),
      totalGrossPayments = Some(1000.00),
      totalTaxDeducted = Some(1000.00),
      payments = Some(
        Seq(
          Payment(
            paymentItem = 1,
            nameOfPayer = "foobar",
            dateOfPayment = "2025-01-01",
            grossPayment = 1000.00,
            taxDeducted = 500.00
          )
        )
      )
    )
  }
}
