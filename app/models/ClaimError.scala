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

import play.api.libs.json.{JsPath, Reads}

sealed trait ClaimError extends Exception

case class UpdatedByAnotherUserException() extends ClaimError {
  override def getMessage: String = "UPDATED_BY_ANOTHER_USER"
}

case class UnsubmittedClaimsLimitExceededException() extends ClaimError {
  override def getMessage: String = "UNSUBMITTED_CLAIMS_LIMIT_EXCEEDED"
}

case class UnsubmittedClaimExistsForCharityException() extends ClaimError {
  override def getMessage: String = "UNSUBMITTED_CLAIM_EXISTS_FOR_CHARITY"
}

case class UnknownClaimError(errorCode: String) extends ClaimError {
  override def getMessage: String = errorCode
}

object ClaimError {
  given reads: Reads[ClaimError] = (JsPath \ "errorCode").read[String].map {
    case "UPDATED_BY_ANOTHER_USER"              => UpdatedByAnotherUserException()
    case "UNSUBMITTED_CLAIMS_LIMIT_EXCEEDED"    => UnsubmittedClaimsLimitExceededException()
    case "UNSUBMITTED_CLAIM_EXISTS_FOR_CHARITY" => UnsubmittedClaimExistsForCharityException()
    case other                                  => UnknownClaimError(other)
  }
}
