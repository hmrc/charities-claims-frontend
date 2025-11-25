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

import play.api.libs.json.{Format, Json}

final case class SessionData(
  // claimId of the unsubmitted claim stored in the backend,
  // if empty, the user has started a new claim
  unsubmittedClaimId: Option[String] = None,
  repaymentClaimDetailsAnswers: Option[RepaymentClaimDetailsAnswers] = None,
  organisationDetailsAnswers: Option[OrganisationDetailsAnswers] = None,
  giftAidScheduleDataAnswers: Option[GiftAidScheduleDataAnswers] = None,
  declarationDetailsAnswers: Option[DeclarationDetailsAnswers] = None,
  otherIncomeScheduleDataAnswers: Option[OtherIncomeScheduleDataAnswers] = None,
  gasdsScheduleDataAnswers: Option[GasdsScheduleDataAnswers] = None
)

object SessionData {

  given Format[SessionData] = Json.format[SessionData]

  def from(claim: Claim): SessionData =
    SessionData(
      unsubmittedClaimId = Some(claim.claimId),
      repaymentClaimDetailsAnswers = Some(
        RepaymentClaimDetailsAnswers.from(claim.claimData.repaymentClaimDetails)
      ),
      organisationDetailsAnswers = claim.claimData.organisationDetails.map(OrganisationDetailsAnswers.from),
      giftAidScheduleDataAnswers = claim.claimData.giftAidScheduleData.map(GiftAidScheduleDataAnswers.from),
      declarationDetailsAnswers = claim.claimData.declarationDetails.map(DeclarationDetailsAnswers.from),
      otherIncomeScheduleDataAnswers = claim.claimData.otherIncomeScheduleData.map(OtherIncomeScheduleDataAnswers.from),
      gasdsScheduleDataAnswers = claim.claimData.gasdsScheduleData.map(GasdsScheduleDataAnswers.from)
    )
}
