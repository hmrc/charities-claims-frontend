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

import cats.syntax.either.*
import play.api.data.*
import play.api.data.format.{Formats, Formatter}
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.text.DecimalFormat
import scala.math.BigDecimal.RoundingMode
import scala.util.control.Exception

object PoundsMapping extends Mappings {
  private val MaxAmount = BigDecimal("1000000000000")

  def apply(
    errorRequired: String,
    formatErrorMsg: String,
    maxAmountError: String,
    allowZero: Boolean = false,
    zeroErrorMsg: Option[String] = None
  ): Mapping[BigDecimal] =
    Forms
      .of[BigDecimal](using moneyBigDecimalFormat(errorRequired, formatErrorMsg))
      .verifying(
        Constraint[BigDecimal]((num: BigDecimal) =>
          num match {
            case n if n < 0         => Invalid(formatErrorMsg)
            case n if n == 0        => if allowZero then Valid else Invalid(zeroErrorMsg.getOrElse(formatErrorMsg))
            case n if n > MaxAmount => Invalid(maxAmountError)
            case _                  => Valid
          }
        )
      )

  private def moneyBigDecimalFormat(errorRequired: String, formatErrorMsg: String): Formatter[BigDecimal] =
    bigDecimalFormat(precision = 15, scale = 2, errorRequired, formatErrorMsg)

  private def bigDecimalFormat(
    precision: Int,
    scale: Int,
    errorRequired: String,
    formatErrorMsg: String
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
          .flatMap { userInput =>
            if userInput.isEmpty then Left(Seq(FormError(key, errorRequired)))
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
