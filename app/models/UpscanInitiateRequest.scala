/*
 * Copyright 2026 HM Revenue & Customs
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

/** Request to Upscan Initiate. see: https://github.com/hmrc/upscan-initiate#post-upscanv2initiate
  *
  * @param callbackUrl
  *   (optional) Set by the connector based on the claimId and the validation service url. Url that will be called to
  *   report the outcome of file checking and upload, including retrieval details
  * @param successRedirect
  *   Url to redirect to after file has been successfully uploaded
  * @param errorRedirect
  *   Url to redirect to if error encountered during upload
  * @param minimumFileSize
  *   (optional) Minimum file size (in Bytes). Default is 0
  * @param maximumFileSize
  *   (optional) Maximum file size (in Bytes). Cannot be greater than 100MB. Default is 100MB
  * @param consumingService
  *   (optional) Set by the connector based on the service config. Used to identify the config name of the service
  *   initiating the upload journey.
  */
case class UpscanInitiateRequest(
  successRedirect: String,
  errorRedirect: String,
  callbackUrl: Option[String] = None,
  minimumFileSize: Option[Int] = None,
  maximumFileSize: Option[Int] = None,
  consumingService: Option[String] = None
)

object UpscanInitiateRequest {
  given Format[UpscanInitiateRequest] = Json.format[UpscanInitiateRequest]
}
