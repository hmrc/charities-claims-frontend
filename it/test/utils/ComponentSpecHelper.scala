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

package utils

import org.mongodb.scala.SingleObservableFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Lang, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.libs.ws.WSBodyWritables.writeableOf_String
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSCookie, WSRequest, WSResponse}
import play.api.mvc.{Session, SessionCookieBaker}
import play.api.test.Helpers.*
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.test.MongoSupport
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

import scala.concurrent.duration.FiniteDuration

trait ComponentSpecHelper
    extends AnyWordSpec
    with Matchers
    with CustomMatchers
    with WiremockHelper
    with MongoSupport
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite {

  def extraConfig(): Map[String, String] = Map.empty

  override protected def initTimeout: FiniteDuration = 10.seconds

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(config ++ extraConfig())
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")
      .build()

  implicit lazy val ws: WSClient =
    app.injector.instanceOf[WSClient]

  implicit lazy val messagesApi: MessagesApi =
    app.injector.instanceOf[MessagesApi]

  implicit lazy val messagesProvider: MessagesImpl =
    MessagesImpl(Lang("en"), messagesApi)

  def msg(key: String) = messagesProvider(key)

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString

  def config: Map[String, String] =
    Map(
      "mongodb.uri"                                            -> mongoUri,
      "microservice.services.auth.host"                        -> mockHost,
      "microservice.services.auth.port"                        -> mockPort,
      "microservice.services.charities-claims.host"            -> mockHost,
      "microservice.services.charities-claims.port"            -> mockPort,
      "microservice.services.charities-claims-validation.host" -> mockHost,
      "microservice.services.charities-claims-validation.port" -> mockPort,
      "microservice.services.upscan-initiate.host"             -> mockHost,
      "microservice.services.upscan-initiate.port"             -> mockPort,
      "microservice.services.upscan-initiate.port"             -> mockPort,
      "auditing.enabled"                                       -> "false",
      "play.filters.csrf.header.bypassHeaders.Csrf-Token"      -> "nocheck"
    )

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    try {
      dropTestDatabase()
      mongoClient.close()
    } finally {
      stopWiremock()
      super.afterAll()
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
    dropTestDatabase()
  }

  private def dropTestDatabase(): Unit = {
    require(databaseName.startsWith("test-"), "Refusing to drop non-test database")
    await(mongoDatabase.drop().toFuture())
  }

  val baseUrl: String = "/charities-claims"

  private def buildClient(path: String): WSRequest =
    ws.url(s"http://localhost:$port$baseUrl$path").withFollowRedirects(false)

  val enLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "en")
  val cyLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "cy")

  def get(uri: String, cookie: WSCookie = enLangCookie): WSResponse =
    await(
      buildClient(uri)
        .withHttpHeaders("Authorization" -> "Bearer 123")
        .withCookies(cookie, mockSessionCookie())
        .get()
    )

  def post[T](uri: String, cookie: WSCookie = enLangCookie)(body: T)(implicit writes: Writes[T]): WSResponse =
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer 123")
        .withCookies(cookie, mockSessionCookie())
        .post(writes.writes(body).toString())
    )

  def delete(uri: String): WSResponse =
    await(
      buildClient(uri)
        .withHttpHeaders("Authorization" -> "Bearer 123")
        .withCookies(mockSessionCookie())
        .delete()
    )

  def mockSessionCookie(): WSCookie = {

    val cookieCrypto = app.injector.instanceOf[SessionCookieCrypto]
    val cookieBaker  = app.injector.instanceOf[SessionCookieBaker]

    val session = Session(
      Map(
        SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
        SessionKeys.authToken            -> "mock-bearer-token",
        SessionKeys.sessionId            -> "mock-sessionid"
      )
    )

    val encoded   = cookieBaker.encodeAsCookie(session)
    val encrypted = cookieCrypto.crypto.encrypt(PlainText(encoded.value))

    val cookie = encoded.copy(value = encrypted.value)

    DefaultWSCookie(
      name = cookie.name,
      value = cookie.value,
      path = Some(cookie.path),
      secure = cookie.secure,
      httpOnly = cookie.httpOnly
    )
  }
}
