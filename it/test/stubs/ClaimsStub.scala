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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import utils.WiremockMethods

trait ClaimsStub extends WiremockMethods {

  def stubRetrieveUnsubmittedClaims(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(GET, uri = "/charities-claims/claims\\?claimSubmitted=false")
      .thenReturn(status, body)

  def stubGetClaims(claimId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(GET, uri = s"/charities-claims/claims/$claimId")
      .thenReturn(status, body)

  def stubGetTotalUnregulatedDonations(charityRef: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(GET, uri = s"/charities-claims/charities/$charityRef/unregulated-donations")
      .thenReturn(status, body)

  def stubUpdateClaim(claimId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(PUT, uri = s"/charities-claims/claims/$claimId")
      .thenReturn(status, body)

  def stubDeleteClaim(claimId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(DELETE, uri = s"/charities-claims/claims/$claimId")
      .thenReturn(status, body)

  def stubChrisSubmission(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(POST, uri = s"/charities-claims/chris")
      .thenReturn(status, body)

  def stubGetSubmissionSummary(claimId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(GET, uri = s"/charities-claims/submission-summary/$claimId")
      .thenReturn(status, body)
}
