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
import play.twirl.api.Html
import play.api.libs.json.*
import repositories.SessionCache
import models.SessionData
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.util.control.NonFatal
import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@Singleton
class SessionDataController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  sessionCache: SessionCache
)(using ExecutionContext)
    extends FrontendBaseController {

  private val showSessionDataAction   = routes.SessionDataController.onPageLoad
  private val submitSessionDataAction = routes.SessionDataController.onSubmit

  val onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    sessionCache
      .get()
      .map(
        _.fold(Ok(renderPage("", Some("session data not found"))))(sessionData =>
          Ok(renderPage(Json.prettyPrint(Json.toJson(sessionData))))
        )
      )
  }

  val onSubmit: Action[AnyContent] = Action.async { implicit request =>
    request.body.asFormUrlEncoded
      .flatMap(_.get("sessionData").flatMap(_.headOption))
      .fold(Future.successful(Redirect(showSessionDataAction))) { sessionData =>
        try {
          val json = Json.parse(sessionData)
          implicitly[Reads[SessionData]].reads(json) match {
            case JsError(errors)       =>
              Future.successful(
                Ok(
                  renderPage(
                    sessionData,
                    Some(
                      errors
                        .map { case (path, es) =>
                          s"json parsing error at <span>$path<span>: <span>${es.map(_.message).mkString(", ")}</span>"
                        }
                        .mkString("<ul><li>", "<li>", "<ul>")
                    )
                  )
                )
              )
            case JsSuccess(session, _) =>
              sessionCache
                .store(session)
                .map(_ => Redirect(showSessionDataAction))
          }
        } catch {
          case NonFatal(e) =>
            Future.successful(Ok(renderPage(sessionData, Some(e.getMessage()))))
        }
      }

  }

  def renderPage(sessionData: String, error: Option[String] = None): Html = Html(
    s"""
    |  <html>
    |  <head>
    |  <style>
    |  body {font-family: monospace;}
    |  form {display: flex; flex-direction: column; width: 100%; height: 100%;}
    |  div {marging: 0.2em; padding: 0.5em;}
    |  textarea {flex: 1; border: 0; margin: 1em 0;}
    |  button {font-size: 1em}
    |  .error {color: red; font-weight: bold; font-size: 1em;}
    |  </style> 
    |  </head>
    |  <body>
    |    ${error.map(e => s"""<div class="error">$e</div>""".stripMargin).getOrElse("")}
    |    <form method="POST" action="${submitSessionDataAction.url}">
    |       <button>Save</button>
    |       <textarea id="sessionData" name="sessionData">$sessionData</textarea>
    |       <button id="save">Save</button>
    |    </form>
    |  </body>
    |  </html>
    """.stripMargin
  )
}
