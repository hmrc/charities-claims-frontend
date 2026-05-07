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

package models

import play.api.libs.json.*

enum WhoShouldHmrcSendPaymentTo(val value: String):
  case CharityOrCASC extends WhoShouldHmrcSendPaymentTo("Charity/CASC")
  case AgentOrNominee extends WhoShouldHmrcSendPaymentTo("Agent/Nominee")

  override def toString: String = value

object WhoShouldHmrcSendPaymentTo extends Enumerable.Implicits {

  private val allValues = WhoShouldHmrcSendPaymentTo.values.toSeq

  given Enumerable[WhoShouldHmrcSendPaymentTo] =
    Enumerable { str =>
      allValues.find(_.value == str)
    }

  given Format[WhoShouldHmrcSendPaymentTo] = Format(
    Reads {
      case JsString(str) =>
        allValues
          .find(_.value == str)
          .map(JsSuccess(_))
          .getOrElse(JsError("error.invalid"))
      case _             => JsError("error.invalid")
    },
    Writes(v => JsString(v.value))
  )
}
