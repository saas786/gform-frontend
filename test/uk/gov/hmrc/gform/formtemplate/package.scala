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

package object formtemplate {
  /* implicit class SectionSyntax(section: Section) {
 *   def page(): Page[Basic] =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.page
 *       case s: Section.RepeatingPage    => s.page
 *       case s: Section.AddToList        => throw new Exception("Cannot get page of a Section.AddToList")
 *     }
 *
 *   def updateTitle(title: SmartString): Section =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.copy(page = s.page.copy(title = title))
 *       case s: Section.RepeatingPage    => s.copy(page = s.page.copy(title = title))
 *       case s: Section.AddToList        => s.copy(title = title)
 *     }
 *
 *   def updateShortName(shortName: Option[SmartString]): Section =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.copy(page = s.page.copy(shortName = shortName))
 *       case s: Section.RepeatingPage    => s.copy(page = s.page.copy(shortName = shortName))
 *       case s: Section.AddToList        => s.copy(shortName = shortName)
 *     }
 *
 *   def updateFields(fields: List[FormComponent]): Section =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.copy(page = s.page.copy(fields = fields))
 *       case s: Section.RepeatingPage    => s.copy(page = s.page.copy(fields = fields))
 *       case _: Section.AddToList        => throw new Exception("Cannot update fields of a Section.AddToList")
 *     }
 *
 *   def updateProgressIndicator(progressIndicator: Option[SmartString]): Section =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.copy(page = s.page.copy(progressIndicator = progressIndicator))
 *       case s: Section.RepeatingPage    => s.copy(page = s.page.copy(progressIndicator = progressIndicator))
 *       case _: Section.AddToList        => throw new Exception("Cannot update fields of a Section.AddToList")
 *     }
 *
 *   def updateIncludeIf(includeIf: Option[IncludeIf]): Section =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.copy(page = s.page.copy(includeIf = includeIf))
 *       case s: Section.RepeatingPage    => s.copy(page = s.page.copy(includeIf = includeIf))
 *       case _: Section.AddToList        => throw new Exception("Cannot update fields of a Section.AddToList")
 *     }
 *   def fields(): List[FormComponent] =
 *     section match {
 *       case s: Section.NonRepeatingPage => s.page.fields
 *       case s: Section.RepeatingPage    => s.page.fields
 *       case _: Section.AddToList        => throw new Exception("Cannot get fields of a Section.AddToList")
 *     }
 * } */
}
