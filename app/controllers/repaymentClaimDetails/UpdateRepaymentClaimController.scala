/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.repaymentClaimDetails

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.Actions
import forms.YesNoFormProvider
import models.RepaymentClaimDetailsAnswers
import play.api.Logging
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ClaimsValidationService, SaveService}
import views.html.UpdateRepaymentClaimView

import scala.concurrent.{ExecutionContext, Future}

class UpdateRepaymentClaimController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  view: UpdateRepaymentClaimView,
  actions: Actions,
  formProvider: YesNoFormProvider,
  saveService: SaveService,
  claimsValidationService: ClaimsValidationService
)(using ec: ExecutionContext)
    extends BaseController
    with Logging {

  val form: Form[Boolean] = formProvider("updateRepaymentClaim.error.required")

  def onPageLoad: Action[AnyContent] = actions.authAndGetData() { implicit request =>
    // confirm and validate play flash parameters are present
    (updateSource, updateMode) match {
      case (Some(source), Some(mode)) =>
        // confirm flash mode is CheckMode - possibly not needed
        if (mode != "CheckMode") {
          logger.warn(s"Incorrect mode used with mode=$mode, expected CheckMode")
        }
        // otherwise render view - flash data in hidden fields
        Ok(view(form))

      case _ =>
        // if missing flash parameters then we redirect to CYA
        logger.error(
          "Missing required Flash parameters (updateSource/updateMode)"
        )
        Redirect(controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad)
    }
  }

  def onSubmit: Action[AnyContent] = actions.authAndGetData().async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        wantsToUpdate => {
          // check hidden fields from form submission
          val source = request.body.asFormUrlEncoded.flatMap(_.get("updateSource").flatMap(_.headOption))
          val mode   = request.body.asFormUrlEncoded.flatMap(_.get("updateMode").flatMap(_.headOption))

          if (!wantsToUpdate) {
            // user selected NO - no update, redirect to CYA
            Future.successful(Redirect(controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad))
          } else {
            // user selected YES - continue with deletion and save, checking source
            source match {
              case Some("claimingGiftAid") =>
                // delete Gift Aid schedule and save the new value to false
                claimsValidationService.deleteGiftAidSchedule
                  .flatMap { _ =>
                    saveService
                      .save(RepaymentClaimDetailsAnswers.setClaimingGiftAid(false))
                      .map { _ =>
                        // TODO: redirect to R2 screen - this route will be updated in the future
                        Redirect(controllers.organisationDetails.routes.MakeCharityRepaymentClaimController.onPageLoad)
                      }
                  }
                  .recover { case exception =>
                    logger
                      .error(s"Failed to delete Gift Aid schedule or update data: ${exception.getMessage}", exception)
                    Redirect(controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad)
                  }

              case Some("claimingOtherIncome") =>
                // TODO: uncomment when ClaimsValidationService.deleteOtherIncomeSchedule is implemented
                // claimsValidationService.deleteOtherIncomeSchedule
                //   .flatMap { _ =>
                saveService
                  .save(RepaymentClaimDetailsAnswers.setClaimingTaxDeducted(false))
                  .map { _ =>
                    // TODO: redirect to R2 screen - this route will be updated in the future
                    Redirect(controllers.organisationDetails.routes.MakeCharityRepaymentClaimController.onPageLoad)
                  }
                  .recover { case exception =>
                    logger.error(s"Failed to update OtherIncome data: ${exception.getMessage}", exception)
                    Redirect(controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad)
                  }
              // TODO: close the flatMap block once deletion is implemented
              //   }
              // }

              case Some("claimingGiftAidSmallDonations") =>
                // TODO: uncomment when ClaimsValidationService.deleteSmallDonationsSchedule is implemented
                // claimsValidationService.deleteSmallDonationsSchedule
                //   .flatMap { _ =>
                saveService
                  .save(RepaymentClaimDetailsAnswers.setClaimingUnderGiftAidSmallDonationsScheme(false))
                  .map { _ =>
                    // TODO: redirect to R2 screen - this route will be updated in the future
                    Redirect(controllers.organisationDetails.routes.MakeCharityRepaymentClaimController.onPageLoad)
                  }
                  .recover { case exception =>
                    logger.error(s"Failed to update SmallDonations data: ${exception.getMessage}", exception)
                    Redirect(controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad)
                  }
              // TODO: Close the flatMap block once deletion is implemented
              //   }
              // }

              case _ =>
                // if missing source - redirect to CYA
                logger.error(s"Update attempted with invalid/missing source: $source")
                Future
                  .successful(Redirect(controllers.repaymentclaimdetails.routes.CheckYourAnswersController.onPageLoad))
            }
          }
        }
      )
  }
}
