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

import controllers.routes
import utils.FakeRequestCSRFSupport._
import views.behaviours.ViewBehaviours
import views.html.start

class StartSpec extends DomAssertions with ViewBehaviours {

  val page = instanceOf[start]

  val view = page()(fakeRequest.withCSRFToken, messages)

  val messageKeyPrefix = "startPage"

  "Start Page" must {
    behave like normalPage(() => view, messageKeyPrefix, "paragraph2")

    "have a start button with correct link" in {
      val doc = asDocument(view)
      val expectedLink = routes.StartController.onStart().url

      val form = doc.getElementsByTag("form").first()
      form.attr("action") mustBe expectedLink

      form.getElementsByTag("button").text() mustBe messages("common.button.startNow")
    }

    "not include the 'Back' link" in {
      assertBackLinkIsNotIncluded(asDocument(view))
    }

    "have paragraph3 with bold text" in {
      val paragraph3 = messages("startPage.paragraph3", messages("startPage.paragraph3.bold"))
      val doc = asDocument(view)

      assertContainsText(doc, paragraph3)
    }

    "have paragraph4 with bold text" in {
      val paragraph4 = messages("startPage.paragraph4", messages("startPage.paragraph4.bold"))
      val doc = asDocument(view)

      assertContainsText(doc, paragraph4)
    }
  }
}
