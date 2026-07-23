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

import services.ScheduleUploadNotFoundException
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import models.ValidationType.{CommunityBuildings, ConnectedCharities, GiftAid, OtherIncome}
import views.html.ErrorView
import uk.gov.hmrc.mdc.RequestMdc
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import play.api.Logger
import models.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@Singleton
class ErrorHandler @Inject() (
  val messagesApi: MessagesApi,
  view: ErrorView
)(implicit override protected val ec: ExecutionContext)
    extends FrontendErrorHandler
    with I18nSupport {

  private val logger = Logger(getClass)

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    RequestMdc.initMdc(request.id)
    resolveCustomError(exception)
      .fold(ex => super.onServerError(request, ex), identity)
  }

  def resolveCustomError(ex: Throwable): Either[Throwable, Future[Result]] =
    ex match {
      case _: UpdatedByAnotherUserException =>
        logger.warn(ex.getMessage)
        Right(
          Future.successful(
            Redirect(controllers.organisationDetails.routes.CannotViewOrManageClaimController.onPageLoad)
          )
        )

      case _: UnsubmittedClaimsLimitExceededException =>
        logger.warn(ex.getMessage)
        Right(
          Future.successful(
            Redirect(controllers.routes.Warning11MaxClaimsReachedController.onPageLoad)
          )
        )

      case _: OrganisationClaimAlreadyInProgressException =>
        logger.warn(ex.getMessage)
        Right(
          Future.successful(
            Redirect(controllers.routes.CannotProgressThisClaimController.onPageLoad)
          )
        )

      case _: UnsubmittedClaimExistsForCharityException =>
        logger.warn(ex.getMessage)
        Right(
          Future.successful(
            Redirect(controllers.routes.ClaimCannotBeSavedController.onPageLoad)
          )
        )

      case value: ScheduleUploadNotFoundException =>
        logger.warn(value.getMessage)
        value.validationType match {
          case GiftAid =>
            Right(
              Future.successful(Redirect(controllers.giftAidSchedule.routes.UploadGiftAidScheduleController.onPageLoad))
            )

          case CommunityBuildings =>
            Right(
              Future.successful(
                Redirect(
                  controllers.communityBuildingsSchedule.routes.UploadCommunityBuildingsScheduleController.onPageLoad
                )
              )
            )
          case OtherIncome        =>
            Right(
              Future.successful(
                Redirect(controllers.otherIncomeSchedule.routes.UploadOtherIncomeScheduleController.onPageLoad)
              )
            )
          case ConnectedCharities =>
            Right(
              Future.successful(
                Redirect(
                  controllers.connectedCharitiesSchedule.routes.UploadConnectedCharitiesScheduleController.onPageLoad
                )
              )
            )
        }
      case exception                              =>
        Left(exception)
    }

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    rh: RequestHeader
  ): Future[Html] = {
    val request: Request[String] = Request(rh, "")
    Future.successful(view(pageTitle, heading, message)(using request))
  }
}
