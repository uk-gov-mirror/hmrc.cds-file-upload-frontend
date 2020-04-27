/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.Cache
import controllers.ControllerSpecBase
import generators.SignedInUserGen
import models.requests.{AuthenticatedRequest, SignedInUser}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CacheDeleteActionSpec extends ControllerSpecBase with SignedInUserGen {

  lazy val user = mock[SignedInUser]
  lazy val cache = mock[Cache]

  def cacheDeleteAction = new CacheDeleteActionImpl(cache)

  "CacheDeleteAction" should {

    "delegate to cache" in {

      when(cache.remove()(any())) thenReturn Future.successful(HttpResponse(200))

      val request = AuthenticatedRequest(fakeRequest, user)

      cacheDeleteAction.filter(request)

      verify(cache).remove()(any())
    }
  }

}
