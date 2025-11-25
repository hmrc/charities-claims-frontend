/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.testonly

import play.api.mvc.*
import views.html.testonly.AuthLoginStubView
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.*
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.binders.UnsafePermitAll
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import javax.inject.Singleton
import config.FrontendAppConfig

@Singleton
class AuthLoginStubController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  authLoginStubView: AuthLoginStubView,
  config: FrontendAppConfig
) extends FrontendBaseController {

  val defaultUserId         = "1234567890"
  val postAction: Call      = Call("POST", config.authLoginStubSignInUrl)
  val startClaimUrl: String = s"${config.baseUrl}${controllers.routes.StartController.start}"
  val redirectPolicy        = UnsafePermitAll

  final val onPageLoad: Action[AnyContent] =
    Action { implicit request =>

      val userId = request.getQueryString("userId").getOrElse(defaultUserId)

      val continueUrl =
        request
          .getQueryString("continue")
          .map(RedirectUrl.apply)
          .map(_.get(UnsafePermitAll).url)
          .getOrElse(startClaimUrl)

      Ok(authLoginStubView(userId, continueUrl, postAction)).withNewSession
    }

}
