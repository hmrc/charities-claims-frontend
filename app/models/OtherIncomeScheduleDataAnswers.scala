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

import play.api.libs.json.Format
import play.api.libs.json.Json

final case class OtherIncomeScheduleDataAnswers(
  prevOverclaimedOtherIncome: Option[BigDecimal] = None,
  totalGrossPayments: Option[BigDecimal] = None,
  totalTaxDeducted: Option[BigDecimal] = None,
  payments: Option[Seq[Payment]] = None
)

object OtherIncomeScheduleDataAnswers {

  given Format[OtherIncomeScheduleDataAnswers] = Json.format[OtherIncomeScheduleDataAnswers]

  def from(otherIncomeScheduleData: OtherIncomeScheduleData): OtherIncomeScheduleDataAnswers =
    OtherIncomeScheduleDataAnswers(
      prevOverclaimedOtherIncome = Some(otherIncomeScheduleData.prevOverclaimedOtherIncome),
      totalGrossPayments = Some(otherIncomeScheduleData.totalGrossPayments),
      totalTaxDeducted = Some(otherIncomeScheduleData.totalTaxDeducted),
      payments = Some(otherIncomeScheduleData.payments)
    )
}
