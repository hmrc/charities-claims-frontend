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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import connectors.UnregulatedDonationsConnector
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator}
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

// TODO: Add Unregulated Limit Check Result
// The limit that was exceeded (either lowIncomeLimit or exceptedLimit)
// The limit formatted for display ("5,000" or "100,000")
final case class UnregulatedLimitExceeded(
  limit: Int,
  formattedLimit: String
)

@ImplementedBy(classOf[UnregulatedDonationsServiceImpl])
trait UnregulatedDonationsService {

  // F9 - Unregulated Limit Check
  //
  // checks if the user should see WRN5 (Register charity with a regulator) screen.
  //
  // the check is only required when:
  // user is claiming Gift Aid (R1.0 = "Yes")
  // name of Charity Regulator (A2.1) = "None"
  // eason Charity is not regulated (A2.2) = "has low income" OR "charity is Excepted"
  //
  // If (current claim's total + sum of existing unregulated donations) > limit:
  // should return Some(UnregulatedLimitExceeded) with the applicable limit
  // WRN5 should be shown
  //
  // if under limit or check not required:
  // Returns None
  // user proceeds directly to D3 (Declaration Details Confirmation)
  //
  // @return Some(UnregulatedLimitExceeded) if WRN5 should be shown, None otherwise
  def checkUnregulatedLimit(using DataRequest[?], HeaderCarrier): Future[Option[UnregulatedLimitExceeded]]

  // F11 - Record Unregulated Donation
  //
  // TODO: implementation details to be confirmed
  // this function will record an unregulated donation in FormP

  def recordUnregulatedDonation(using DataRequest[?], HeaderCarrier): Future[Unit]
}

@Singleton
class UnregulatedDonationsServiceImpl @Inject() (
  unregulatedDonationsConnector: UnregulatedDonationsConnector,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends UnregulatedDonationsService {

  import java.text.DecimalFormat
  private val currencyFormatter = new DecimalFormat("#,###")

  // F9 - Unregulated Limit Check
  def checkUnregulatedLimit(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Option[UnregulatedLimitExceeded]] = {

    // check if the unregulated limit check is required
    // TODO: confirm if reasonNotRegisteredWithRegulator can be None at this point
    // if None, we should probably not show WRN5 (return None)
    val reasonNotRegistered: Option[ReasonNotRegisteredWithRegulator] =
      OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator(using request.sessionData)

    reasonNotRegistered match {
      case Some(ReasonNotRegisteredWithRegulator.LowIncome) =>
        performLimitCheck(appConfig.lowIncomeLimit)

      case Some(ReasonNotRegisteredWithRegulator.Excepted) =>
        performLimitCheck(appConfig.exceptedLimit)

      // Exempt, Waiting, or None - no limit check required, proceed to D3
      case _                                               =>
        Future.successful(None)
    }
  }

  // this will perform the actual limit check calculation
  //
  // The applicable limit (lowIncomeLimit or exceptedLimit)
  // return Some(UnregulatedLimitExceeded) if over limit, None if under
  private def performLimitCheck(
    limit: Int // double check Int is ok
  )(using request: DataRequest[?], hc: HeaderCarrier): Future[Option[UnregulatedLimitExceeded]] = {

    val charityReference = request.sessionData.charitiesReference

    for {
      // get the sum of all previously submitted unregulated donations from FormP
      existingUnregulatedDonationsOpt <- unregulatedDonationsConnector.getTotalUnregulatedDonations(charityReference)

      // get the total of donations for the current claim
      // TODO: confirm where this comes from - possibly from sessionData schedule uploads
      //       this is a sum of all the donations uploaded within the spreadsheet data.
      //       this sum is returned as part of the response for F2 - Get Unsubmitted Claims
      //       for now, using placeholder - need to confirm the correct source here
      currentClaimTotal = getCurrentClaimDonationsTotal

    } yield {
      val existingUnregulatedDonations = existingUnregulatedDonationsOpt.getOrElse(BigDecimal(0))

      // check if (current claim's total + sum of existing unregulated donations) > limit
      val totalDonations = currentClaimTotal + existingUnregulatedDonations

      if (totalDonations > limit) {
        Some(
          UnregulatedLimitExceeded(
            limit = limit,
            formattedLimit = currencyFormatter.format(limit)
          )
        )
      } else {
        None
      }
    }
  }

  // gets the total donations value from the current claim's schedule uploads
  //
  // TODO: confirm the correct source for this value
  private def getCurrentClaimDonationsTotal(using _request: DataRequest[?]): BigDecimal =
    // TODO: Implement - placeholder returning 0 for now

    BigDecimal(0)

  // Record Unregulated Donation (F11) TBC
  //
  // TODO: Full implementation details to be confirmed
  // This will call an endpoint on the backend to record the
  // unregulated donation when a claim is submitted on read declaration?
  def recordUnregulatedDonation(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    // TODO: Implement F11 - Record Unregulated Donation
    // This will need:
    // - endpoint on UnregulatedDonationsConnector to POST the donation record
    // - the donation amount from the current claim
    // - the charity reference
    Future.successful(())
}
