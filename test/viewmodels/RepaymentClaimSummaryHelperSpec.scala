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

package viewmodels

import models.*
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class RepaymentClaimSummaryHelperSpec extends PlaySpec with GuiceOneAppPerSuite {

  implicit val messages: Messages = stubMessages()

  private val claimDetails = ClaimDetails(
    charityName = "Test Charity",
    hmrcCharityReference = "ABC123",
    submissionTimestamp = "2026-04-07T11:34:21.147Z",
    submittedBy = "John Doe"
  )

  private val giftAidDetails = GiftAidDetails(
    numberGiftAidDonations = 10,
    totalValueGiftAidDonations = BigDecimal(100)
  )

  private val otherIncomeDetails = OtherIncomeDetails(
    numberOtherIncomeItems = 5,
    totalValueOtherIncomeItems = BigDecimal(50)
  )

  private val gasdsDetails = GasdsDetails(
    totalValueGasdsNotInCommunityBuilding = Some(BigDecimal(25)),
    numberCommunityBuildings = Some(2),
    totalValueGasdsInCommunityBuilding = Some(BigDecimal(40)),
    numberConnectedCharities = Some(1)
  )

  private val adjustmentDetails = AdjustmentDetails(
    previouslyOverclaimedGiftAidOtherIncome = Some(BigDecimal(15)),
    previouslyOverclaimedGasds = Some(BigDecimal(10))
  )

  private val summary = SubmissionSummaryResponse(
    claimDetails = claimDetails,
    giftAidDetails = Some(giftAidDetails),
    otherIncomeDetails = Some(otherIncomeDetails),
    gasdsDetails = Some(gasdsDetails),
    adjustmentDetails = Some(adjustmentDetails),
    submissionReferenceNumber = "SUB123456"
  )

  "claimDetails" should {

    "build a SummaryList with claim details rows" in {

      val result = RepaymentClaimSummaryHelper.claimDetails(summary)

      result.rows.size mustBe 5

      result.rows.head.value.content.asHtml.body must include("Test Charity")
      result.rows(1).value.content.asHtml.body   must include("ABC123")
      result.rows(2).value.content.asHtml.body   must include("SUB123456")
      result.rows(3).value.content.asHtml.body   must include("7 Apr 2026 11:34:21")
      result.rows(4).value.content.asHtml.body   must include("John Doe")
    }
  }

  "giftAidDetails" should {

    "return a summary list when gift aid details exist" in {

      val result = RepaymentClaimSummaryHelper.giftAidDetails(summary)

      result mustBe defined
      result.get.rows.size mustBe 2

      result.get.rows.head.value.content.asHtml.body must include("10")
      result.get.rows(1).value.content.asHtml.body   must include("£100.00")
    }

    "return None when gift aid details do not exist" in {

      val result = RepaymentClaimSummaryHelper.giftAidDetails(summary.copy(giftAidDetails = None))

      result mustBe None
    }
  }

  "otherIncomeDetails" should {

    "return a summary list when other income exists" in {

      val result = RepaymentClaimSummaryHelper.otherIncomeDetails(summary)

      result mustBe defined
      result.get.rows.size mustBe 2

      result.get.rows.head.value.content.asHtml.body must include("5")
      result.get.rows(1).value.content.asHtml.body   must include("£50.00")
    }
  }

  "gasdsDetails" should {

    "return summary rows for all defined GASDS fields" in {

      val result = RepaymentClaimSummaryHelper.gasdsDetails(summary)

      result mustBe defined
      result.get.rows.size mustBe 4

      result.get.rows(0).value.content.asHtml.body must include("£25.00")
      result.get.rows(1).value.content.asHtml.body must include("2")
      result.get.rows(2).value.content.asHtml.body must include("£40.00")
      result.get.rows(3).value.content.asHtml.body must include("1")
    }

    "flatten optional values when some GASDS fields are missing" in {

      val partialGasds = gasdsDetails.copy(
        totalValueGasdsInCommunityBuilding = None
      )

      val result = RepaymentClaimSummaryHelper
        .gasdsDetails(summary.copy(gasdsDetails = Some(partialGasds)))

      result mustBe defined
      result.get.rows.size mustBe 3
    }
  }

  "adjustmentDetails" should {

    "return rows for adjustment values" in {

      val result = RepaymentClaimSummaryHelper.adjustmentDetails(summary)

      result mustBe defined
      result.get.rows.size mustBe 2

      result.get.rows(0).value.content.asHtml.body must include("£15.00")
      result.get.rows(1).value.content.asHtml.body must include("£10.00")
    }
  }
}
