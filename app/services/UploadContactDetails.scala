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

package services

import java.io.{File, PrintWriter}
import java.util.UUID

import com.google.inject.Singleton
import connectors.UpscanS3Connector
import javax.inject.Inject
import models.{ContactDetails, UploadRequest}
import play.api.libs.Files.TemporaryFile

import scala.concurrent.Future

@Singleton
class UploadContactDetails @Inject()(upscanConnector: UpscanS3Connector) {

  def fileName = s"contact_details_${UUID.randomUUID().toString}.txt"

  def apply(contactDetails: ContactDetails, uploadRequest: UploadRequest): Future[Unit] = upscanConnector.upload(uploadRequest, toFile(contactDetails), fileName)

  def toFile(contactDetails: ContactDetails): TemporaryFile = {
    val uploadFile = File.createTempFile("cds-file-upload-ui", "")

    new PrintWriter(uploadFile) {
      try {
        write(contactDetails.toString())
      } finally {
        close()
      }
    }

    TemporaryFile(uploadFile)
  }
}