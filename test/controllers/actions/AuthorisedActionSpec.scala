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

package controllers.actions

import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.*
import util.BaseSpec
import play.api.mvc.*
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

import java.net.URLEncoder

class AuthorisedActionSpec extends BaseSpec {

  class Harness(authorisedAction: AuthorisedAction) {
    def onPageLoad: Action[AnyContent] = authorisedAction { _ =>
      Results.Ok("")
    }
  }

  val bodyParser: BodyParsers.Default = BodyParsers.Default(Helpers.stubPlayBodyParsers)
  val credentials                     = Credentials("test-user-id", "GovernmentGateway")

  "AuthorisedAction" - {
    "create AuthorisedRequest when user has an Individual affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Option[Credentials]])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(Some(AffinityGroup.Individual), Some(credentials))
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) must be(OK)
    }

    "create AuthorisedRequest when user has an Organisation affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Option[Credentials]])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(Some(AffinityGroup.Organisation), Some(credentials))
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) must be(OK)
    }

    "create AuthorisedRequest when user has an Agent affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Option[Credentials]])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(Some(AffinityGroup.Agent), Some(credentials))
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result) must be(OK)
    }

    "redirect to login page when user has no affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Option[Credentials]])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(None, Some(credentials))
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(
          s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
        )
      )
    }

    "redirect to login page when user has no credential provider id" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Option[Credentials]])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(Some(AffinityGroup.Individual), None)
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(
          s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
        )
      )
    }

  }
}
