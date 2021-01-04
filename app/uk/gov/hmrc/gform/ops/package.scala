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

package uk.gov.hmrc.gform

import uk.gov.hmrc.gform.sharedmodel.formtemplate.{ FormComponent, Number, PositiveNumber, ReferenceNumber, Sterling, Text, TextArea }

package object ops {

  implicit class FormComponentOps(formComponent: FormComponent) {
    def isSterling = formComponent.`type` match {
      case Text(_: Sterling, _, _, _)  => true
      case TextArea(_: Sterling, _, _) => true
      case _                           => false
    }
    def isNumber = formComponent.`type` match {
      case Text(Number(_, _, _, _), _, _, _)  => true
      case TextArea(Number(_, _, _, _), _, _) => true
      case _                                  => false
    }
    def isPositiveNumber = formComponent.`type` match {
      case Text(PositiveNumber(_, _, _, _), _, _, _)  => true
      case TextArea(PositiveNumber(_, _, _, _), _, _) => true
      case _                                          => false
    }

    def isReferenceNumber = formComponent.`type` match {
      case Text(_: ReferenceNumber, _, _, _)  => true
      case TextArea(_: ReferenceNumber, _, _) => true
      case _                                  => false
    }
  }
}
