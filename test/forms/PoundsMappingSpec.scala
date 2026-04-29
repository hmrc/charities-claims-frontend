/*
 * Copyright 2025 HM Revenue & Customs
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

import util.BaseSpec
import play.api.data.FormError

class PoundsMappingSpec extends BaseSpec {

  "PoundsMapping" - {
    "bind valid pounds amount with decimal places" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "123.45"))
      result shouldBe Right(BigDecimal("123.45"))
    }

    "bind valid pounds amount without decimal places" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "123"))
      result shouldBe Right(BigDecimal("123"))
    }

    "bind invalid pounds amount (empty)" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> ""))
      result shouldBe Left(List(FormError("", List("error.required"), List())))
    }

    "bind invalid pounds amount (too many decimal places)" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "123.456"))
      result shouldBe Left(List(FormError("", List("error.invalid"), List())))
    }

    "bind invalid pounds amount (invalid characters)" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "123.45a"))
      result shouldBe Left(List(FormError("", List("error.invalid"), List())))
    }

    "bind invalid pounds amount (too many digits)" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxLength",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "12345678901234567890"))
      result shouldBe Left(List(FormError("", List("error.maxLength"), List())))
    }

    "bind invalid pounds amount (negative)" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "-123.45"))
      result shouldBe Left(List(FormError("", List("error.invalid"), List())))
    }

    "bind invalid pounds amount (zero but not allowed)" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = false
      )
      val result   = mappings.bind(Map("" -> "0"))
      result shouldBe Left(List(FormError("", List("error.required"), List())))
    }

    "bind zero pounds amount when allowed" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxAmount",
        allowZero = true
      )
      val result   = mappings.bind(Map("" -> "0"))
      result shouldBe Right(BigDecimal("0"))
    }

    "bind invalid pounds amount when input exceeds max length" in {
      val mappings = PoundsMapping(
        errorRequired = "error.required",
        formatErrorMsg = "error.invalid",
        maxLengthErrorMsg = "error.maxLength",
        allowZero = true
      )

      val result = mappings.bind(Map("" -> "12345678901234567"))

      result shouldBe Left(List(FormError("", List("error.maxLength"), List())))
    }
  }
}
