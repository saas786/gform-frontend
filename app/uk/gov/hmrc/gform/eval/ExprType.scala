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

package uk.gov.hmrc.gform.eval

sealed trait ExprType extends Product with Serializable {
  def fold[B](
    a: ExprType.Number.type => B
  )(
    b: ExprType.String.type => B
  )(
    c: ExprType.ChoiceSelection.type => B
  )(
    d: ExprType.Illegal.type => B
  ): B =
    this match {
      case t: ExprType.Number.type          => a(t)
      case t: ExprType.String.type          => b(t)
      case t: ExprType.ChoiceSelection.type => c(t)
      case t: ExprType.Illegal.type         => d(t)
    }
}

object ExprType {

  val number: ExprType = Number
  val string: ExprType = String
  val illegal: ExprType = Illegal
  val choiceSelection: ExprType = ChoiceSelection

  case object Number extends ExprType
  case object String extends ExprType
  case object Illegal extends ExprType
  case object ChoiceSelection extends ExprType
}
