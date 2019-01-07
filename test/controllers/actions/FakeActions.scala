/*
 * Copyright 2019 HM Revenue & Customs
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

import models.requests.{AuthenticatedRequest, EORIRequest, OptionalDataRequest, SignedInUser}
import models.UserAnswers
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class FakeAuthAction(result: SignedInUser) extends AuthAction {
  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    Future.successful(Right(AuthenticatedRequest(request, result)))
  }
}

class FakeEORIAction(eori: String) extends EORIAction {
  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, EORIRequest[A]]] =
    Future.successful(Right(EORIRequest[A](request, eori)))
}

class FakeDataRetrievalAction(cacheMap: Option[CacheMap]) extends DataRetrievalAction {
  override protected def transform[A](request: EORIRequest[A]): Future[OptionalDataRequest[A]] =
    Future.successful(OptionalDataRequest(request, cacheMap.map(UserAnswers(_))))
}