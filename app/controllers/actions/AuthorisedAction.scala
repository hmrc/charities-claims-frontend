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

import play.api.mvc.*
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.Results.*
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import models.requests.AuthorisedRequest

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[DefaultAuthorisedAction])
trait AuthorisedAction
    extends ActionBuilder[AuthorisedRequest, AnyContent]
    with ActionFunction[Request, AuthorisedRequest]

@Singleton
class DefaultAuthorisedAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends AuthorisedAction
    with AuthorisedFunctions {

  val logger: Logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: AuthorisedRequest[A] => Future[Result]): Future[Result] = {

    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.affinityGroup.and(Retrievals.credentials)) {
        case Some(AffinityGroup.Organisation) ~ Some(credentials) =>
          block(AuthorisedRequest(request, credentials.providerId))
        case Some(otherAffinityGroup) ~ _                         =>
          throw UnsupportedAffinityGroup(s"Unsupported affinity group $otherAffinityGroup found")
        case _ ~ None                                             =>
          throw UnsupportedAuthProvider("No credential provider id (internal id) found")
        case None ~ _                                             =>
          throw UnsupportedAffinityGroup("No affinity group found")
      }
      .recover { case _: AuthorisationException =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      }
  }
}
