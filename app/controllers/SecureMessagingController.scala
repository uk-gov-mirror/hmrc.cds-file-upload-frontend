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

package controllers

import connectors.SecureMessageFrontendConnector
import controllers.actions.{AuthAction, MessageFilterAction, SecureMessagingFeatureAction, VerifiedEmailAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.messaging.{inbox_wrapper, partial_wrapper}
import java.net.URLEncoder.encode

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SecureMessagingController @Inject()(
  authenticate: AuthAction,
  verifiedEmail: VerifiedEmailAction,
  secureMessagingFeatureAction: SecureMessagingFeatureAction,
  messageFilterAction: MessageFilterAction,
  messageConnector: SecureMessageFrontendConnector,
  mcc: MessagesControllerComponents,
  inbox_wrapper: inbox_wrapper,
  partial_wrapper: partial_wrapper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val actions = authenticate andThen verifiedEmail andThen secureMessagingFeatureAction andThen messageFilterAction

  val displayInbox: Action[AnyContent] = actions.async { implicit request =>
    messageConnector
      .retrieveInboxPartial(request.secureMessageAnswers.eori, request.secureMessageAnswers.filter)
      .map { partial =>
        Ok(inbox_wrapper(HtmlFormat.raw(partial.body)))
      }
  }

  def displayConversation(client: String, conversationId: String): Action[AnyContent] = actions.async { implicit request =>
    messageConnector
      .retrieveConversationPartial(client, conversationId)
      .map(
        partial =>
          Ok(
            partial_wrapper(
              HtmlFormat.raw(partial.body),
              "conversation.heading",
              defineUploadLink(routes.SecureMessagingController.displayConversation(client, conversationId).url),
              Some(routes.SecureMessagingController.displayInbox.url)
            )
        )
      )
  }

  def displayReplyResult(client: String, conversationId: String): Action[AnyContent] = actions.async { implicit request =>
    messageConnector
      .retrieveReplyResult(client, conversationId)
      .map(
        partial =>
          Ok(
            partial_wrapper(
              HtmlFormat.raw(partial.body),
              "replyResult.heading",
              defineUploadLink(routes.SecureMessagingController.displayReplyResult(client, conversationId).url)
            )
        )
      )
  }

  def submitReply(client: String, conversationId: String): Action[AnyContent] = actions.async { implicit request =>
    val formData = request.body.asFormUrlEncoded.getOrElse(Map.empty)

    messageConnector
      .submitReply(client, conversationId, formData)
      .map { maybeErrorPartial =>
        maybeErrorPartial match {
          case None => Redirect(routes.SecureMessagingController.displayReplyResult(client, conversationId))
          case Some(partial) =>
            Ok(
              partial_wrapper(
                HtmlFormat.raw(partial.body),
                "replyResult.heading",
                defineUploadLink(routes.SecureMessagingController.displayConversation(client, conversationId).url),
                Some(routes.SecureMessagingController.displayInbox.url)
              )
            )
        }
      }
  }

  private def defineUploadLink(refererUrl: String) = {
    val encodedRefererUrl = encode(refererUrl, "UTF-8")
    routes.MrnEntryController.onPageLoad(Some(encodedRefererUrl)).url
  }
}
