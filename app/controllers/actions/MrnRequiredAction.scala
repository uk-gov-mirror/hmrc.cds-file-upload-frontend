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

import controllers.routes
import models.requests.{ContactDetailsRequest, MrnRequest}
import pages.MrnEntryPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.Future

class MrnRequiredAction extends ActionRefiner[ContactDetailsRequest, MrnRequest] {

  private val onError = Redirect(routes.SessionExpiredController.onPageLoad())

  override protected def refine[A](request: ContactDetailsRequest[A]): Future[Either[Result, MrnRequest[A]]] = {
    Future.successful(request.userAnswers.get(MrnEntryPage).map(mrn => MrnRequest(request, request.userAnswers, mrn)).toRight(onError))
  }
}