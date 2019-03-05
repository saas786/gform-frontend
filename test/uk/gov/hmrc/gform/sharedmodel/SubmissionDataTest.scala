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

package uk.gov.hmrc.gform.sharedmodel

import play.api.libs.json.{ JsValue, Json }
import uk.gov.hmrc.gform.Spec

class SubmissionDataTest extends Spec {

  it should "serialized/deserialize in to/from json" in {
    val submissionData = SubmissionData(htmlForm, Variables(Json.parse("""{"user":{"enrolledIdentifier":"ITC"}}""")))

    Json.toJson(submissionData) should be(expectedJson)
    expectedJson.as[SubmissionData] should be(submissionData)
  }

  val htmlForm = "<hmtl>something</html>"

  val expectedJson: JsValue =
    Json.parse(
      s"""|{
          |  "pdfData": "$htmlForm",
          |  "variables": {
          |    "value": {
          |      "user": {
          |      "enrolledIdentifier": "ITC"
          |      }
          |    }
          |  }
          |}""".stripMargin
    )
}