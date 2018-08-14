/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.gform.graph

import uk.gov.hmrc.gform.sharedmodel.formtemplate._

object FormTemplateBuilder {
  def mkGroup(max: Int, formComponents: List[FormComponent]): Group =
    Group(
      formComponents,
      Vertical,
      Some(max),
      None,
      None,
      None
    )

  def mkSection(formComponents: List[FormComponent]) =
    Section(
      "Section Name",
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      formComponents,
      None
    )

  def mkFormComponent(fcId: String, ct: ComponentType) =
    FormComponent(
      FormComponentId(fcId),
      ct,
      "Label",
      None,
      None,
      None,
      true,
      false,
      true,
      false,
      false,
      None,
      None
    )

  def mkFormComponent(fcId: String, expr: Expr): FormComponent =
    mkFormComponent(fcId, Text(AnyText, expr))

  def mkFormTemplate(sections: List[Section]) = FormTemplate(
    FormTemplateId("tst1"),
    "Dependecy heavy experiment",
    "",
    Some(BetaBanner),
    None,
    None,
    DmsSubmission("R&D", TextExpression(FormCtx("utrRepComp")), "CCG-CT-RandDreports", "CCG", None),
    HMRCAuthConfigWithAuthModule(AuthConfigModule("hmrc")),
    "randd_confirmation_submission",
    "http://www.google.co.uk",
    "http://www.yahoo.co.uk",
    sections,
    AcknowledgementSection(
      "Acknowledgement Page",
      Some("this page is to acknowledge submission"),
      Some("shortName for acknowledgement"),
      List.empty[FormComponent]
    ),
    DeclarationSection("Declaration", None, None, Nil)
  )

}