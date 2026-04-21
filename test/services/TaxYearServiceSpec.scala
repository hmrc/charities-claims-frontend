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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import services.TaxYearService.TaxYearError

import java.time.LocalDate

class TaxYearServiceSpec extends AnyWordSpec with Matchers {

  private val service = new TaxYearService

  private val fixedToday = LocalDate.of(2026, 6, 1)

  // currentTaxYearStart = 2026
  // currentTaxYearEnd   = 2027
  // valid range         = 2024 to 2027
  private val minYear = 2024
  private val maxYear = 2027

  "TaxYearService" should {

    "return None for a valid tax year within range and not duplicate" in {
      val result = service.validateTaxYears(
        taxYear = 2025,
        existingYears = Seq.empty,
        today = fixedToday
      )

      result shouldBe None
    }

    "return TooOld when tax year is below minimum" in {
      val result = service.validateTaxYears(
        taxYear = 2023,
        existingYears = Seq.empty,
        today = fixedToday
      )

      result shouldBe Some(TaxYearError.TooOld(minYear))
    }

    "return Future when tax year is above maximum" in {
      val result = service.validateTaxYears(
        taxYear = 2028,
        existingYears = Seq.empty,
        today = fixedToday
      )

      result shouldBe Some(TaxYearError.Future)
    }

    "return Duplicate when tax year already exists" in {
      val result = service.validateTaxYears(
        taxYear = 2025,
        existingYears = Seq(2024, 2025),
        today = fixedToday
      )

      result shouldBe Some(TaxYearError.Duplicate)
    }

    "allow boundary values (min and max)" in {
      val minResult = service.validateTaxYears(
        taxYear = minYear,
        existingYears = Seq.empty,
        today = fixedToday
      )

      val maxResult = service.validateTaxYears(
        taxYear = maxYear,
        existingYears = Seq.empty,
        today = fixedToday
      )

      minResult shouldBe None
      maxResult shouldBe None
    }

    "prioritise range validation over duplicate check" in {
      val result = service.validateTaxYears(
        taxYear = 2023,
        existingYears = Seq(2023),
        today = fixedToday
      )

      result shouldBe Some(TaxYearError.TooOld(minYear))
    }
  }
}
