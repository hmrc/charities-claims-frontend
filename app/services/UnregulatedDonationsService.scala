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
import models.{OrganisationDetailsAnswers, ReasonNotRegisteredWithRegulator, SessionData}
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import java.text.DecimalFormat

case class UnregulatedLimitExceeded(
  limit: Int,
  formattedLimit: String
)

@ImplementedBy(classOf[UnregulatedDonationsServiceImpl])
trait UnregulatedDonationsService {

  def checkUnregulatedLimit(using DataRequest[?], HeaderCarrier): Future[Option[UnregulatedLimitExceeded]]

  def getApplicableLimit(using DataRequest[?]): Option[String]

  // TODO: implementation of F11 def recordUnregulatedDonation
  //  (records unregulated donation in FormP) details to be confirmed
}

object UnregulatedDonationsService {

  private val currencyFormatter = new DecimalFormat("#,###")

  def getReasonNotRegistered(sessionData: SessionData): Option[ReasonNotRegisteredWithRegulator] =
    OrganisationDetailsAnswers.getReasonNotRegisteredWithRegulator(using sessionData)

  def getCurrentClaimDonationsTotal(sessionData: SessionData): BigDecimal = {
    val giftAidTotal            = sessionData.giftAidScheduleData.flatMap(_.totalDonations).getOrElse(BigDecimal(0))
    val otherIncomeTotal        = sessionData.otherIncomeScheduleData.map(_.totalOfGrossPayments).getOrElse(BigDecimal(0))
    val communityBuildingsTotal =
      sessionData.communityBuildingsScheduleData.flatMap(_.totalOfAllAmounts).getOrElse(BigDecimal(0))

    giftAidTotal + otherIncomeTotal + communityBuildingsTotal
  }

  def getLimitForReason(
    reason: ReasonNotRegisteredWithRegulator,
    lowIncomeLimit: Int,
    exceptedLimit: Int
  ): Option[Int] =
    reason match {
      case ReasonNotRegisteredWithRegulator.LowIncome => Some(lowIncomeLimit)
      case ReasonNotRegisteredWithRegulator.Excepted  => Some(exceptedLimit)
      case _                                          => None
    }

  def formatLimit(limit: Int): String =
    currencyFormatter.format(limit)

  def calculateTotalDonations(
    currentClaimTotal: BigDecimal,
    existingUnregulatedDonations: BigDecimal
  ): BigDecimal =
    currentClaimTotal + existingUnregulatedDonations

  def isOverLimit(totalDonations: BigDecimal, limit: Int): Boolean =
    totalDonations > limit

  def buildLimitExceededResult(limit: Int): UnregulatedLimitExceeded =
    UnregulatedLimitExceeded(limit, formatLimit(limit))

  def checkIfOverLimit(
    currentClaimTotal: BigDecimal,
    existingUnregulatedDonations: BigDecimal,
    limit: Int
  ): Option[UnregulatedLimitExceeded] = {
    val totalDonations = calculateTotalDonations(currentClaimTotal, existingUnregulatedDonations)

    if (isOverLimit(totalDonations, limit)) {
      Some(buildLimitExceededResult(limit))
    } else {
      None
    }
  }

  def getFormattedLimitForReason(
    reason: Option[ReasonNotRegisteredWithRegulator],
    lowIncomeLimit: Int,
    exceptedLimit: Int
  ): Option[String] =
    reason.flatMap(r => getLimitForReason(r, lowIncomeLimit, exceptedLimit).map(formatLimit))
}

@Singleton
class UnregulatedDonationsServiceImpl @Inject() (
  unregulatedDonationsConnector: UnregulatedDonationsConnector,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext)
    extends UnregulatedDonationsService {

  import UnregulatedDonationsService.*

  def checkUnregulatedLimit(using
    request: DataRequest[?],
    hc: HeaderCarrier
  ): Future[Option[UnregulatedLimitExceeded]] = {

    val sessionData         = request.sessionData
    val reasonNotRegistered = getReasonNotRegistered(sessionData)

    reasonNotRegistered match {
      case Some(reason) =>
        getLimitForReason(reason, appConfig.lowIncomeLimit, appConfig.exceptedLimit) match {
          case Some(limit) =>
            val charityReference = sessionData.charitiesReference
            unregulatedDonationsConnector.getTotalUnregulatedDonations(charityReference).map { existingDonationsOpt =>

              val existingDonations = existingDonationsOpt.getOrElse(BigDecimal(0))
              val currentClaimTotal = getCurrentClaimDonationsTotal(sessionData)

              checkIfOverLimit(currentClaimTotal, existingDonations, limit)
            }

          case None =>
            // Exempt or Waiting - no limit check required, proceed
            Future.successful(None)
        }

      // No reason set (charity has regulator) - no limit check required, proceed
      case None         =>
        Future.successful(None)
    }
  }

  def getApplicableLimit(using request: DataRequest[?]): Option[String] = {
    val sessionData = request.sessionData
    val reason      = getReasonNotRegistered(sessionData)

    getFormattedLimitForReason(reason, appConfig.lowIncomeLimit, appConfig.exceptedLimit)
  }

  // TODO: Implementation of F11 - def recordUnregulatedDonation - details to be confirmed
}
