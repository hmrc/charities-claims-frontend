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

package forms

import forms.Mappings
import play.api.data.Form
import play.api.data.Forms.*
import models.RepaymentClaimType
import CheckBoxListForm.*

import javax.inject.Inject

class CheckBoxListFormProvider @Inject() extends Mappings {

  def apply(
  ): Form[Set[String]] =
    Form(
      "value" -> set(text("alcoholType.error.required")).verifying(nonEmptySet("alcoholType.error.required"))
//      mapping(
//        claimingGiftAid                          -> (boolean("repaymentClaimType.label.claimingGiftAid")),
//        claimingTaxDeducted                      -> (boolean("repaymentClaimType.label.claimingTaxDeducted")),
//        claimingUnderGiftAidSmallDonationsScheme -> (
//          boolean(
//            "repaymentClaimType.label.claimingUnderGiftAidSmallDonationsScheme"
//          )
//        )
//      )(RepaymentClaimType.apply)(x =>
//        Some(x.claimingGiftAid, x.claimingTaxDeducted, x.claimingUnderGiftAidSmallDonationsScheme)
//      )
    )
}

object CheckBoxListForm {

  val claimingGiftAid                          = "claimingGiftAid"
  val claimingUnderGiftAidSmallDonationsScheme = "claimingUnderGiftAidSmallDonationsScheme"
  val claimingTaxDeducted                      = "claimingTaxDeducted"

}
