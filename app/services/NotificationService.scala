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

import connectors.DataCacheConnector
import javax.inject.Inject
import play.api.libs.json.JsString
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import scala.xml.Elem

class NotificationService @Inject()(cache: DataCacheConnector) {

  def save(notification: Elem)(implicit hc: HeaderCarrier): Future[CacheMap] = {
    val fileReference = (notification \\ "FileReference").text
    val outcome = (notification \\ "Outcome").text

    println("&" * 100)
    println("saving notification to cache for file ref: " + fileReference)
    println("&" * 100)
    cache.save(CacheMap(fileReference, Map("outcome" -> JsString(outcome))))}
}
