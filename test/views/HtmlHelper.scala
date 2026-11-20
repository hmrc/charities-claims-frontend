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

package views

import config.Constants.Css
import play.twirl.api.Html
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.jsoup.nodes.Element

import scala.jdk.CollectionConverters.IteratorHasAsScala

trait HtmlHelper {

  def mainContent(html: Html): Element =
    Jsoup.parse(html.body).getElementsByAttributeValue("data-testid", "main-content").first()

  def title(html: Html): String = Jsoup.parse(html.body).title()

  def h1(html: Html): String       = mainContent(html).getElementsByTag("h1").first().text()
  def h2(html: Html): List[String] =
    mainContent(html).getElementsByTag("h2").iterator().asScala.map(_.text()).toList

  def elementText(html: Html): List[String] =
    p(html) ++ li(html) ++ labels(html) ++ legend(html) ++ h2(html)

  def p(html: Html): List[String] =
    mainContent(html).getElementsByTag("p").iterator().asScala.map(_.text()).toList

  def li(html: Html): List[String] =
    mainContent(html).getElementsByTag("li").iterator().asScala.map(_.text()).toList

  def legend(html: Html): List[String] =
    mainContent(html).getElementsByTag("legend").iterator().asScala.map(_.text()).toList

  def span(html: Html): List[String] =
    mainContent(html).getElementsByTag("span").iterator().asScala.map(_.text()).toList

  def panel(html: Html): Panel = {
    val parent    = mainContent(html).getElementsByClass(Css.panelCssClass).first()
    val title     = parent.getElementsByClass(Css.panelTitleCssClass).first()
    val maybeBody = parent.getElementsByClass(Css.panelBodyCssClass).iterator().asScala.toList.headOption
    Panel(title, maybeBody)
  }

  def dividers(html: Html): List[Element] =
    mainContent(html).getElementsByClass(Css.radiosDividerCssClass).iterator().asScala.toList

  def labels(html: Html): List[String] =
    mainContent(html).getElementsByTag("label").iterator().asScala.map(_.text()).toList

  def inputLabel(html: Html)(name: String): Element =
    mainContent(html).selectFirst(s"label[for=$name]")

  def inputByClass(html: Html)(cssClass: String): Element =
    mainContent(html).select(s"input.$cssClass").first()

  def upload(html: Html): Element = inputByClass(html)(Css.govukFileUploadCssClass)

  def inputHint(html: Html)(name: String): Element =
    mainContent(html).getElementById(s"$name-hint")

  def tr(html: Html): List[List[String]] =
    mainContent(html)
      .getElementsByTag("tr")
      .iterator()
      .asScala
      .toList
      .map(
        _.getElementsByTag("td").iterator().asScala.toList.map(_.text())
      )

  def inset(html: Html): Element =
    mainContent(html).getElementsByClass(Css.insetTextCssClass).first()

  def details(html: Html): Element =
    mainContent(html).selectFirst("details.govuk-details")

  def buttons(html: Html): Elements =
    mainContent(html).getElementsByTag("button")

  def button(html: Html): Element = buttons(html).first()

  def errorSummary(html: Html): Elements =
    mainContent(html).getElementsByClass(Css.errorSummaryCssClass)

  def errorMessage(html: Html): Elements =
    mainContent(html).getElementsByClass(Css.errorMessageCssClass)

  def summaryListKeys(html: Html): List[String] =
    mainContent(html).getElementsByClass(Css.summaryListKeyCssClass).iterator().asScala.toList.map(_.text())

  def summaryListValues(html: Html): List[String] =
    mainContent(html).getElementsByClass(Css.summaryListValueCssClass).iterator().asScala.toList.map(_.text())

  def summaryListRows(html: Html): List[Element] =
    mainContent(html).getElementsByClass(Css.summaryListRowCssClass).iterator().asScala.toList

  def radioListRows(html: Html): List[Element] =
    mainContent(html).getElementsByClass(Css.radiosItemCssClass).iterator().asScala.toList

  def paginationLabel(html: Html): Option[String] =
    Option(mainContent(html).getElementById("pagination-label")).map(_.text())

  def date(html: Html)(id: String): DateElements = {
    val elements = mainContent(html).selectFirst(s"div.govuk-date-input")
    DateElements(
      elements.selectFirst(s"label[for=$id.day]"),
      elements.selectFirst(s"label[for=$id.month]"),
      elements.selectFirst(s"label[for=$id.year]")
    )
  }

  def textAreas(html: Html)(name: String): Elements =
    mainContent(html).select(s"textarea[name=$name]")

  case class DateElements(day: Element, month: Element, year: Element)
  case class Panel(title: Element, body: Option[Element])
}
