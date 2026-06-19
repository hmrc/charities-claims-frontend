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
import utils.TaxYearUtils

import java.time.{LocalDate, ZoneId}

class TaxYearService {

  def validateTaxYears(
    taxYear: Int,
    existingYears: Seq[Int],
    today: LocalDate = LocalDate.now(ZoneId.of("Europe/London"))
  ): Option[TaxYearError] = {

    val (minYear, maxYear) = TaxYearUtils.validRange(today)

    if (taxYear < minYear)
      Some(TaxYearError.TooOld(minYear))
    else if (taxYear > maxYear)
      Some(TaxYearError.Future)
    else if (existingYears.contains(taxYear))
      Some(TaxYearError.Duplicate)
    else
      None
  }
}

object TaxYearService {

  enum TaxYearError {
    case TooOld(minYear: Int)
    case Future
    case Duplicate
  }
}
