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

package models

import play.api.libs.json.Format
import play.api.libs.json.Json
import scala.util.Try
import utils.Required.required

final case class DeclarationDetailsAnswers(
  understandFalseStatements: Option[Boolean] = None,
  includedAnyAdjustmentsInClaimPrompt: Option[String] = None
)

object DeclarationDetailsAnswers {

  given Format[DeclarationDetailsAnswers] = Json.format[DeclarationDetailsAnswers]

  private def get[A](f: DeclarationDetailsAnswers => Option[A])(using session: SessionData): Option[A] =
    session.declarationDetailsAnswers.flatMap(f)

  private def set[A](value: A)(f: (DeclarationDetailsAnswers, A) => DeclarationDetailsAnswers)(using
    session: SessionData
  ): SessionData =
    val updated = session.declarationDetailsAnswers match
      case Some(existing) => f(existing, value)
      case None           => f(DeclarationDetailsAnswers(), value)
    session.copy(declarationDetailsAnswers = Some(updated))

  def from(declarationDetails: DeclarationDetails): DeclarationDetailsAnswers =
    DeclarationDetailsAnswers(
      understandFalseStatements = Some(declarationDetails.understandFalseStatements),
      includedAnyAdjustmentsInClaimPrompt = Some(declarationDetails.includedAnyAdjustmentsInClaimPrompt)
    )

  def getIncludedAnyAdjustmentsInClaimPrompt(using session: SessionData): Option[String] = get(
    _.includedAnyAdjustmentsInClaimPrompt
  )

  def setIncludedAnyAdjustmentsInClaimPrompt(value: Option[String])(using session: SessionData): SessionData =
    set(value)((a, v) => a.copy(includedAnyAdjustmentsInClaimPrompt = v))

  def toDeclarationDetails(answers: DeclarationDetailsAnswers): Try[DeclarationDetails] =
    for {
      understandFalseStatements           <- required(answers)(_.understandFalseStatements)
      includedAnyAdjustmentsInClaimPrompt <- required(answers)(_.includedAnyAdjustmentsInClaimPrompt)
    } yield DeclarationDetails(
      understandFalseStatements = understandFalseStatements,
      includedAnyAdjustmentsInClaimPrompt = includedAnyAdjustmentsInClaimPrompt
    )
}
