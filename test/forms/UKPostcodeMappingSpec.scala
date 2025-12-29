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

class UKPostcodeMappingSpec extends BaseSpec {

  "UKPostcodeMapping" - {
    "bind valid UK Postcode" in {
      val mappings = UKPostcodeMapping(
        "a",
        "b",
        "c"
      )
      val result   = mappings.bind(Map("" -> "AA1 1AA"))
      result shouldBe Right(Some("AA1 1AA"))
    }

    "bind empty UK Postcode" in {
      val mappings = UKPostcodeMapping(
        "foo",
        "b",
        "c"
      )
      val result   = mappings.bind(Map("" -> ""))
      result shouldBe Left(List(FormError("", List("foo"), List())))
    }

    "bind invalid UK Postcode" in {
      val mappings = UKPostcodeMapping(
        "a",
        "b",
        "foo"
      )
      val result   = mappings.bind(Map("" -> "AA!"))
      result shouldBe Left(
        List(
          FormError(
            "",
            List("foo"),
            List(
              """^\s*((GIR 0AA)|((([a-zA-Z][0-9][0-9]?)|(([a-zA-Z][a-hj-yA-HJ-Y][0-9][0-9]?)|(([a-zA-Z][0-9][a-zA-Z])|([a-zA-Z][a-hj-yA-HJ-Y][0-9]?[a-zA-Z]))))\s?[0-9][a-zA-Z]{2})\s*)$"""
            )
          )
        )
      )
    }

    "bind too long UK Postcode" in {
      val mappings = UKPostcodeMapping(
        "a",
        "foo",
        "c"
      )
      val result   = mappings.bind(Map("" -> "AAAAAAAAA"))
      result shouldBe Left(List(FormError("", List("foo"), List(8))))
    }
  }
}
