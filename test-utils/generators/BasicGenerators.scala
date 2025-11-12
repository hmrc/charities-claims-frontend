/*
 * Copyright 2024 HM Revenue & Customs
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

package generators

import org.scalacheck.Gen._
import org.scalacheck.Gen
import org.scalatest.EitherValues

trait BasicGenerators extends EitherValues {

  implicit val basicStringGen: Gen[String] = nonEmptyAlphaString
  implicit val unitGen: Gen[Unit] = Gen.const(())

  def nonEmptyString: Gen[String] =
    for {
      c <- alphaNumChar
      s <- alphaNumStr
    } yield s"$c$s"

  def nonEmptyAlphaString: Gen[String] =
    for {
      c <- alphaChar
      s <- alphaStr
    } yield {
      s"$c$s"
    }
}
