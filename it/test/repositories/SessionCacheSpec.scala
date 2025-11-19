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

package repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.Application
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.SessionId
import java.util.UUID
import models.SessionData
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import models.SectionOneAnswers

class SessionCacheSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder().build()

  private val sessionCache = app.injector.instanceOf[SessionCache]

  "SessionCache" should {
    "return None if no session data is found" in {
      given HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))
      whenReady(sessionCache.get()) { result =>
        result should be(None)
      }
    }

    "return some SessionData if found" in {
      given HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))
      whenReady(sessionCache.store(SessionData())) { _ =>
        whenReady(sessionCache.get()) { result =>
          result should be(Some(SessionData()))
        }
      }
    }

    "throw an exception if no session id is found" in {
      given HeaderCarrier = HeaderCarrier()
      intercept[Exception] {
        whenReady(sessionCache.get())(identity)
      }
    }

    "throw an exception if no session data is found and forceSessionCreation is false" in {
      given HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))
      intercept[Exception] {
        whenReady(sessionCache.update(forceSessionCreation = false)(s => s))(identity)
      }
    }

    "create a new session data if no session data is found and forceSessionCreation is true" in {
      given HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))
      whenReady(
        sessionCache.update(forceSessionCreation = true)(s =>
          s.copy(
            sectionOneAnswers = Some(
              SectionOneAnswers(
                claimingGiftAid = Some(true)
              )
            )
          )
        )
      ) { result =>
        result should be(SessionData(sectionOneAnswers = Some(SectionOneAnswers(claimingGiftAid = Some(true)))))
      }
    }

    "update the session data if it exists" in {
      given HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))
      val initialSessionData = SessionData(
        sectionOneAnswers = Some(SectionOneAnswers(claimingGiftAid = Some(true)))
      )
      whenReady(sessionCache.store(initialSessionData)) { _ =>
        whenReady(
          sessionCache.update(forceSessionCreation = false)(s =>
            s.copy(
              sectionOneAnswers = Some(
                SectionOneAnswers(
                  claimingGiftAid = Some(true),
                  claimingTaxDeducted = Some(true)
                )
              )
            )
          )
        ) { result =>
          result should be(
            SessionData(
              sectionOneAnswers = Some(
                SectionOneAnswers(
                  claimingGiftAid = Some(true),
                  claimingTaxDeducted = Some(true)
                )
              )
            )
          )
        }
      }
    }

    "skip updating the session data if it exists and the updated session data is the same" in {
      given HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId(UUID.randomUUID().toString)))
      val initialSessionData = SessionData(
        sectionOneAnswers = Some(SectionOneAnswers(claimingGiftAid = Some(true)))
      )
      whenReady(sessionCache.store(initialSessionData)) { _ =>
        whenReady(
          sessionCache.update(forceSessionCreation = false)(s => s)
        ) { result =>
          result should be(initialSessionData)
        }
      }
    }
  }

}
