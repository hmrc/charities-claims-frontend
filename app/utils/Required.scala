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

package utils

import scala.deriving.Mirror
import scala.compiletime.{constValue, erasedValue}
import scala.util.{Failure, Success, Try}
import models.MissingRequiredFieldsException

object Required {

  inline def required[A, T](obj: T)(field: T => Option[A])(using m: Mirror.ProductOf[T]): Try[A] =
    val labels = summonLabels[m.MirroredElemLabels]
    val values = obj.asInstanceOf[Product].productIterator.toList

    val extracted = field(obj) // Option[A]
    val idx       = values.indexWhere(_ == extracted) // compare whole field value

    if idx == -1 then Failure(new MissingRequiredFieldsException("Field not found"))
    else
      val name = labels(idx)
      extracted match
        case Some(v) => Success(v)
        case None    => Failure(new MissingRequiredFieldsException(s"Missing required field: $name"))

  inline def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (h *: t)   => constValue[h].toString :: summonLabels[t]

  extension [A](opt: Option[A]) {
    def flatMapTry[B](f: A => Try[B]): Try[Option[B]] =
      opt match {
        case None    => Success(None)
        case Some(a) => f(a).map(Some(_))
      }
  }
}
