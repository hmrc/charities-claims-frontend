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

package services

import com.google.inject.ImplementedBy
import models.SessionData
import models.requests.DataRequest
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

@ImplementedBy(classOf[SaveServiceImpl])
trait SaveService {
  def save(sessionData: SessionData)(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit]
}

class SaveServiceImpl @Inject() (cache: SessionCache) extends SaveService {
  override def save(updatedSessionData: SessionData)(using request: DataRequest[?], hc: HeaderCarrier): Future[Unit] =
    if updatedSessionData == request.sessionData
    then Future.successful(())
    else cache.store(updatedSessionData)
}
