/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.collection.JavaConverters._

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.twirl.api.{Html, HtmlFormat}

trait DomAssertions extends SpecBase {

  def asDocument(string: String) = Jsoup.parse(string)

  def asDocument(html: Html): Document = asDocument(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String, args: Any*) =
    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey, args))

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String) = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertContainsValue(doc: Document, cssSelector: String, expectedValue: String) = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "").contains(expectedValue))
  }

  def assertPageTitleEquals(doc: Document, expectedMessage: String) =
    doc.title mustBe expectedMessage

  def assertH1EqualsMessage(doc: Document, expectedMessageKey: String, args: Any*) = {
    val headers = doc.getElementsByTag("h1")
    headers.size mustBe 1
    headers.first.text.replaceAll("\u00a0", " ") mustBe messages(expectedMessageKey, args: _*).replaceAll("&nbsp;", " ")
  }

  def assertContainsText(doc: Document, text: String) =
    assert(doc.text().contains(Html(text).body), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertContainsLink(doc: Document, text: String, href: String) = {
    val anchors = doc.getElementsByTag("a").asScala
    val exists = anchors.exists(a => a.text() == text && a.attr("href") == href)
    assert(exists, s"\n\nanchor with text $text and href $href was not rendered on the page.\n")
  }

  def assertContainsMessages(doc: Document, expectedMessageKeys: String*) =
    for (key <- expectedMessageKeys) assertContainsMessage(doc, key)

  def assertContainsMessage(doc: Document, messageKey: String, args: Any*) =
    assertContainsText(doc, messages(messageKey, args: _*))

  def assertRenderedById(doc: Document, id: String) =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String) =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String) =
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String) =
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")

  def assertContainsLabel(doc: Document, forElement: String, expectedText: String, expectedHintText: List[String] = List()) = {
    val labels = doc.getElementsByAttributeValue("for", forElement)
    assert(labels.size == 1, s"\n\nLabel for $forElement was not rendered on the page.")
    val label = labels.first
    assert(label.text().contains(expectedText), s"\n\nLabel for $forElement was not $expectedText")

    expectedHintText.foreach(
      msg =>
        assert(
          label.getElementsByClass("form-hint").first.text.contains(msg),
          s"\n\nLabel for $forElement did not contain hint text $expectedHintText"
      )
    )
  }

  def assertElementHasClass(doc: Document, id: String, expectedClass: String) =
    assert(doc.getElementById(id).hasClass(expectedClass), s"\n\nElement $id does not have class $expectedClass")

  def assertContainsRadioButton(doc: Document, id: String, name: String, value: String, isChecked: Boolean) = {
    assertRenderedById(doc, id)
    val radio = doc.getElementById(id)
    assert(radio.attr("name") == name, s"\n\nElement $id does not have name $name")
    assert(radio.attr("value") == value, s"\n\nElement $id does not have value $value")
    isChecked match {
      case true => assert(radio.attr("checked") == "checked", s"\n\nElement $id is not checked")
      case _    => assert(!radio.hasAttr("checked") && radio.attr("checked") != "checked", s"\n\nElement $id is checked")
    }
  }

  def assertSignoutLinkIsIncluded(view: HtmlFormat.Appendable): Assertion =
    assertContainsLink(asDocument(view), messages("signOut.link"), controllers.routes.SignOutController.signOut.url)

  def assertBulletList(doc: Document, bulletText: String*) = {
    val bullets = doc.select("li").asScala

    val itemsWithExpected = bullets.zip(bulletText)

    itemsWithExpected.foreach { itemWithExpected =>
      val (item, expectedContent) = itemWithExpected
      assert(item.text() == expectedContent)
    }
  }
}
