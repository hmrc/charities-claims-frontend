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

import play.api.data.*
import scala.math.BigDecimal.RoundingMode
import play.api.data.format.Formatter
import play.api.data.format.Formats
import scala.util.control.Exception
import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import cats.syntax.either.*
import java.text.DecimalFormat

object PoundsMapping extends Mappings {

  def apply(
    errorRequired: String,
    formatErrorMsg: String,
    maxLengthErrorMsg: String,
    allowZero: Boolean = false
  ): Mapping[BigDecimal] =
    Forms
      .of[BigDecimal](using moneyBigDecimalFormat(errorRequired, formatErrorMsg, maxLengthErrorMsg))
      .verifying(
        Constraint[BigDecimal]((num: BigDecimal) =>
          num match {
            case n if n < 0  => Invalid(formatErrorMsg)
            case n if n == 0 => if allowZero then Valid else Invalid(errorRequired)
            case _           => Valid
          }
        )
      )

  private def moneyBigDecimalFormat(
    errorRequired: String,
    formatErrorMsg: String,
    maxLengthErrorMsg: String
  ): Formatter[BigDecimal] =
    bigDecimalFormat(precision = 15, scale = 2, errorRequired, formatErrorMsg, maxLengthErrorMsg)

  def bigDecimalFormat(
    precision: Int,
    scale: Int,
    errorRequired: String,
    formatErrorMsg: String,
    maxLengthErrorMsg: String
  ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {
      override val format: Option[(String, Nil.type)] = Some(("format.real", Nil))

      val decimalFormat = new DecimalFormat()
      decimalFormat.setMaximumFractionDigits(scale)
      decimalFormat.setMinimumFractionDigits(0)
      decimalFormat.setGroupingUsed(false)

      def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        Formats.stringFormat
          .bind(key, data)
          .flatMap { value =>
            val userInput = value.trim
            if userInput.isEmpty then Left(Seq(FormError(key, errorRequired)))
            else if userInput.length > 16 then Left(Seq(FormError(key, maxLengthErrorMsg)))
            else
              Exception
                .allCatch[BigDecimal]
                .either(
                  BigDecimal(userInput)
                )
                .flatMap { bd =>
                  if bd.scale > scale then Left(new Throwable("Wrong precision"))
                  else if bd.precision - bd.scale > precision - scale then Left(new Throwable("Wrong precision"))
                  else Right(bd.setScale(scale, RoundingMode.HALF_UP))
                }
                .leftMap(_ => Seq(FormError(key, formatErrorMsg)))
          }

      def unbind(key: String, value: BigDecimal): Map[String, String] =
        Map(key -> decimalFormat.format(value))
    }
}
