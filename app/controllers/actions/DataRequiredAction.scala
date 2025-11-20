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
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.Results.*
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultDataRequiredAction])
trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]

class DefaultDataRequiredAction @Inject() ()(using val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](
    request: OptionalDataRequest[A]
  ): Future[Either[Result, DataRequest[A]]] =
    request.sessionData match {
      case None                    =>
        Future.successful(Left(InternalServerError("session data missing")))
      case Some(data: SessionData) =>
        Future.successful(
          Right(
            DataRequest(
              request.underlying,
              request.userId,
              data
            )
          )
        )
    }
}
