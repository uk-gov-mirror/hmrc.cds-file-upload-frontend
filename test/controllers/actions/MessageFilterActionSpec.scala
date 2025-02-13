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

package controllers.actions

import controllers.Assets.Redirect
import controllers.{routes, ControllerSpecBase}
import models.{ExportMessages, SecureMessageAnswers}
import models.requests.{AuthenticatedRequest, MessageFilterRequest, VerifiedEmailRequest}
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import play.api.mvc.Result
import services.SecureMessageAnswersService
import testdata.CommonTestData._

import scala.concurrent.Future

class MessageFilterActionSpec extends ControllerSpecBase {

  private val answersService: SecureMessageAnswersService = mock[SecureMessageAnswersService]
  private val action: ActionTestWrapper = new ActionTestWrapper(answersService)

  private val answers = SecureMessageAnswers(eori, ExportMessages)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(answersService)
  }

  override def afterEach(): Unit = {
    reset(answersService)
    super.afterEach()
  }

  "MessageFilterAction" when {
    "the repository finds the user's filter selection" must {
      "build a SecureMessageAnswers object and add it to the MessageFilterRequest" in {
        when(answersService.findByEori(eqTo(eori))) thenReturn Future.successful(Some(answers))
        val request = VerifiedEmailRequest(AuthenticatedRequest(fakeRequest, signedInUser), verifiedEmail)

        val result = action.callRefine(request).futureValue

        result.isRight mustBe true
        result.right.get.secureMessageAnswers mustBe answers
      }
    }

    "the repository does not find the user's filter selection" must {
      "redirect the user to the message filter choice page" in {
        when(answersService.findByEori(eqTo(eori_2))) thenReturn Future.successful(None)
        val request = VerifiedEmailRequest(AuthenticatedRequest(fakeRequest, signedInUser.copy(eori = eori_2)), verifiedEmail)

        val result = action.callRefine(request).futureValue

        result.isLeft mustBe true
        result.left.get mustBe Redirect(routes.InboxChoiceController.onPageLoad())
      }
    }
  }

  class ActionTestWrapper(answersService: SecureMessageAnswersService) extends MessageFilterActionImpl(answersService, mcc) {
    def callRefine[A](request: VerifiedEmailRequest[A]): Future[Either[Result, MessageFilterRequest[A]]] = refine(request)
  }
}
