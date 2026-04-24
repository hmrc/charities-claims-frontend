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

import models.GiftAidSmallDonationsSchemeDonationDetailsAnswers
import play.api.i18n.Messages

object ClaimAddedForTaxYearHelper {

  def getTaxYears(gasdsAnswers: Option[GiftAidSmallDonationsSchemeDonationDetailsAnswers]): Seq[Int] =
    gasdsAnswers.toSeq
      .flatMap(_.claims.toSeq)
      .flatMap(_.collect { case Some(c) => c.taxYear })

  def buildCustomSummaryList(
    taxYears: Seq[Int]
  )(using messages: Messages): Option[Seq[(String, Seq[(String, String, String)])]] = {
    val taxYearLabels: Seq[String] = taxYears.map { taxYear =>
      messages("claimAddedForTaxYear.taxYear.key", taxYear.toString)
    }

    val customSummaryListRows: Seq[(String, Seq[(String, String, String)])] = taxYearLabels.zipWithIndex.map {
      (taxYearLabel, index) =>
        buildCustomSummaryListRows(taxYearLabels.size > 1, taxYearLabel, index + 1)
    }

    Option.when(customSummaryListRows.nonEmpty)(customSummaryListRows)
  }

  def buildCustomSummaryListRows(
    isMultipleTaxYears: Boolean,
    label: String,
    index: Int
  ): (String, Seq[(String, String, String)]) =
    val baseActions =
      Seq(
        (
          controllers.giftAidSmallDonationsScheme.routes.ClaimDetailsForTaxYearCheckYourAnswersController
            .onPageLoad(index)
            .url,
          "site.change",
          label
        )
      )

    val actions =
      if isMultipleTaxYears then
        baseActions :+ (controllers.giftAidSmallDonationsScheme.routes.RemoveClaimForTaxYearController
          .onPageLoad(index)
          .url, "site.remove", label)
      else baseActions

    label -> actions

  def getSingularOrPlural(countOfTaxYears: Int)(using messages: Messages): String =
    if (countOfTaxYears > 1) messages("claimAddedForTaxYear.singularOrPlural.plural")
    else messages("claimAddedForTaxYear.singularOrPlural.singular")

}
