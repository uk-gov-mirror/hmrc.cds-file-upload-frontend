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

import connectors.CdsFileUploadConnector
import controllers.ControllerSpecBase
import generators.SignedInUserGen
import models.{EORI, VerifiedEmailAddress}
import models.requests.{AuthenticatedRequest, EORIRequest, SignedInUser, VerifiedEmailRequest}
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Inside}
import play.api.{Configuration, Environment}
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest

import java.time.ZonedDateTime
import scala.concurrent.Future

class VerifiedEmailActionSpec extends ControllerSpecBase with SignedInUserGen with BeforeAndAfterEach with Inside {

  lazy val conf = instanceOf[Configuration]
  lazy val env = instanceOf[Environment]
  lazy val user = mock[SignedInUser]
  lazy val backendConnector = mock[CdsFileUploadConnector]

  lazy val action = new ActionTestWrapper(backendConnector, mcc)

  lazy val sampleEmailAddress = "example@example.com"
  lazy val sampleEori = EORI("12345")
  lazy val verifiedEmail = VerifiedEmailAddress(sampleEmailAddress, ZonedDateTime.now())
  lazy val authenticatedRequest = AuthenticatedRequest[Any](FakeRequest("GET", "requestPath"), user)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(backendConnector)
  }

  "VerifiedEmailAction" should {
    "return a VerifiedEmailRequest" when {
      "user has a verified email address" in {
        when(backendConnector.getVerifiedEmailAddress(meq(sampleEori))(any())).thenReturn(Future.successful(Some(verifiedEmail)))

        val request = EORIRequest(authenticatedRequest, sampleEori.value)

        whenReady(action.testRefine(request)) { result =>
          result mustBe Right(VerifiedEmailRequest(request, sampleEmailAddress))
        }
      }
    }

    "return a redirection Result" when {
      "user has no verified email address" in {
        when(backendConnector.getVerifiedEmailAddress(meq(sampleEori))(any())).thenReturn(Future.successful(None))

        val request = EORIRequest(authenticatedRequest, sampleEori.value)

        whenReady(action.testRefine(request)) { result =>
          result must be('left)
        }
      }
    }

    "propagate exception" when {
      "connector fails" in {
        when(backendConnector.getVerifiedEmailAddress(meq(sampleEori))(any())).thenReturn(Future.failed(new Exception("Some unhappy response")))

        val request = EORIRequest(authenticatedRequest, sampleEori.value)
        val result = action.testRefine(request)

        assert(result.failed.futureValue.isInstanceOf[Exception])
      }
    }
  }

  class ActionTestWrapper(backendConnector: CdsFileUploadConnector, mcc: MessagesControllerComponents)
      extends VerifiedEmailActionImpl(backendConnector, mcc) {
    def testRefine[A](request: EORIRequest[A]): Future[Either[Result, VerifiedEmailRequest[A]]] =
      refine(request)
  }
}
