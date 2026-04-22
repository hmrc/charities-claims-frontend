/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.giftAidSmallDonationsScheme

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import models.{GiftAidSmallDonationsSchemeDonationDetailsAnswers, SessionData}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.GasdsDonationDetailsIncompleteAnswersView

class GasdsDonationDetailsIncompleteAnswersController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: GasdsDonationDetailsIncompleteAnswersView
) extends BaseController {

  def onPageLoad: Action[AnyContent] =
    actions.authAndGetDataWithGuard(SessionData.isRepaymentClaimDetailsComplete) { implicit request =>
      val missingFields =
        GiftAidSmallDonationsSchemeDonationDetailsAnswers.getMissingFields(
          request.sessionData.giftAidSmallDonationsSchemeDonationDetailsAnswers
        )
      Ok(
        view(
          "/charities-claims/check-your-GASDS-donation-details", // TODO: update when S2.16 route is available
          missingFields
        )
      )
    }
}
