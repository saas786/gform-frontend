/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.gform.gform.processor

import cats.data.Validated.Invalid
import cats.syntax.validated._
import play.api.i18n.Messages
import play.api.mvc.{ AnyContent, Request, Result }
import play.twirl.api.Html
import play.api.mvc.Results.{ Ok, Redirect }
import uk.gov.hmrc.csp.WebchatClient
import uk.gov.hmrc.gform.auth.models._
import uk.gov.hmrc.gform.config.FrontendAppConfig
import uk.gov.hmrc.gform.fileupload.Envelope
import uk.gov.hmrc.gform.gform.{ EnrolmentFormNotValid, NoIdentifierProvided, SubmitEnrolmentError }
import uk.gov.hmrc.gform.gform.RegimeIdNotMatch
import uk.gov.hmrc.gform.models.helpers.Fields
import uk.gov.hmrc.gform.sharedmodel.LangADT
import uk.gov.hmrc.gform.sharedmodel.form.{ FormDataRecalculated, ValidationResult }
import uk.gov.hmrc.gform.sharedmodel.formtemplate.{ EnrolmentSection, FormComponent, FormTemplate }
import uk.gov.hmrc.gform.validation.{ FormFieldValidationResult, ValidationUtil }
import uk.gov.hmrc.gform.validation.ValidationUtil.ValidatedType
import uk.gov.hmrc.gform.views.{ ViewHelpersAlgebra, html }
import uk.gov.hmrc.govukfrontend.views.viewmodels.content
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.ErrorLink

class EnrolmentResultProcessor(
  renderEnrolmentSection: RenderEnrolmentSection,
  formTemplate: FormTemplate,
  retrievals: MaterialisedRetrievals,
  enrolmentSection: EnrolmentSection,
  data: FormDataRecalculated,
  frontendAppConfig: FrontendAppConfig
)(implicit viewHelpers: ViewHelpersAlgebra) {

  private def getErrorMap(
    validationResult: ValidatedType[ValidationResult]
  ): List[(FormComponent, FormFieldValidationResult)] = {
    val enrolmentFields = Fields.flattenGroups(enrolmentSection.fields)
    evaluateValidation(validationResult, enrolmentFields, data, Envelope.empty)
  }

  private def evaluateValidation(
    v: ValidatedType[ValidationResult],
    fields: List[FormComponent],
    data: FormDataRecalculated,
    envelope: Envelope): List[(FormComponent, FormFieldValidationResult)] =
    // We need to keep the formComponent order as they appear on the form for page-level-error rendering, do not convert to map
    ValidationUtil
      .evaluateValidationResult(fields, v, data, envelope)
      .map(ffvr => ffvr.fieldValue -> ffvr)

  private def getResult(validationResult: ValidatedType[ValidationResult], globalErrors: List[ErrorLink]): Result = {
    val errorMap = getErrorMap(validationResult)
    Ok(
      renderEnrolmentSection(
        formTemplate,
        retrievals,
        enrolmentSection,
        data,
        errorMap,
        globalErrors,
        validationResult
      )
    )
  }

  def recoverEnrolmentError(implicit request: Request[AnyContent], messages: Messages): SubmitEnrolmentError => Result =
    enrolmentError => {

      def convertEnrolmentError(see: SubmitEnrolmentError): (ValidatedType[ValidationResult], List[ErrorLink]) =
        see match {
          case RegimeIdNotMatch(identifierRecipe) =>
            val regimeIdError = Map(identifierRecipe.value.toFieldId -> Set(messages("enrolment.error.regimeId")))
            (Invalid(regimeIdError), List.empty)
          case NoIdentifierProvided =>
            val globalError = ErrorLink(
              content = content.Text(messages("enrolment.error.missingIdentifier"))
            )
            (ValidationResult.empty.valid, globalError :: Nil)
          case EnrolmentFormNotValid(invalid) => (Invalid(invalid), List.empty)
        }

      val (validationResult, globalErrors) = convertEnrolmentError(enrolmentError)
      getResult(validationResult, globalErrors)

    }

  def processEnrolmentResult(
    authRes: CheckEnrolmentsResult)(implicit request: Request[AnyContent], messages: Messages, l: LangADT): Result =
    authRes match {
      case CheckEnrolmentsResult.Conflict =>
        Ok(uk.gov.hmrc.gform.views.html.hardcoded.pages.error_enrolment_conflict(formTemplate, frontendAppConfig))
      case CheckEnrolmentsResult.Successful =>
        Redirect(uk.gov.hmrc.gform.gform.routes.NewFormController.dashboard(formTemplate._id).url)
      case CheckEnrolmentsResult.InvalidIdentifiers | CheckEnrolmentsResult.InvalidCredentials |
          CheckEnrolmentsResult.InsufficientEnrolments =>
        val globalError: ErrorLink = ErrorLink(
          content = content.HtmlContent(html.form.errors.error_global_enrolment(formTemplate._id)))

        val globalErrors = globalError :: Nil
        val validationResult = ValidationResult.empty.valid
        getResult(validationResult, globalErrors)
      case CheckEnrolmentsResult.Failed =>
        // Nothing we can do here, so technical difficulties it is.
        throw new Exception("Enrolment has failed. Most probable reason is enrolment service being unavailable")
    }
}
