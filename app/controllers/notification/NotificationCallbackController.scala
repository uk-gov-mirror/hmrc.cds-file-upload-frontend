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

package controllers.notification

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.Action
import services.NotificationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class NotificationCallbackController @Inject()(notificationService: NotificationService)(implicit ec: ExecutionContext) extends FrontendController {

  def onNotify = Action.async(parse.xml) { implicit req =>
    val notification = req.body

    notificationService.save(notification) map {
      case Right(_) => Accepted
      case Left(exception) =>
        Logger.error(s"Failed to save invalid notification: $notification", exception)
        BadRequest
    } recover {
      case exception: Throwable =>
        Logger.error(s"Failed to save notification: $notification", exception)
        BadRequest
    }
  }
}
