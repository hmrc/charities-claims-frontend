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

import services.TaxYearService.TaxYearError

import java.time.LocalDate

class TaxYearService {

  def validateTaxYears(
    taxYear: Int,
    existingYears: Seq[Int],
    today: LocalDate = LocalDate.now()
  ): Option[TaxYearError] = {

    val (minYear, maxYear) = validRange(today)

    if (taxYear < minYear)
      Some(TaxYearError.TooOld(minYear))
    else if (taxYear > maxYear)
      Some(TaxYearError.Future)
    else if (existingYears.contains(taxYear))
      Some(TaxYearError.Duplicate)
    else
      None
  }

  private def validRange(today: LocalDate): (Int, Int) = {
    val end = currentTaxYearEnd(today)
    (end - 3, end)
  }

  private def currentTaxYearStart(today: LocalDate): Int = {
    val year         = today.getYear
    val taxYearStart = LocalDate.of(year, 4, 6)

    if (today.isBefore(taxYearStart)) year - 1 else year
  }

  private def currentTaxYearEnd(today: LocalDate): Int =
    currentTaxYearStart(today) + 1
}

object TaxYearService {

  enum TaxYearError {
    case TooOld(minYear: Int)
    case Future
    case Duplicate
  }
}
