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

package controllers

import models.SessionData
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.AnyContent

trait BaseController extends FrontendBaseController with I18nSupport {

  given sessionData(using req: DataRequest[?]): SessionData =
    req.sessionData

  extension [A](form: Form[A]) {
    def withDefault(optValue: Option[A]): Form[A] =
      optValue.map(form.fill).getOrElse(form)
  }

  def isWarning(using request: Request[AnyContent]): Boolean =
    request.flash.get("warning").contains("true")

  def warningAnswerBoolean(using request: Request[AnyContent]): Option[Boolean] =
    request.flash.get("warningAnswer").flatMap(_.toBooleanOption)

  def warningAnswerString(using request: Request[AnyContent]): Option[String] =
    request.flash.get("warningAnswer")

  def hadNoWarningShown(using request: Request[AnyContent]): Boolean =
    !request.body.asFormUrlEncoded.flatMap(_.get("warningShown").flatMap(_.headOption)).contains("true")

  extension (result: Result) {
    def withWarning(answer: String): Result =
      result.flashing("warning" -> "true", "warningAnswer" -> answer)
  }
}
