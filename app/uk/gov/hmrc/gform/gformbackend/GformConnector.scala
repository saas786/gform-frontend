/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.gform.gformbackend

import play.api.Logger
import uk.gov.hmrc.gform.gformbackend.model._
import uk.gov.hmrc.gform.models.{ SaveResult, UserId }
import uk.gov.hmrc.gform.wshttp.WSHttp
import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpResponse, NotFoundException }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

class GformConnector(ws: WSHttp, baseUrl: String) {

  //TODO: remove userId since this information will be passed using HeaderCarrier
  def newForm(formTypeId: FormTypeId, userId: UserId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Form] =
    ws.POSTEmpty[Form](s"$baseUrl/new-form/${formTypeId.value}/${userId.value}")

  def getFormTemplate(formTypeId: FormTypeId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FormTemplate] =
    ws.GET[FormTemplate](s"$baseUrl/formtemplates/${formTypeId.value}")

  def getForm(formId: FormId)(implicit hc: HeaderCarrier): Future[Form] =
    ws.GET[Form](s"$baseUrl/forms/${formId.value}")

  def maybeForm(formId: FormId)(implicit hc: HeaderCarrier): Future[Option[Form]] =
    ws.GET[Form](s"$baseUrl/forms/${formId.value}").map(Some(_)).recover {
      case e: NotFoundException => None
    }

  def saveForm(formDetails: FormData, tolerant: Boolean)(implicit hc: HeaderCarrier): Future[SaveResult] = {
    ws.POST[FormData, SaveResult](s"$baseUrl/forms?tolerant=$tolerant", formDetails)
  }

  def updateForm(formId: FormId, formData: FormData, tolerant: Boolean)(implicit hc: HeaderCarrier): Future[SaveResult] = {
    ws.PUT[FormData, HttpResponse](s"$baseUrl/forms/${formId.value}?tolerant=$tolerant", formData).map(x => SaveResult(None, None))
  }

  def sendSubmission(formTypeId: FormTypeId, formId: FormId)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    ws.POSTEmpty[HttpResponse](s"$baseUrl/forms/${formTypeId.value}/submission/${formId.value}")
  }

  def deleteForm(formId: FormId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SaveResult] =
    ws.POSTEmpty[SaveResult](baseUrl + s"/forms/$formId/delete")

}