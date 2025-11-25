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

package util

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

import play.api.test.FakeRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalamock.scalatest.MockFactory
import org.apache.pekko.stream.Materializer
import config.FrontendAppConfig
import generators.Generators
import org.apache.pekko.actor.ActorSystem
import play.api.i18n.{Messages, MessagesApi}
import org.scalatest.freespec.AnyFreeSpec
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import play.api.{Application, Configuration}
import org.scalatest.time.{Millis, Span}

abstract class BaseSpec
    extends AnyFreeSpec
    with MockFactory
    with Matchers
    with ScalaFutures
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with OptionValues
    with Generators {

  implicit val actorSystem: ActorSystem = ActorSystem("unit-tests")
  implicit val mat: Materializer        = Materializer.createMaterializer(actorSystem)

  override protected def afterAll(): Unit =
    actorSystem.terminate()

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(1000, Millis)), interval = scaled(Span(50, Millis)))

  implicit def createMessages(implicit app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def messages(key: String)(implicit m: Messages): String = m(key)

  protected def messages(key: String, args: String*)(implicit m: Messages): String = m(key, args*)

  protected val testFrontendAppConfig = new FrontendAppConfig(
    Configuration(
      ConfigFactory.parseString(
        """
          | appName = foo-bar-frontend
          | mongodb {
          |  ttl = 15 minutes
          | }
          | urls {
          |  login = "http://foo.com/login"
          |  loginContinue = "http://foo.com/bar"
          |  signOut = "http://foo.com/sign-out"
          |  homePageUrl = "http://foo.com/home"
          |  accessibilityStatementUrl = "http://foo.com/accessibility-statement"
          |  betaFeedbackUrl = "http://foo.com/beta-feedback"
          |  researchUrl = "http://foo.com/research"
          | }
          |
          | enableLanguageSwitching = true  
          |
          | timeout-dialog {
          |   timeout   = 10  
          |   countdown = 5
          | }
          |""".stripMargin
      )
    )
  )
}
