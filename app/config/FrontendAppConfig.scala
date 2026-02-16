/*
 * Copyright 2024 HM Revenue & Customs
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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import scala.concurrent.duration.Duration

@Singleton
class FrontendAppConfig @Inject() (config: Configuration) { self =>

  lazy val appName: String      = config.get[String]("appName")
  lazy val mongoDbTTL: Duration = config.get[Duration]("mongodb.ttl")

  lazy val baseUrl: String                                                 = config.get[String]("urls.baseUrl")
  lazy val loginUrl: String                                                = config.get[String]("urls.login")
  lazy val loginContinueUrl: String                                        = config.get[String]("urls.loginContinue")
  lazy val signOutUrl: String                                              = config.get[String]("urls.signOut")
  lazy val homePageUrl: String                                             = config.get[String]("urls.homePageUrl")
  lazy val accessibilityStatementUrl: String                               = config.get[String]("urls.accessibilityStatementUrl")
  lazy val betaFeedbackUrl: String                                         = config.get[String]("urls.betaFeedbackUrl")
  lazy val researchUrl: String                                             = config.get[String]("urls.researchUrl")
  lazy val authLoginStubSignInUrl: String                                  = config.get[String]("urls.authLoginStubSignInUrl")
  lazy val charityRepaymentDashboardUrl: String                            = config.get[String]("urls.charityRepaymentDashboardUrl")
  lazy val accountUrl: String                                              = config.get[String]("urls.accountUrl")
  lazy val makeCharityRepaymentClaimUrl: String                            = config.get[String]("urls.makeCharityRepaymentClaimUrl")
  lazy val registerCharityWithARegulatorUrl: String                        = config.get[String]("urls.registerCharityWithARegulatorUrl")
  lazy val giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl: String =
    config.get[String]("urls.giftAidScheduleSpreadsheetsToClaimBackTaxOnDonationsUrl")
  lazy val otherIncomeScheduleSpreadsheetGuidanceUrl: String               =
    config.get[String]("urls.otherIncomeScheduleSpreadsheetGuidanceUrl")
  lazy val giftAidScheduleSpreadsheetGuidanceUrl: String                   =
    config.get[String]("urls.giftAidScheduleSpreadsheetGuidanceUrl")
  lazy val communityBuildingsScheduleSpreadsheetGuidanceUrl: String        =
    config.get[String]("urls.communityBuildingsScheduleSpreadsheetGuidanceUrl")

  lazy val enableLanguageSwitching: Boolean = config.get[Boolean]("enableLanguageSwitching")
  lazy val timeoutInSeconds: Int            = config.get[Int]("timeout-dialog.timeout")
  lazy val countdownInSeconds: Int          = config.get[Int]("timeout-dialog.countdown")
  lazy val agentUnsubmittedClaimLimit: Int  = config.get[Int]("agentUnsubmittedClaimLimit")
  lazy val exceptedLimit: Int               = config.get[Int]("unregulated-limits.exceptedLimit")
  lazy val lowIncomeLimit: Int              = config.get[Int]("unregulated-limits.lowIncomeLimit")

  lazy val maxGifAidScheduleUploadSize: Int      = config.get[Int]("scheduleUpload.gifAid.maxSize")
  lazy val maxOtherIncomeScheduleUploadSize: Int = config.get[Int]("scheduleUpload.otherIncome.maxSize")

  lazy val allowedFileTypesHint: String   = config.get[String]("scheduleUpload.allowedFileTypesHint")
  lazy val filePickerAcceptFilter: String = config.get[String]("scheduleUpload.filePickerAcceptFilter")

  def pageTitleWithServiceName(
    pageTitle: String,
    serviceName: String
  ): String = {
    val pageTitleNoHTML = pageTitle.replaceAll("\\<.*?\\>", "")
    s"$pageTitleNoHTML - $serviceName - GOV.UK"
  }

  def pageTitleWithServiceNameAndError(
    pageTitle: String,
    serviceName: String,
    errorPrefix: String,
    hasErrors: Boolean
  ): String =
    if hasErrors then s"$errorPrefix ${pageTitleWithServiceName(pageTitle, serviceName)}"
    else pageTitleWithServiceName(pageTitle, serviceName)
}
