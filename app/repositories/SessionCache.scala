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

import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import uk.gov.hmrc.mongo.cache.{CacheIdType, DataKey, MongoCacheRepository}
import uk.gov.hmrc.http.HeaderCarrier
import models.SessionData
import scala.util.Success

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultSessionCache])
trait SessionCache {

  def get()(implicit
    hc: HeaderCarrier
  ): Future[Option[SessionData]]

  def store(sessionData: SessionData)(implicit
    hc: HeaderCarrier
  ): Future[Unit]

  final def update[R](forceSessionCreation: Boolean)(
    update: SessionData => Future[SessionData]
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[SessionData] =
    get()
      .flatMap {
        case Some(sessionData) =>
          update(sessionData).andThen { case Success(updatedSessionData) =>
            if sessionData == updatedSessionData
            then Future.successful(())
            else store(updatedSessionData)
          }

        case None =>
          if forceSessionCreation then
            update(SessionData())
              .flatMap { updatedSessionData =>
                store(updatedSessionData)
                  .map(_ => updatedSessionData)
              }
          else Future.failed(new Exception("no session found in mongodb"))
      }

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
  config: FrontendAppConfig
)(implicit
  ec: ExecutionContext
) extends MongoCacheRepository[HeaderCarrier](
      mongoComponent = mongoComponent,
      collectionName = "sessions",
      ttl = config.mongoDbTTL,
      timestampSupport = timestampSupport,
      cacheIdType = HeaderCarrierCacheId
    )
    with SessionCache {

  val sessionDataKey: DataKey[SessionData] =
    DataKey[SessionData]("session-data")

  final def get()(implicit
    hc: HeaderCarrier
  ): Future[Option[SessionData]] =
    super.get[SessionData](hc)(sessionDataKey)

  final def store(
    sessionData: SessionData
  )(implicit hc: HeaderCarrier): Future[Unit] =
    super
      .put(hc)(sessionDataKey, sessionData)
      .map(_ => ())

}
