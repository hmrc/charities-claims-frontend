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

import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import models.*
import util.BaseSpec
import viewmodels.ClaimAddedForTaxYearHelper

class ClaimAddedForTaxYearHelperSpec extends BaseSpec {

  lazy val app: Application = new GuiceApplicationBuilder().build()

  given Messages = MessagesImpl(
    Lang.defaultLang,
    app.injector.instanceOf[MessagesApi]
  )

  "ClaimAddedForTaxYearHelper" - {

    "getTaxYears" - {

      "should return tax years from valid GASDS answers" in {
        val answers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(
              Seq(
                Some(GiftAidSmallDonationsSchemeClaimAnswers(2024, None)),
                Some(GiftAidSmallDonationsSchemeClaimAnswers(2025, None))
              )
            )
          )
        )

        val result = ClaimAddedForTaxYearHelper.getTaxYears(answers)

        result shouldBe Seq(2024, 2025)
      }

      "should ignore None values in claims" in {
        val answers = Some(
          GiftAidSmallDonationsSchemeDonationDetailsAnswers(
            claims = Some(
              Seq(
                Some(GiftAidSmallDonationsSchemeClaimAnswers(2024, None)),
                None
              )
            )
          )
        )

        val result = ClaimAddedForTaxYearHelper.getTaxYears(answers)

        result shouldBe Seq(2024)
      }

      "should return empty sequence when no answers" in {
        val result = ClaimAddedForTaxYearHelper.getTaxYears(None)

        result shouldBe empty
      }
    }

    "buildCustomSummaryList" - {

      "should return None when no tax years" in {
        val result = ClaimAddedForTaxYearHelper.buildCustomSummaryList(Seq.empty)

        result shouldBe None
      }

      "should return summary list for single tax year (only change action)" in {
        val result = ClaimAddedForTaxYearHelper.buildCustomSummaryList(Seq(2024))

        result.isDefined shouldBe true

        val rows = result.get
        rows.size shouldBe 1

        val (_, actions) = rows.head

        actions.size    shouldBe 1
        actions.head._2 shouldBe "site.change"
      }

      "should return summary list with change and remove for multiple tax years" in {
        val result = ClaimAddedForTaxYearHelper.buildCustomSummaryList(Seq(2024, 2025))

        val rows = result.get
        rows.size shouldBe 2

        rows.foreach { case (_, actions) =>
          actions.size     shouldBe 2
          (actions.map(_._2) should contain).allOf("site.change", "site.remove")
        }
      }
    }

    "buildCustomSummaryListRows" - {

      "should return only change action when single tax year" in {
        val (label, actions) =
          ClaimAddedForTaxYearHelper.buildCustomSummaryListRows(
            isMultipleTaxYears = false,
            label = "2024",
            index = 1
          )

        label           shouldBe "2024"
        actions.size    shouldBe 1
        actions.head._2 shouldBe "site.change"
      }

      "should return change and remove actions when multiple tax years" in {
        val (_, actions) =
          ClaimAddedForTaxYearHelper.buildCustomSummaryListRows(
            isMultipleTaxYears = true,
            label = "2024",
            index = 1
          )

        actions.size     shouldBe 2
        (actions.map(_._2) should contain).allOf("site.change", "site.remove")
      }

      "should generate correct URLs with index" in {
        val (_, actions) =
          ClaimAddedForTaxYearHelper.buildCustomSummaryListRows(
            isMultipleTaxYears = true,
            label = "2024",
            index = 2
          )

        actions.head._1 should include("/2")
      }
    }

    "getSingularOrPlural" - {

      "should return singular when count is 1" in {
        val result = ClaimAddedForTaxYearHelper.getSingularOrPlural(1)

        result shouldBe messages("claimAddedForTaxYear.singularOrPlural.singular")
      }

      "should return plural when count is greater than 1" in {
        val result = ClaimAddedForTaxYearHelper.getSingularOrPlural(2)

        result shouldBe messages("claimAddedForTaxYear.singularOrPlural.plural")
      }
    }
  }
}
