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

import play.api.libs.json.{Format, Json}

/** Request to create upload tracking. see: https://github.com/hmrc/charities-claims-validation#create-upload-tracking
  *
  * @param reference
  *   The reference that the uploading service receives during the call to upscan-initiate. This will be the correlating
  *   reference used when upscan-notify calls the callback URL for this microservice.
  * @param validationType
  *   The specific validation ruleset to be executed after the callback URL is called with a 'success' response.
  * @param uploadUrl
  *   The unique upload URL (href) provided in the response from upscan-initiate.
  * @param initiateTimestamp
  *   The timestamp of when the call to upscan-initiate was made.
  */
final case class CreateUploadTrackingRequest(
  reference: UpscanReference,
  validationType: ValidationType,
  uploadUrl: String,
  initiateTimestamp: String,
  fields: Map[String, String]
)

object CreateUploadTrackingRequest {
  given Format[CreateUploadTrackingRequest] = Json.format[CreateUploadTrackingRequest]
}
