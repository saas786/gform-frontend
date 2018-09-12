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

package uk.gov.hmrc.gform.models

import cats.data.NonEmptyList
import org.scalactic.source.Position
import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.prop.TableDrivenPropertyChecks.{ Table, forAll }
import ExpandUtils._
import uk.gov.hmrc.gform.graph.FormTemplateBuilder._
import uk.gov.hmrc.gform.models.helpers.Fields
import uk.gov.hmrc.gform.sharedmodel.form.{ FormData, FormField }
import uk.gov.hmrc.gform.sharedmodel.formtemplate._

class ExpandUtilsSpec extends FlatSpec with Matchers {

  "submittedFCs" should "FormComponents reconstructed from data and ignore unrelated FormComponents" in {
    val data = mkData(
      "repeatingSectionDriver",
      "repeatingSecondField",
      "repeatingThirdField-day",
      "repeatingThirdField-month",
      "repeatingThirdField-year",
      "repeatingFourthField-street1",
      "repeatingFourthField-street2",
      "repeatingFourthField-street3",
      "repeatingFourthField-street4",
      "repeatingFourthField-uk",
      "repeatingFourthField-postcode",
      "repeatingFourthField-country",
      "repeatingFifthField-1",
      "repeatingFifthField-2",
      "repeatingFifthField-3"
    )

    val formComponents =
      mkFormComponent(FormComponentId("repeatingSectionDriver"), Text(AnyText, Value)) ::
        mkFormComponent(FormComponentId("repeatingSecondField"), Text(AnyText, Value)) ::
        mkFormComponent(FormComponentId("repeatingThirdField"), Date(AnyDate, Offset(0), None)) ::
        mkFormComponent(FormComponentId("repeatingFourthField"), Address(true)) ::
        mkFormComponent(FormComponentId("repeatingFifthField"), UkSortCode(Value)) :: Nil

    val unrelated =
      mkFormComponent(FormComponentId("not-submitted-text"), Text(AnyText, Value)) ::
        mkFormComponent(FormComponentId("not-submitted-address"), Address(true)) ::
        mkFormComponent(FormComponentId("not-submitted-ukSortCode"), UkSortCode(Value)) :: Nil

    val submittedFormComponents = submittedFCs(data, unrelated ++ formComponents)

    submittedFormComponents shouldBe formComponents

  }

  "appendZeroPrefix" should "append 0_ prefix to FormComponentId if it doesn't have another prefix" in {

    val formComponentIds = Table(
      // format: off
      ("input",     "output"),
      ("abd",       "0_abd"),
      ("1_abd",     "1_abd"),
      ("0_abd",     "0_abd"),
      ("1_",        "1_"),
      ("123_abd",   "123_abd"),
      ("123_",      "123_"),
      ("das123_dd", "0_das123_dd")
      // format: on
    )

    forAll(formComponentIds) { (input, expectedOuput) ⇒
      appendZeroPrefix(FormComponentId(input)) shouldBe FormComponentId(expectedOuput)
    }
  }
  "stripAnyPrefix" should "strip any prefix" in {

    val formComponentIds = Table(
      // format: off
      ("input",     "output"),
      ("abd",       "abd"),
      ("1_abd",     "abd"),
      ("1_",        ""),
      ("123_abd",   "abd"),
      ("123_",      ""),
      ("das123_dd", "das123_dd")
      // format: on
    )

    forAll(formComponentIds) { (input, expectedOuput) ⇒
      stripAnyPrefix(FormComponentId(input)) shouldBe FormComponentId(expectedOuput)
    }
  }

  "groupIndex" should "return group fields ids with required index" in {
    val groupIds = "a" :: "b" :: "c" :: "d" :: Nil map FormComponentId.apply
    val group = mkGroup(groupIds)

    val expectedForIndex2 = "1_a" :: "1_b" :: "1_c" :: "1_d" :: Nil map FormComponentId.apply
    val expectedForIndex5 = "4_a" :: "4_b" :: "4_c" :: "4_d" :: Nil map FormComponentId.apply

    groupIndex(1, group) shouldBe groupIds
    groupIndex(2, group) shouldBe expectedForIndex2
    groupIndex(5, group) shouldBe expectedForIndex5
  }

