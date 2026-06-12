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

  lazy val gdsFormatter: DateTimeFormatter           = DateTimeFormatter.ofPattern("d MMMM yyyy")
  lazy val gdsShortMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
  lazy val ddMmYyyyFormatter: DateTimeFormatter      = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  private def formatDateToGds(localDate: LocalDate)(using messages: Messages): String = {
    val month = messages(s"month.${localDate.getMonthValue}.long")
    s"${localDate.getDayOfMonth} $month ${localDate.getYear}"
  }

  private def formatDateToGdsShortMonth(localDate: LocalDate)(using messages: Messages): String = {
    val month = messages(s"month.${localDate.getMonthValue}.short")
    s"${localDate.getDayOfMonth} $month ${localDate.getYear}"
  }

  private def formatDateToDdMmYyyy(localDate: LocalDate): String =
    localDate.format(ddMmYyyyFormatter)

  private def formatAmount(amount: BigDecimal): String = {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.UK)
    currencyFormatter.format(amount)
  }

  extension (string: String) {
    def toGdsDateString(using messages: Messages): String           = formatDateToGds(LocalDate.parse(string))
    def toGdsShortMonthDateString(using messages: Messages): String = formatDateToGdsShortMonth(LocalDate.parse(string))
    def toDdMmYyyyDateString: String                                = formatDateToDdMmYyyy(LocalDate.parse(string))
  }

  extension (amount: BigDecimal) {
    def toAmountString: String = formatAmount(amount)
  }

  extension (boolean: Boolean) {
    def toYesNoString(using messages: Messages): String =
      if boolean
      then messages("site.yes")
      else messages("site.no")
  }

  extension (field: String) {
    def toRowNumber: String = {
      val pattern = """\[(\d+)\]""".r
      pattern.findFirstMatchIn(field).map(m => (m.group(1).toInt + 1).toString).getOrElse("0")
    }
  }
}
