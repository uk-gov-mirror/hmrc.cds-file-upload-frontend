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

import base.SpecBase
import connectors.UpscanConnector
import models.{ContactDetails, UploadRequest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.Files.TemporaryFile

import scala.io.Source
import scala.util.Try

class UploadContactDetailsSpec extends SpecBase with MockitoSugar {

  val mockS3Connector = mock[UpscanConnector]

  val uploadContactDetails = new UploadContactDetails(mockS3Connector)

  "UploadContactDetails" must {

    "upload Contact Details to s3" in {

      val contactDetails = ContactDetails("name", "companyName", "phoneNumber", "email")
      val uploadRequest = UploadRequest("someUrl", Map("k" -> "v"))

      when(mockS3Connector.upload(any[UploadRequest], any[ContactDetails], any[String])).thenReturn(Try(202))

      uploadContactDetails.upload(contactDetails, uploadRequest)

      val fileCaptor: ArgumentCaptor[ContactDetails] = ArgumentCaptor.forClass(classOf[ContactDetails])
      val fileNameCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(mockS3Connector).upload(meq(uploadRequest), fileCaptor.capture(), fileNameCaptor.capture())

      val source = fileCaptor.getValue.toString
      source mustEqual contactDetails.toString
      val fileName = fileNameCaptor.getValue

      fileName must startWith("contact_details")
      fileName must endWith(".txt")
    }
  }
}