  "addNextGroup" should "add group contaning simple components" in {

    val groupIds = "a" :: "b" :: "c" :: "d" :: Nil map FormComponentId.apply
    val group = mkFormComponent(FormComponentId("HA"), mkGroup(groupIds))

    val formData1 = "a" :: "b" :: "c" :: "d" :: Nil map (fcId => FormField(FormComponentId(fcId), "dummy"))
    val formData2 = "a" :: "b" :: "c" :: "d" :: "1_a" :: "1_b" :: "1_c" :: "1_d" :: Nil map (fcId =>
      FormField(FormComponentId(fcId), "dummy"))

    val expected1 = List(
      FormField(FormComponentId("1_a"), ""),
      FormField(FormComponentId("1_b"), ""),
      FormField(FormComponentId("1_c"), ""),
      FormField(FormComponentId("1_d"), "")
    )
    val expected2 = List(
      FormField(FormComponentId("2_a"), ""),
      FormField(FormComponentId("2_b"), ""),
      FormField(FormComponentId("2_c"), ""),
      FormField(FormComponentId("2_d"), "")
    )

    addNextGroup(Some(group), FormData(formData1)) shouldBe ((FormData(expected1), Some("1_a")))
    addNextGroup(Some(group), FormData(formData2)) shouldBe ((FormData(expected2), Some("2_a")))

  }

  it should "add group containing Date component" in {
    val groupIds = "a" :: Nil map FormComponentId.apply
    val group = mkFormComponent(FormComponentId("HA"), mkGroupWithDate(groupIds))

    val formData1 = "a-day" :: "a-month" :: "a-year" :: Nil map (fcId => FormField(FormComponentId(fcId), "dummy"))
    val formData2 = "a-day" :: "a-month" :: "a-year" :: "1_a-day" :: "1_a-month" :: "1_a-year" :: Nil map (fcId =>
      FormField(FormComponentId(fcId), "dummy"))

    val expected1 = List(
      FormField(FormComponentId("1_a-day"), ""),
      FormField(FormComponentId("1_a-month"), ""),
      FormField(FormComponentId("1_a-year"), "")
    )
    val expected2 = List(
      FormField(FormComponentId("2_a-day"), ""),
      FormField(FormComponentId("2_a-month"), ""),
      FormField(FormComponentId("2_a-year"), "")
    )

    addNextGroup(Some(group), FormData(formData1)) shouldBe ((FormData(expected1), Some("1_a-day")))
    addNextGroup(Some(group), FormData(formData2)) shouldBe ((FormData(expected2), Some("2_a-day")))

  }

