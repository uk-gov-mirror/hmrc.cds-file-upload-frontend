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

package controllers

import controllers.actions.{DataRetrievalAction, FakeActions, FileUploadResponseRequiredActionImpl}
import generators.Generators
import models.{File, FileUploadResponse, UploadRequest}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import pages.HowManyFilesUploadPage
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.upload_your_files

import scala.util.Random

class UploadYourFilesControllerSpec extends ControllerSpecBase with PropertyChecks with Generators with FakeActions {

  val responseGen: Gen[(File, FileUploadResponse)] =
    arbitrary[FileUploadResponse].map { response =>
      (Random.shuffle(response.files).head, response)
    }

  def controller(getData: DataRetrievalAction) =
    new UploadYourFilesController(
      messagesApi,
      new FakeAuthAction(),
      new FakeEORIAction(),
      getData,
      new FileUploadResponseRequiredActionImpl(),
      appConfig)

  def viewAsString(uploadRequest: UploadRequest, callbackUrl: String): String =
    upload_your_files(uploadRequest, callbackUrl)(fakeRequest, messages, appConfig).toString

  ".onPageLoad" should {

    "load the view" in {

      forAll(responseGen, arbitrary[CacheMap]) {
        case ((file, response), cacheMap) =>

          def nextRef(ref: String, refs: List[String]): String = {
            refs
              .sorted
              .partition(_ == ref)._2
              .headOption
              .getOrElse("reciepts")
          }

          val updatedCache = cacheMap.copy(data = cacheMap.data + (HowManyFilesUploadPage.Response.toString -> Json.toJson(response)))
          val result = controller(getCacheMap(updatedCache)).onPageLoad(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(file.uploadRequest, s"/upload/${nextRef(file.reference, response.files.map(_.reference))}")
      }
    }
  }
}
