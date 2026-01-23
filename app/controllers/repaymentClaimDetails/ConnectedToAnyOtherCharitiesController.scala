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

package controllers.repaymentClaimDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.RepaymentClaimDetailsAnswers
import controllers.repaymentClaimDetails.routes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.data.Form
import services.SaveService
import views.html.ConnectedToAnyOtherCharitiesView

import scala.concurrent.{ExecutionContext, Future}

class ConnectedToAnyOtherCharitiesController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: ConnectedToAnyOtherCharitiesView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService
)(using ec: ExecutionContext)
    extends BaseController {
  val form: Form[Boolean] = formProvider("connectedToAnyOtherCharities.error.required")

  def onPageLoad: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    if RepaymentClaimDetailsAnswers.getClaimingUnderGiftAidSmallDonationsScheme.contains(true) then {
      val previousAnswer = RepaymentClaimDetailsAnswers.getConnectedToAnyOtherCharities
      Future.successful(Ok(view(form.withDefault(previousAnswer))))
    } else {
      Future.successful(Redirect(controllers.routes.PageNotFoundController.onPageLoad))
    }
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          saveService
            .save(RepaymentClaimDetailsAnswers.setConnectedToAnyOtherCharities(value))
            .map(_ =>
              Redirect(
                routes.ConnectedToAnyOtherCharitiesController.onPageLoad
              )
            )
      )
  }
}
