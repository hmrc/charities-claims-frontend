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

// result returned when limit is exceeded - contains limit value and formatted string for display
case class UnregulatedLimitExceeded(
  limit: Int,
  formattedLimit: String
)

@ImplementedBy(classOf[UnregulatedDonationsServiceImpl])
trait UnregulatedDonationsService {

  // F9 - checks if user should see WRN5 screen
  // returns Some(UnregulatedLimitExceeded) if over limit, None otherwise
  def checkUnregulatedLimit(using DataRequest[?], HeaderCarrier): Future[Option[UnregulatedLimitExceeded]]

  // returns formatted limit string for WRN5 display (example: "5,000" or "100,000")
  // returns None if charity is not LowIncome or Excepted
  def getApplicableLimit(using DataRequest[?]): Option[String]

  // F11 - records unregulated donation in FormP
  // TODO: implementation details to be confirmed
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

  // F9 - unregulated limit check
  def checkUnregulatedLimit(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Option[UnregulatedLimitExceeded]] = {

    val reasonNotRegistered: Option[ReasonNotRegisteredWithRegulator] =
      OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator(using request.sessionData)

    reasonNotRegistered match {
      case Some(ReasonNotRegisteredWithRegulator.LowIncome) =>
        performLimitCheck(appConfig.lowIncomeLimit)

      case Some(ReasonNotRegisteredWithRegulator.Excepted) =>
        performLimitCheck(appConfig.exceptedLimit)

      // Exempt, Waiting, or None - no limit check required proceed to D3
      case _                                               =>
        Future.successful(None)
    }
  }

  // performs limit check: (current claim total + existing unregulated donations) > limit
  private def performLimitCheck(
    limit: Int
  )(using request: DataRequest[?], hc: HeaderCarrier): Future[Option[UnregulatedLimitExceeded]] = {

    val charityReference = request.sessionData.charitiesReference

    for {
      existingUnregulatedDonationsOpt <- unregulatedDonationsConnector.getTotalUnregulatedDonations(charityReference)
      currentClaimTotal                = getCurrentClaimDonationsTotal
    } yield {
      val existingUnregulatedDonations = existingUnregulatedDonationsOpt.getOrElse(BigDecimal(0))
      val totalDonations               = currentClaimTotal + existingUnregulatedDonations

      if (totalDonations > limit) {
        Some(UnregulatedLimitExceeded(limit, currencyFormatter.format(limit)))
      } else {
        None
      }
    }
  }

  // sums schedule totals from sessionData (giftAid, otherIncome, communityBuildings - connected charities schedule has no total donations)
  // pass in sessionData - make the function pure - use companion objects
  // update unit test for all sub functions
  private def getCurrentClaimDonationsTotal(using request: DataRequest[?]): BigDecimal = {
    val session = request.sessionData

    val giftAidTotal            = session.giftAidScheduleData.flatMap(_.totalDonations).getOrElse(BigDecimal(0))
    val otherIncomeTotal        = session.otherIncomeScheduleData.map(_.totalOfGrossPayments).getOrElse(BigDecimal(0))
    val communityBuildingsTotal =
      session.communityBuildingsScheduleData.map(_.totalOfAllAmounts).getOrElse(BigDecimal(0))

    giftAidTotal + otherIncomeTotal + communityBuildingsTotal
  }

  // returns formatted limit for WRN5 display based on charity type
  def getApplicableLimit(using request: DataRequest[?]): Option[String] = {
    val reasonNotRegistered = OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator(using request.sessionData)

    reasonNotRegistered match {
      case Some(ReasonNotRegisteredWithRegulator.LowIncome) =>
        Some(currencyFormatter.format(appConfig.lowIncomeLimit))

      case Some(ReasonNotRegisteredWithRegulator.Excepted) =>
        Some(currencyFormatter.format(appConfig.exceptedLimit))

      case _ =>
        None
    }
  }

  // F11 - record unregulated donation
  // TODO: implementation TBC
  def recordUnregulatedDonation(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    Future.successful(())
}
