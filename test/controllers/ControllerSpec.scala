/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.test._
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import util.BaseSpec
import play.api.http._
import play.api.Application
import play.api.data.Form
import play.api.mvc.{Security => _, _}
import com.softwaremill.diffx.scalatest.DiffShouldMatcher

trait ControllerSpec
    extends BaseSpec
    with DefaultAwaitTimeout
    with HttpVerbs
    with Writeables
    with HeaderNames
    with Status
    with PlayRunners
    with RouteInvokers
    with ResultExtractors
    with DiffShouldMatcher {

  val baseUrl = "/charities-claims"

  val testOnwardRoute: Call = Call("GET", "/foo")

  override protected def applicationBuilder: GuiceApplicationBuilder =
    super.applicationBuilder
      .overrides(
        additionalBindings *
      )
      .configure("play.filters.csp.nonce.enabled" -> false)

  protected val additionalBindings: List[GuiceableModule] = List()

  def runningApplication[T](block: Application => T): T =
    running(_ => applicationBuilder)(block)

  def formData[A](form: Form[A], data: A): List[(String, String)] = form.fill(data).data.toList
}
