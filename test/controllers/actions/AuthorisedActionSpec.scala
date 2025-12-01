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
import uk.gov.hmrc.auth.core.retrieve.{~, Retrieval}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.EnrolmentIdentifier

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import java.net.URLEncoder

class AuthorisedActionSpec extends BaseSpec {

  class Harness(authorisedAction: AuthorisedAction) {
    def onPageLoad: Action[AnyContent] = authorisedAction { request =>
      Results.Ok(request.affinityGroup.toString())
    }
  }

  val bodyParser: BodyParsers.Default = BodyParsers.Default(Helpers.stubPlayBodyParsers)

  "AuthorisedAction" - {
    "create AuthorisedRequest when user has an Organisation affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-ORG", Seq(EnrolmentIdentifier("CHARID", "1234567890")), "Activated"))
              )
            )
          )
        )
      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      println(redirectLocation(result))
      status(result)          shouldBe OK
      contentAsString(result) shouldBe "Organisation"
    }

    "create AuthorisedRequest when user has an Agent affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-AGENT", Seq(EnrolmentIdentifier("AGENTCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)          shouldBe OK
      contentAsString(result) shouldBe "Agent"
    }

    "redirect to login page when user has no affinity group" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              None,
              Enrolments(
                Set()
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )
    }

    "redirect to login page when an agent has no enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(
                Set()
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )

    }

    "redirect to login page when an organisation has no enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set()
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )

    }

    "redirect to login page when an agent but has enrolment for organisation" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Agent),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-ORG", Seq(EnrolmentIdentifier("CHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )

    }

    "redirect to login page when an organisation but has enrolment for agent" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Organisation),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-AGENT", Seq(EnrolmentIdentifier("AGENTCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )

    }

    "redirect to login page when an Individual affinity group tries to access with incorrect enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Individual),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-IND", Seq(EnrolmentIdentifier("INDCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )

    }

    "redirect to login page when an Individual affinity group tries to access with agent enrolment" in {
      val mockAuthConnector: AuthConnector = mock[AuthConnector]

      (mockAuthConnector
        .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup] ~ Enrolments])(using
          _: HeaderCarrier,
          _: ExecutionContext
        ))
        .expects(*, *, *, *)
        .returning(
          Future.successful(
            `~`(
              Some(AffinityGroup.Individual),
              Enrolments(
                Set(Enrolment("HMRC-CHAR-AGENT", Seq(EnrolmentIdentifier("AGENTCHARID", "1234567890")), "Activated"))
              )
            )
          )
        )

      val authorisedAction =
        new DefaultAuthorisedAction(mockAuthConnector, testFrontendAppConfig, bodyParser)

      val controller = new Harness(authorisedAction)
      val result     = controller.onPageLoad(FakeRequest("GET", "/test"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        s"${testFrontendAppConfig.loginUrl}?continue=${URLEncoder.encode(testFrontendAppConfig.loginContinueUrl, "UTF-8")}"
      )

    }
  }
}
