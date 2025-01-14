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

import com.google.inject.Singleton
import config.SecureMessagingConfig
import connectors.CdsFileUploadConnector
import controllers.actions._

import javax.inject.Inject
import metrics.MetricIdentifiers._
import metrics.SfusMetrics
import models.{FileUpload, MRN}
import models.requests.VerifiedEmailRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import services.FileUploadAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{upload_your_files_confirmation, upload_your_files_receipt}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadYourFilesReceiptController @Inject()(
  authenticate: AuthAction,
  verifiedEmail: VerifiedEmailAction,
  cdsFileUploadConnector: CdsFileUploadConnector,
  metrics: SfusMetrics,
  uploadYourFilesReceipt: upload_your_files_receipt,
  uploadYourFilesConfirmation: upload_your_files_confirmation,
  answersService: FileUploadAnswersService,
  secureMessagingConfig: SecureMessagingConfig
)(implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen verifiedEmail).async { implicit req =>
    answersService.findByEori(req.eori).flatMap { maybeUserAnswers =>
      answersService.removeByEori(req.eori)

      val result = for {
        userAnswers <- getOrRedirect(maybeUserAnswers, routes.RootController.displayPage())
        fileUploads <- getOrRedirect(userAnswers.fileUploadResponse, routes.ErrorPageController.error())
      } yield {
        composeSuccessResult(fileUploads.uploads, userAnswers.mrn).map(Ok(_))
      }

      result match {
        case Right(successResult) => successResult
        case Left(errorResult)    => errorResult
      }
    }
  }

  private def composeSuccessResult(
    uploads: List[FileUpload],
    maybeMrn: Option[MRN]
  )(implicit hc: HeaderCarrier, req: VerifiedEmailRequest[AnyContent]): Future[Html] =
    addFilenames(uploads).map { uploads =>
      if (secureMessagingConfig.isSecureMessagingEnabled)
        uploadYourFilesConfirmation(uploads, maybeMrn, req.email)
      else
        uploadYourFilesReceipt(uploads, maybeMrn)
    }

  private def getOrRedirect[A](option: Option[A], errorAction: Call): Either[Future[Result], A] =
    option.fold[Either[Future[Result], A]](Left(Future.successful(Redirect(errorAction))))(Right(_))

  private def addFilenames(uploads: List[FileUpload])(implicit hc: HeaderCarrier): Future[List[FileUpload]] =
    Future
      .sequence(uploads.map { u =>
        val timer = metrics.startTimer(fetchNotificationMetric)
        cdsFileUploadConnector.getNotification(u.reference).map { notificationOpt =>
          timer.stop()
          notificationOpt.map { notification =>
            u.copy(filename = notification.filename)
          }
        }
      })
      .map(_.flatten)
}
