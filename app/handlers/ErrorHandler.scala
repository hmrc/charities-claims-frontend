/*
 * Copyright 2022 HM Revenue & Customs
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

package handlers

import play.api.mvc.{Request, RequestHeader}
import play.twirl.api.Html
import views.html.ErrorView
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import models.UpdatedByAnotherUserException
import play.api.mvc.Results.BadRequest
import play.api.Logger

@Singleton
class ErrorHandler @Inject() (
  val messagesApi: MessagesApi,
  view: ErrorView
)(implicit override protected val ec: ExecutionContext)
    extends FrontendErrorHandler
    with I18nSupport {

  private val logger = Logger(getClass)

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] =
    ex match {
      case UpdatedByAnotherUserException(message) =>
        logger.error(message)
        Future.successful(
          BadRequest("Screen WRN4-You can not manage this claim")
          // Redirect(controllers.organisationDetails.routes.CannotViewOrManageClaimController.onPageLoad)
        ) // TODO: Replace with the correct path to CannotViewOrManageClaim page
      case _                                      =>
        super.resolveError(rh, ex)
    }

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    rh: RequestHeader
  ): Future[Html] = {
    val request: Request[String] = Request(rh, "")
    Future.successful(view(pageTitle, heading, message)(using request))
  }
}
