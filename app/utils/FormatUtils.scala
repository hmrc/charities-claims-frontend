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

package utils

import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.text.NumberFormat
import java.util.Locale
import play.api.i18n.Messages

object FormatUtils {

  lazy val gdsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def formatDateToGds(localDate: LocalDate): String =
    localDate.format(gdsFormatter)

  def formatAmount(amount: BigDecimal): String = {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.UK)
    currencyFormatter.format(amount)
  }

  extension (localDate: LocalDate) {
    def toGdsDateString: String = formatDateToGds(localDate)
  }

  extension (amount: BigDecimal) {
    def toAmountString: String = formatAmount(amount)
  }

  extension (string: String) {
    def toGdsDateString: String = formatDateToGds(LocalDate.parse(string))
  }

  extension (boolean: Boolean) {
    def toYesNoString(using messages: Messages): String =
      if boolean
      then messages("site.yes")
      else messages("site.no")
  }

}
