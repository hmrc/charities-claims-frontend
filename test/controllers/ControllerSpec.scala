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

package controllers

import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import connectors.{ClaimsConnector, ClaimsValidationConnector}
import controllers.actions.{AuthorisedAction, DataRetrievalAction, RefreshDataAction}
import models.*
import play.api.data.Form
import play.api.http.*
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.*
import play.api.test.*
import play.api.{inject, Application}
import repositories.SessionCache
import services.{ClaimsService, SaveService}
import uk.gov.hmrc.http.HeaderCarrier
import util.*

import scala.concurrent.Future

trait ControllerSpec
    extends BaseSpec
    with DefaultAwaitTimeout
    with HttpVerbs
    with Writeables
    with HeaderNames
    with Status
    with PlayRunners
    with RouteInvokers
    with ResultExtractors
    with DiffShouldMatcher {

  val baseUrl = "/charities-claims"

  val testOnwardRoute: Call = Call("GET", "/foo")

  given defaultSessionData: SessionData = SessionData.empty(testCharitiesReference)

  protected def applicationBuilder(sessionData: SessionData = defaultSessionData): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        List[GuiceableModule](
          inject
            .bind[DataRetrievalAction]
            .toInstance(new FakeDataRetrievalAction(sessionData)),
          inject
            .bind[RefreshDataAction]
            .toInstance(new FakeRefreshDataAction(sessionData)),
          inject
            .bind[AuthorisedAction]
            .toInstance(new FakeAuthorisedAction)
        ) ++
          additionalBindings*
      )
      .configure(
        "play.filters.csp.nonce.enabled" -> false,
        "auditing.enabled"               -> false,
        "metric.enabled"                 -> false
      )

  protected def applicationBuilder(claim: Claim, uploads: Seq[UploadSummary]): GuiceApplicationBuilder = {
    val sessionData = SessionData.from(claim, "org-123", Some(GetUploadSummaryResponse(uploads)))
    new GuiceApplicationBuilder()
      .overrides(
        List[GuiceableModule](
          inject
            .bind[SessionCache]
            .toInstance(new FakeSessionCache(sessionData)),
          inject
            .bind[ClaimsConnector]
            .toInstance(new FakeClaimsConnector(claim)),
          inject
            .bind[ClaimsValidationConnector]
            .toInstance(new FakeClaimsValidationConnector(uploads)),
          inject
            .bind[AuthorisedAction]
            .toInstance(new FakeAuthorisedAction)
        ) ++
          additionalBindings*
      )
      .configure(
        "play.filters.csp.nonce.enabled" -> false,
        "auditing.enabled"               -> false,
        "metric.enabled"                 -> false
      )
  }

  extension (appBuilder: GuiceApplicationBuilder) {
    def mockSaveSession: GuiceApplicationBuilder = {
      val mockSaveService: SaveService = mock[SaveService]
      (mockSaveService
        .save(_: SessionData)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))
      appBuilder.overrides(
        inject
          .bind[SaveService]
          .toInstance(mockSaveService)
      )
    }

    def mockSaveClaim: GuiceApplicationBuilder = {
      val mockClaimsService: ClaimsService = mock[ClaimsService]
      (mockClaimsService
        .save(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.successful(()))
      appBuilder.overrides(
        inject
          .bind[ClaimsService]
          .toInstance(mockClaimsService)
      )
    }

    def mockSaveClaimFailed(exception: Throwable): GuiceApplicationBuilder = {
      val mockClaimsService: ClaimsService = mock[ClaimsService]
      (mockClaimsService
        .save(using _: HeaderCarrier))
        .expects(*)
        .returning(Future.failed(exception))
      appBuilder.overrides(
        inject
          .bind[ClaimsService]
          .toInstance(mockClaimsService)
      )
    }

    def mockClaimsConnectorLastVisitedAt: GuiceApplicationBuilder = {
      val mockClaimsConnector: ClaimsConnector = mock[ClaimsConnector]
      (mockClaimsConnector
        .updateLastVisitedAt(_: String)(using _: HeaderCarrier))
        .expects(*, *)
        .returning(Future.successful(()))
      appBuilder.overrides(
        inject
          .bind[ClaimsConnector]
          .toInstance(mockClaimsConnector)
      )
    }
  }
  protected val additionalBindings: List[GuiceableModule] = List()

  def messages(application: Application): Messages = {
    val messagesApi = application.injector.instanceOf[MessagesApi]
    messagesApi.preferred(Seq(Lang.defaultLang))
  }

  def runningApplication[T](block: Application => T): T =
    running(_ => applicationBuilder())(block)

  def formData[A](form: Form[A], data: A): List[(String, String)] = form.fill(data).data.toList
}
