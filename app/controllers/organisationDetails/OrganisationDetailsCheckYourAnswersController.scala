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

package controllers.organisationDetails

import com.google.inject.Inject
import controllers.actions.Actions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.OrganisationDetailsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class OrganisationDetailsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  claimsService: ClaimsService,
  val controllerComponents: MessagesControllerComponents,
  view: OrganisationDetailsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    val previousAnswers = request.sessionData.organisationDetailsAnswers
    Future.successful(Ok(view(previousAnswers)))
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
//    val checkAnswers = Some(request.sessionData.organisationDetailsAnswers.hasOrganisationDetailsCompleteAnswers)
    val checkAnswers = false
    if checkAnswers
    then
      claimsService.save.map { _ =>
        Redirect(
          // TODO: replace with correct url when ready
          "next-page-after-organisation-details-check-your-answers"
        )
      }
    else Future.successful(Redirect(routes.OrganisationDetailsIncompleteAnswersController.onPageLoad))
  }
}
