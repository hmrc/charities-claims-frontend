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

package config

object Constants {
  inline val maxAdjustmentToClaimLength = 350

  inline val maxClaimsReferenceNumberLength = 20

  inline val maxCharityRegulatorNumberLength = 20

  inline val maxTitleLength       = 4
  inline val maxAddressLineLength = 35
  inline val maxPostcodeLength    = 8
  inline val maxPhoneNumberLength = 30

  inline val maxCharityNameLength = 160

  inline val maxTrusteeNameLength = 160

  object Css {
    inline val bodyCssClass = "govuk-body"

    inline val linkCssClass                = "govuk-link"
    inline val linkTextCssClass            = "link-text"
    inline val linkNotVisitedStateCssClass = "govuk-link--no-visited-state"

    inline val printLinkCssClass = "print-link"

    inline val hintCssClass = "govuk-hint"

    inline val jsVisibleCssClass = "hmrc-!-js-visible"

    inline val jsFormCssClass = "js-form"

    inline val fileUploadCssClass                 = "file-upload"
    inline val fileUploadLoadingContainerCssClass = "file-upload__loading-container"
    inline val fileUploadSpinnerCssClass          = "file-upload__spinner"
    inline val fileUploadErrorCssClass            = "govuk-file-upload--error"
    inline val ccmsLoaderHiddenCssClass           = "ccms-loader hidden"

    inline val displayNonePrintCssClass = "govuk-!-display-none-print"

    inline val buttonCssClass      = "govuk-button"
    inline val buttonGroupCssClass = "govuk-button-group"

    inline val radiosCssClass       = "govuk-radios"
    inline val inlineRadiosCssClass = "govuk-radios--inline"
    inline val radiosInputCssClass  = "govuk-radios__input"
    inline val radiosItemCssClass   = "govuk-radios__item"
    inline val radiosLabelCssClass  = "govuk-radios__label"

    inline val hmrcTagCssClass = "hmrc-status-tag"
    inline val tagCssClass     = "govuk-tag"

    inline val yellowTagCssClass = "govuk-tag--yellow"
    inline val greenTagCssClass  = "govuk-tag--green"

    inline val headingXLCssClass = "govuk-heading-xl"
    inline val headingLCssClass  = "govuk-heading-l"
    inline val headingMCssClass  = "govuk-heading-m"

    inline val labelCssClass  = "govuk-label"
    inline val labelLCssClass = "govuk-label--l"

    inline val captionLLabelCssClass = "govuk-caption-l"

    inline val fieldsetClass = "govuk-fieldset"

    inline val fieldsetLegendMCssClass = "govuk-fieldset__legend--m"
    inline val fieldsetLegendLCssClass = "govuk-fieldset__legend--l"

    inline val textAreaCssClass              = "govuk-textArea"
    inline val jsCharacterCountCssClass      = "govuk-js-character-count"
    inline val characterCountCssClass        = "govuk-character-count"
    inline val characterCountMessageCssClass = "govuk-character-count__message"

    inline val marginTop1CssClass = "govuk-!-margin-top-1"
    inline val marginTop6CssClass = "govuk-!-margin-top-6"

    inline val marginBottom0CssClass = "govuk-!-margin-bottom-0"
    inline val marginBottom3CssClass = "govuk-!-margin-bottom-3"
    inline val marginBottom6CssClass = "govuk-!-margin-bottom-6"
    inline val marginBottom7CssClass = "govuk-!-margin-bottom-7"
    inline val marginBottom8CssClass = "govuk-!-margin-bottom-8"
    inline val marginBottom9CssClass = "govuk-!-margin-bottom-9"

    inline val inputCssClass = "govuk-input"

    inline val inputWidth4CssClass  = "govuk-input--width-4"
    inline val inputWidth5CssClass  = "govuk-input--width-5"
    inline val inputWidth10CssClass = "govuk-input--width-10"
    inline val inputWidth20CssClass = "govuk-input--width-20"

    inline val listCssClass       = "govuk-list"
    inline val bulletListCssClass = "govuk-list govuk-list--bullet"
    inline val numberListCssClass = "govuk-list govuk-list--number"

    inline val panelCssClass             = "govuk-panel"
    inline val panelConfirmationCssClass = "govuk-panel--confirmation"
    inline val panelTitleCssClass        = "govuk-panel__title"
    inline val panelBodyCssClass         = "govuk-panel__body"

    inline val visuallyHiddenCssClass = "govuk-visually-hidden"

    inline val formGroupCssClass = "govuk-form-group"

    inline val addToAListCssClass                = "hmrc-add-to-a-list"
    inline val addToAListShortClass              = "hmrc-add-to-a-list--short"
    inline val addToAListContentsCssClass        = "hmrc-add-to-a-list__contents"
    inline val addToAListChangeCssClass          = "hmrc-add-to-a-list__change"
    inline val addToAListRemoveCssClass          = "hmrc-add-to-a-list__remove"
    inline val addToAListIdentifierCssClass      = "hmrc-add-to-a-list__identifier"
    inline val addToAListIdentifierLightCssClass = "hmrc-add-to-a-list__identifier--light"

    inline val currencyInputCssClass           = "govuk-currency-input"
    inline val currencyInputInnerCssClass      = "govuk-currency-input__inner"
    inline val currencyInputInnerInputCssClass = "govuk-currency-input__inner__input"
    inline val currencyInputUnitCssClass       = "hmrc-currency-input__unit"

    inline val warningTextCssClass     = "govuk-warning-text"
    inline val warningTextIconCssClass = "govuk-warning-text__icon"
    inline val warningTextTextCssClass = "govuk-warning-text__text"

    inline val taskListCssClass              = "app-task-list"
    inline val taskListSectionCssClass       = "app-task-list__section"
    inline val taskListSectionNumberCssClass = "app-task-list__section-number"
    inline val taskListItemCssClass          = "app-task-list__item"
    inline val taskListItemsCssClass         = "app-task-list__items"
    inline val taskListTaskNameCssClass      = "app-task-list__task-name"

    inline val autocompleteWrapperCssClass = "autocomplete-wrapper"

    inline val tableHeaderCssClass = "govuk-table__header"

    inline val communityBuildingsTableCssClass = "community-buildings-table"
    inline val connectedCharitiesTableCssClass = "connected-charities-table"

    inline val summaryListCssClass                = "govuk-summary-list"
    inline val summaryListLongKeyCssClass         = "govuk-summary-list--long-key"
    inline val summaryListRowCssClass             = "govuk-summary-list__row"
    inline val summaryListKeyCssClass             = "govuk-summary-list__key"
    inline val summaryListValueCssClass           = "govuk-summary-list__value"
    inline val summaryListActionsCssClass         = "govuk-summary-list__actions"
    inline val hmrcSummaryListActionsCssClass     = "hmrc-summary-list__actions"
    inline val hmrcSummaryListActionsListCssClass = "govuk-summary-list__actions-list"
    inline val summaryListActionsListItemCssClass = "govuk-summary-list__actions-list-item"

    inline val customSummaryListCssClass   = "custom-summary-list"
    inline val customPaddingHeaderCssClass = "custom-padding-header"

    inline val claimReferenceNumberCssClass = "claim-reference-number"
    inline val claimTypeHintCssClass        = "claim-type-hint"

    inline val charityNameCaptionCssClass = "charity-name-caption"
  }
}
