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
import java.math.BigInteger
import java.security.MessageDigest

import scala.math.pow
import uk.gov.hmrc.gform.sharedmodel.form.EnvelopeId

object SubmissionReferenceUtil {
  val radix = 36
  val digits = 11
  val comb: Stream[Int] = Stream.continually(List(1, 3)).flatten

  def verifyCheckChar(reference: String): Boolean =
    "^([A-Z0-9]{4})-([A-Z0-9]{4})-([A-Z0-9]{4})$".r.findFirstMatchIn(reference) match {
      case Some(a) => verify(s"${a.group(1)}${a.group(2)}${a.group(3)}", radix, comb)
      case _       => false
    }

  def getSubmissionReference(maybeEnvelopeId: Option[EnvelopeId]): String = {
    val envelopeId = maybeEnvelopeId match {
      case Some(x) => x
      case None    => EnvelopeId("")
    }
    if (!envelopeId.value.isEmpty) {
      // As 36^11 (number of combinations of 11 base 36 digits) < 2^63 (number of combinations of 63 base 2 digits) we can get full significance from this digest.
      val digest = MessageDigest.getInstance("SHA-256").digest(envelopeId.value.getBytes()).take(8)
      val initialValue = new BigInteger(digest).abs()
      val unformattedString = calculate(initialValue, radix, digits, comb)
      unformattedString.grouped(4).mkString("-").toUpperCase
    } else { "" }
  }

  def calculate(value: BigInteger, radix: Int, digits: Int, comb: Stream[Int]): String = {
    val modulus: BigInteger = BigInteger.valueOf(pow(radix, digits).toLong)
    val derivedDigits = (value.mod(modulus) add modulus).toString(radix).takeRight(digits)
    val checkCharacter = BigInteger.valueOf(calculateCheckCharacter(derivedDigits, radix, comb)).toString(radix)
    checkCharacter + derivedDigits
  }

  def calculateCheckCharacter(digits: String, radix: Int, comb: Stream[Int]): Int = {
    val stringToInts = digits.toCharArray.map(i => Integer.parseInt(i.toString, radix))
    stringToInts.zip(comb).map(i => i._1 * i._2).sum % radix
  }

  def verify(reference: String, radix: Int, comb: Stream[Int]): Boolean = {
  calculateCheckCharacter(reference.tail, radix, comb) == Integer.parseInt(reference.head.toString, radix)
  }
}