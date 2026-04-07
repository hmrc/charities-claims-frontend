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

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.validation

trait Constraints extends validation.Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.replace("\r\n", "\n").matches(regex) =>
        Valid
      case _                                               =>
        Invalid(errorKey, regex)
    }

  override def maxLength(length: Int, errorKey: String): Constraint[String] =
    Constraint[String]("constraint.maxLength", length) { input =>
      val normalisedInput = input.replace("\r\n", "\n")
      require(length >= 0, "string maxLength must not be negative")
      if (normalisedInput == null) Invalid(ValidationError(errorKey, length))
      else if (normalisedInput.length <= length) Valid
      else Invalid(ValidationError(errorKey, length))
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[?]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _                   =>
        Invalid(errorKey)
    }

  protected def nonEmptySeq(errorKey: String, args: Any*): Constraint[Seq[?]] =
    Constraint {
      case seq if seq.nonEmpty =>
        Valid
      case _                   =>
        Invalid(errorKey, args*)
    }

}
