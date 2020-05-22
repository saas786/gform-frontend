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

package uk.gov.hmrc.gform.gform

import play.api.i18n.I18nSupport
import play.api.mvc.{ Action, AnyContent, MessagesControllerComponents }
import uk.gov.hmrc.gform.config.FrontendAppConfig
import uk.gov.hmrc.gform.controllers.AuthenticatedRequestActions
import uk.gov.hmrc.gform.sharedmodel.formtemplate.FormTemplateId
import uk.gov.hmrc.gform.views.ViewHelpersAlgebra
import uk.gov.hmrc.gform.views.html.hardcoded.pages.{ accessibility_statement, help_with_registration }
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class StaticPagesController(
  auth: AuthenticatedRequestActions,
  i18nSupport: I18nSupport,
  frontendAppConfig: FrontendAppConfig,
  messagesControllerComponents: MessagesControllerComponents)(implicit viewHelpers: ViewHelpersAlgebra)
    extends FrontendController(messagesControllerComponents) {

  def accessibilityPage(formTemplateId: FormTemplateId): Action[AnyContent] =
    auth.asyncNoAuth(formTemplateId) { implicit request => implicit l => formTemplate =>
      import i18nSupport._

      Future.successful(Ok(accessibility_statement(formTemplate, frontendAppConfig)))
    }

  def helpWithRegistrationPage(formTemplateId: FormTemplateId) =
    auth.asyncNoAuth(formTemplateId) { implicit request => implicit l => formTemplate =>
      import i18nSupport._

      Future.successful(Ok(help_with_registration(formTemplate, frontendAppConfig)))
    }
}