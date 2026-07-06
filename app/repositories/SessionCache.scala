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

import play.api.libs.json.{OWrites, Reads}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import com.google.inject.{ImplementedBy, Inject, Singleton}
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import config.{CryptoProvider, FrontendAppConfig}
import uk.gov.hmrc.mongo.cache.{CacheIdType, DataKey, MongoCacheRepository}
import uk.gov.hmrc.http.HeaderCarrier
import models.{SessionData, SessionDataEncrypted}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultSessionCache])
trait SessionCache {

  def get()(using
    hc: HeaderCarrier
  ): Future[Option[SessionData]]

  def store(sessionData: SessionData)(using
    hc: HeaderCarrier
  ): Future[Unit]
}

object HeaderCarrierCacheId extends CacheIdType[HeaderCarrier] {

  override def run: HeaderCarrier => String =
    _.sessionId
      .map(_.value)
      .getOrElse(throw new NoSessionException())

  class NoSessionException() extends Exception("Could not find sessionId")
}

@Singleton
class DefaultSessionCache @Inject() (
  mongoComponent: MongoComponent,
  timestampSupport: TimestampSupport,
  config: FrontendAppConfig,
  cryptoProvider: CryptoProvider
)(using
  ec: ExecutionContext
) extends MongoCacheRepository[HeaderCarrier](
      mongoComponent = mongoComponent,
      collectionName = "sessions",
      ttl = config.mongoDbTTL,
      timestampSupport = timestampSupport,
      cacheIdType = HeaderCarrierCacheId
    )
    with SessionCache {

  private given crypto: Encrypter & Decrypter =
    cryptoProvider.get()

  private given Reads[SessionDataEncrypted] =
    SessionDataEncrypted.reads(using crypto)

  private given OWrites[SessionDataEncrypted] =
    SessionDataEncrypted.writes(using crypto)  

  private val sessionDataKey: DataKey[SessionDataEncrypted] =
    DataKey[SessionDataEncrypted]("session-data")


  final def get()(using
    hc: HeaderCarrier
  ): Future[Option[SessionData]] =
    super.get[SessionDataEncrypted](hc)(sessionDataKey)
      .map(_.map(_.toSessionData))

  final def store(
    sessionData: SessionData
  )(using hc: HeaderCarrier): Future[Unit] =
    super
      .put(hc)(
        sessionDataKey,
        SessionDataEncrypted.fromSessionData(sessionData)
      )
      .map(_ => ())
}