  "removeGroupFromData" should "remove group fields at particular index from data, and keep unrelated fields untouched" in {

    val groupIds = "a" :: "b" :: "c" :: "d" :: Nil map FormComponentId.apply
    val group = mkFormComponent(FormComponentId("dummy"), mkGroup(groupIds))

    val data = mkData(
      "unrelated",
      "a",
      "b",
      "c",
      "d",
      "1_a",
      "1_b",
      "1_c",
      "1_d",
      "2_a",
      "2_b",
      "2_c",
      "2_d",
      "3_a",
      "3_b",
      "3_c",
      "3_d")

    val res1 = removeGroupFromData(1, Some(group), data)
    val res2 = removeGroupFromData(2, Some(group), data)
    val res3 = removeGroupFromData(3, Some(group), data)
    val res4 = removeGroupFromData(4, Some(group), data)

    val expected1 = Map(
      FormComponentId("a")         -> List("1_A"),
      FormComponentId("b")         -> List("1_B"),
      FormComponentId("c")         -> List("1_C"),
      FormComponentId("d")         -> List("1_D"),
      FormComponentId("1_a")       -> List("2_A"),
      FormComponentId("1_b")       -> List("2_B"),
      FormComponentId("1_c")       -> List("2_C"),
      FormComponentId("1_d")       -> List("2_D"),
      FormComponentId("2_a")       -> List("3_A"),
      FormComponentId("2_b")       -> List("3_B"),
      FormComponentId("2_c")       -> List("3_C"),
      FormComponentId("2_d")       -> List("3_D"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )
    val expected2 = Map(
      FormComponentId("a")         -> List("A"),
      FormComponentId("b")         -> List("B"),
      FormComponentId("c")         -> List("C"),
      FormComponentId("d")         -> List("D"),
      FormComponentId("1_a")       -> List("2_A"),
      FormComponentId("1_b")       -> List("2_B"),
      FormComponentId("1_c")       -> List("2_C"),
      FormComponentId("1_d")       -> List("2_D"),
      FormComponentId("2_a")       -> List("3_A"),
      FormComponentId("2_b")       -> List("3_B"),
      FormComponentId("2_c")       -> List("3_C"),
      FormComponentId("2_d")       -> List("3_D"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )
    val expected3 = Map(
      FormComponentId("a")         -> List("A"),
      FormComponentId("b")         -> List("B"),
      FormComponentId("c")         -> List("C"),
      FormComponentId("d")         -> List("D"),
      FormComponentId("1_a")       -> List("1_A"),
      FormComponentId("1_b")       -> List("1_B"),
      FormComponentId("1_c")       -> List("1_C"),
      FormComponentId("1_d")       -> List("1_D"),
      FormComponentId("2_a")       -> List("3_A"),
      FormComponentId("2_b")       -> List("3_B"),
      FormComponentId("2_c")       -> List("3_C"),
      FormComponentId("2_d")       -> List("3_D"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )
    val expected4 = Map(
      FormComponentId("a")         -> List("A"),
      FormComponentId("b")         -> List("B"),
      FormComponentId("c")         -> List("C"),
      FormComponentId("d")         -> List("D"),
      FormComponentId("1_a")       -> List("1_A"),
      FormComponentId("1_b")       -> List("1_B"),
      FormComponentId("1_c")       -> List("1_C"),
      FormComponentId("1_d")       -> List("1_D"),
      FormComponentId("2_a")       -> List("2_A"),
      FormComponentId("2_b")       -> List("2_B"),
      FormComponentId("2_c")       -> List("2_C"),
      FormComponentId("2_d")       -> List("2_D"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )

    res1 shouldBe expected1
    res2 shouldBe expected2
    res3 shouldBe expected3
    res4 shouldBe expected4
  }

  it should "remove group containing Date component" in {
    val groupIds = "a" :: Nil map FormComponentId.apply
    val group = mkFormComponent(FormComponentId("dummy"), mkGroupWithDate(groupIds))

    val data = mkData(
      "unrelated",
      "a-day",
      "a-month",
      "a-year",
      "1_a-day",
      "1_a-month",
      "1_a-year",
      "2_a-day",
      "2_a-month",
      "2_a-year")

    val res1 = removeGroupFromData(1, Some(group), data)
    val res2 = removeGroupFromData(2, Some(group), data)
    val res3 = removeGroupFromData(3, Some(group), data)

    val expected1 = Map(
      FormComponentId("a-day")     -> List("1_A-DAY"),
      FormComponentId("a-month")   -> List("1_A-MONTH"),
      FormComponentId("a-year")    -> List("1_A-YEAR"),
      FormComponentId("1_a-day")   -> List("2_A-DAY"),
      FormComponentId("1_a-month") -> List("2_A-MONTH"),
      FormComponentId("1_a-year")  -> List("2_A-YEAR"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )
    val expected2 = Map(
      FormComponentId("a-day")     -> List("A-DAY"),
      FormComponentId("a-month")   -> List("A-MONTH"),
      FormComponentId("a-year")    -> List("A-YEAR"),
      FormComponentId("1_a-day")   -> List("2_A-DAY"),
      FormComponentId("1_a-month") -> List("2_A-MONTH"),
      FormComponentId("1_a-year")  -> List("2_A-YEAR"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )
    val expected3 = Map(
      FormComponentId("a-day")     -> List("A-DAY"),
      FormComponentId("a-month")   -> List("A-MONTH"),
      FormComponentId("a-year")    -> List("A-YEAR"),
      FormComponentId("1_a-day")   -> List("1_A-DAY"),
      FormComponentId("1_a-month") -> List("1_A-MONTH"),
      FormComponentId("1_a-year")  -> List("1_A-YEAR"),
      FormComponentId("unrelated") -> List("UNRELATED")
    )

    res1 shouldBe expected1
    res2 shouldBe expected2
    res3 shouldBe expected3
  }

  "getAllFieldsInGroup" should "return List of GroupList constructed from data" in {

    val groupIds = "a" :: "b" :: "c" :: "d" :: Nil map FormComponentId.apply

    val data = mkData(
      "unrelated",
      "a",
      "b",
      "c",
      "d",
      "1_a",
      "1_b",
      "1_c",
      "1_d",
      "2_a",
      "2_b",
      "2_c",
      "2_d",
      "3_a",
      "3_b",
      "3_c",
      "3_d")

    val group = mkGroup(groupIds)
    val formComponent = mkFormComponent(FormComponentId("dummy"), group)
    val res = getAllFieldsInGroup(formComponent, group, data)

    val expectedChoice =
      List(
        GroupList(
          List(
            mkFormComponent(FormComponentId("a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("1_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("2_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("3_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("3_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("3_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("3_d"), Text(AnyText, Value))
          ))
      )

    res shouldBe expectedChoice

  }

  "fillToMin" should "fill group to required min size" in {

    def toComponent(fcId: String): FormComponent = mkFormComponent(FormComponentId(fcId), Text(AnyText, Value))

    val groupIds = "a" :: "b" :: "c" :: "d" :: Nil map FormComponentId.apply
    val group = mkGroup(groupIds).copy(repeatsMin = Some(3))

    val first = "a" :: "b" :: "c" :: "d" :: Nil map toComponent
    val second = "1_a" :: "1_b" :: "1_c" :: "1_d" :: Nil map toComponent
    val third = "2_a" :: "2_b" :: "2_c" :: "2_d" :: Nil map toComponent
    val fourth = "3_a" :: "3_b" :: "3_c" :: "3_d" :: Nil map toComponent

    val oneGroup = GroupList(first) :: Nil
    val twoGroups = GroupList(first) :: GroupList(second) :: Nil
    val threeGroups = GroupList(first) :: GroupList(second) :: GroupList(third) :: Nil
    val fourGroups = GroupList(first) :: GroupList(second) :: GroupList(third) :: GroupList(fourth) :: Nil

    val res1 = fillToMin(oneGroup, group)
    val res2 = fillToMin(twoGroups, group)
    val res3 = fillToMin(threeGroups, group)
    val res4 = fillToMin(fourGroups, group)

    val expectedChoice =
      List(
        GroupList(
          List(
            mkFormComponent(FormComponentId("a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("1_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("2_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_d"), Text(AnyText, Value))
          ))
      )

    res1 shouldBe expectedChoice
    res2 shouldBe expectedChoice
    res3 shouldBe expectedChoice

    val expected4 =
      List(
        GroupList(
          List(
            mkFormComponent(FormComponentId("a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("1_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("1_d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("2_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("2_d"), Text(AnyText, Value))
          )),
        GroupList(
          List(
            mkFormComponent(FormComponentId("3_a"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("3_b"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("3_c"), Text(AnyText, Value)),
            mkFormComponent(FormComponentId("3_d"), Text(AnyText, Value))
          ))
      )

    res4 shouldBe expected4

  }

  private def mkData(fcIds: String*) =
    fcIds.toList map (fcId => (FormComponentId(fcId), fcId.toUpperCase :: Nil)) toMap

  "getAlwaysEmptyHidden" should "should ignore Choice which is not part of a Group" in {

    val section = mkSection(mkFormComponent(FormComponentId("a"), choice) :: Nil)

    val data = mkData("a")

    val res = getAlwaysEmptyHidden(data, section)

    res shouldBe empty
  }

  it should "return base FormComponent of group if submitted data are empty" in {

    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice
      )
    )

    val data = Map.empty[FormComponentId, Seq[String]]

    val res = getAlwaysEmptyHidden(data, section)

    val expectedChoice = "a" :: Nil map (id => mkFormComponent(FormComponentId(id), choice))

    res shouldBe expectedChoice

  }

  it should "return base FormComponent of a group if submitted data contains only base formComponent id" in {

    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice
      )
    )

    val data = mkData("a")

    val res = getAlwaysEmptyHidden(data, section)

    val expectedChoice = "a" :: Nil map (id => mkFormComponent(FormComponentId(id), choice))

    res shouldBe expectedChoice

  }

  it should "return base FormComponent, first and second index of a group" in {

    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice
      )
    )

    val data = mkData("a", "1_a", "2_a")

    val res = getAlwaysEmptyHidden(data, section)

    val expectedChoice = "a" :: "1_a" :: "2_a" :: Nil map (id => mkFormComponent(FormComponentId(id), choice))

    res shouldBe expectedChoice

  }

  it should "handle group with multiple fields" in {

    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice,
        "b" -> choice
      )
    )

    val data = mkData("a", "b")

    val res = getAlwaysEmptyHidden(data, section)

    val expectedChoice = "a" :: "b" :: Nil map (id => mkFormComponent(FormComponentId(id), choice))

    res shouldBe expectedChoice

  }

  it should "disregard non choice FormComponents" in {

    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice,
        "b" -> choice,
        "c" -> Text(AnyText, Value)
      )
    )

    val data = mkData("a", "b", "c", "1_a", "1_b", "1_c")

    val res = getAlwaysEmptyHidden(data, section)

    val expectedChoice = "a" :: "b" :: "1_a" :: "1_b" :: Nil map (id => mkFormComponent(FormComponentId(id), choice))

    res shouldBe expectedChoice

  }

  it should "handle FormComponent of type InformationMessage" in {

    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice,
        "b" -> informationMessage,
        "c" -> Text(AnyText, Value)
      )
    )

    val data = mkData("a", "b", "c", "1_a", "1_b", "1_c")

    val emptyHidden = getAlwaysEmptyHidden(data, section)

    val expectedChoice = "a" :: "1_a" :: Nil map (id => mkFormComponent(FormComponentId(id), choice))
    val expectedInfo = "b" :: "1_b" :: Nil map (id => mkFormComponent(FormComponentId(id), informationMessage))

    emptyHidden should contain theSameElementsAs (expectedChoice ++ expectedInfo)

  }

  "getHiddenTemplateFields" should "produce list of hidden components and data with empty values for all choice components present on the page " in {
    val section = mkSectionWithGroupHaving(
      List(
        "a" -> choice,
        "b" -> choice,
        "c" -> Text(AnyText, Value)
      )
    )

    val sections = section ::
      mkSection(mkFormComponent(FormComponentId("e"), Text(AnyText, Value)) :: Nil) ::
      mkSection(mkFormComponent(FormComponentId("f"), Text(AnyText, Value)) :: Nil) :: Nil

    val data = mkData("a", "b", "c", "1_a", "1_b", "1_c", "e", "f")

    val (hiddenFormComponent, dataUpd) = Fields.getHiddenTemplateFields(section, sections, data)

    val expectedFC = List(
      "e"   -> Text(AnyText, Value),
      "f"   -> Text(AnyText, Value),
      "a"   -> choice,
      "b"   -> choice,
      "1_a" -> choice,
      "1_b" -> choice
    ) map { case (id, comp) => mkFormComponent(FormComponentId(id), comp) }

    val expectedData = Map(
      FormComponentId("a")   -> List(""),
      FormComponentId("1_a") -> List(""),
      FormComponentId("b")   -> List(""),
      FormComponentId("1_b") -> List(""),
      FormComponentId("c")   -> List("C"),
      FormComponentId("1_c") -> List("1_C"),
      FormComponentId("e")   -> List("E"),
      FormComponentId("f")   -> List("F")
    )

    dataUpd shouldBe expectedData
    hiddenFormComponent shouldBe expectedFC
  }

  private def mkSectionWithGroupHaving(xs: List[(String, ComponentType)]): Section = {
    val fields = xs.map { case (id, comp) => mkFormComponent(FormComponentId(id), comp) }

    val group = mkGroupWith(fields)

    mkSection(mkFormComponent(FormComponentId("dummy"), group) :: Nil)
  }

  val choice = Choice(YesNo, NonEmptyList.of("yes", "no"), Vertical, List.empty, None)
  val informationMessage = InformationMessage(StandardInfo, "info-text")

  def mkGroup(groupIds: List[FormComponentId]) =
    Group(
      groupIds.map(fcId => mkFormComponent(fcId, Text(AnyText, Value))),
      Vertical,
      repeatsMax = Some(5),
      repeatsMin = None,
      None,
      None
    )

  def mkGroupWithDate(groupIds: List[FormComponentId]) =
    Group(
      groupIds.map(fcId => mkFormComponent(fcId, Date(AnyDate, Offset(0), None))),
      Vertical,
      repeatsMax = Some(5),
      repeatsMin = None,
      None,
      None
    )

  def mkGroupWith(fcIds: List[FormComponent]) =
    Group(
      fcIds,
      Vertical,
      repeatsMax = Some(5),
      repeatsMin = None,
      None,
      None
    )

  private def mkFormComponent(fcId: FormComponentId, ct: ComponentType) =
    FormComponent(
      fcId,
      ct,
      "some-component",
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
}