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

import com.google.inject.ImplementedBy
import models.SessionData
import models.requests.{AuthorisedRequest, OptionalDataRequest}
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultDataRetrievalAction])
trait DataRetrievalAction extends ActionRefiner[AuthorisedRequest, OptionalDataRequest]

class DefaultDataRetrievalAction @Inject() (
  cache: SessionCache
)(using val executionContext: ExecutionContext)
    extends DataRetrievalAction {

  override protected def refine[A](
    request: AuthorisedRequest[A]
  ): Future[Either[Result, OptionalDataRequest[A]]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.underlying, request.underlying.session)
    cache
      .get()
      .map {
        case None              =>
          Right(
            OptionalDataRequest(
              request,
              request.userId,
              Some(SessionData(None)) // todo will need changing once we fetch from backend
            )
          )
        case Some(sessionData) =>
          Right(
            OptionalDataRequest(
              request,
              request.userId,
              Some(sessionData)
            )
          )
      }
  }
}
