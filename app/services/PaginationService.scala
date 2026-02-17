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

package services

import viewmodels.govuk.PaginationFluency.*
import models.*

case class PaginationConfig(
  recordsPerPage: Int = 10,
  maxRecords: Int = 10000, // TBC
  maxVisiblePages: Int = 5
)

case class DonationsPaginationResult(
  paginatedData: Seq[Donation],
  paginationViewModel: PaginationViewModel,
  totalRecords: Int,
  currentPage: Int,
  totalPages: Int
)

case class OtherIncomesPaginationResult(
  paginatedData: Seq[OtherIncome],
  paginationViewModel: PaginationViewModel,
  totalRecords: Int,
  currentPage: Int,
  totalPages: Int
)

case class ValidationErrorsPaginationResult(
  paginatedData: Seq[ValidationError],
  paginationViewModel: PaginationViewModel,
  totalRecords: Int,
  currentPage: Int,
  totalPages: Int
)

object PaginationService {

  private val config = PaginationConfig()

  def paginateDonations(
    allDonations: Seq[Donation],
    currentPage: Int = 1,
    baseUrl: String
  ): DonationsPaginationResult = {

    val donations = allDonations

    val totalRecords     = donations.length
    val totalPages       = calculateTotalPages(totalRecords)
    val validCurrentPage = validateCurrentPage(currentPage, totalPages)

    val (startIndex, endIndex) = calculatePageIndices(validCurrentPage, totalRecords)

    val paginatedData = donations.slice(startIndex, endIndex)

    val paginationViewModel = createPaginationViewModel(
      currentPage = validCurrentPage,
      totalPages = totalPages,
      baseUrl = baseUrl
    )

    DonationsPaginationResult(
      paginatedData = paginatedData,
      paginationViewModel = paginationViewModel,
      totalRecords = totalRecords,
      currentPage = validCurrentPage,
      totalPages = totalPages
    )
  }

  def paginateOtherIncomes(
    allOtherIncomes: Seq[OtherIncome],
    currentPage: Int = 1,
    baseUrl: String
  ): OtherIncomesPaginationResult = {

    val totalRecords     = allOtherIncomes.length
    val totalPages       = calculateTotalPages(totalRecords)
    val validCurrentPage = validateCurrentPage(currentPage, totalPages)

    val (startIndex, endIndex) = calculatePageIndices(validCurrentPage, totalRecords)

    val paginatedData = allOtherIncomes.slice(startIndex, endIndex)

    val paginationViewModel = createPaginationViewModel(
      currentPage = validCurrentPage,
      totalPages = totalPages,
      baseUrl = baseUrl
    )

    OtherIncomesPaginationResult(
      paginatedData = paginatedData,
      paginationViewModel = paginationViewModel,
      totalRecords = totalRecords,
      currentPage = validCurrentPage,
      totalPages = totalPages
    )
  }

  def paginateValidationErrors(
    allErrors: Seq[ValidationError],
    currentPage: Int = 1,
    baseUrl: String
  ): ValidationErrorsPaginationResult = {

    val errors = allErrors

    val totalRecords     = errors.length
    val totalPages       = calculateTotalPages(totalRecords)
    val validCurrentPage = validateCurrentPage(currentPage, totalPages)

    val (startIndex, endIndex) = calculatePageIndices(validCurrentPage, totalRecords)

    val paginatedData = errors.slice(startIndex, endIndex)

    val paginationViewModel = createPaginationViewModel(
      currentPage = validCurrentPage,
      totalPages = totalPages,
      baseUrl = baseUrl
    )

    ValidationErrorsPaginationResult(
      paginatedData = paginatedData,
      paginationViewModel = paginationViewModel,
      totalRecords = totalRecords,
      currentPage = validCurrentPage,
      totalPages = totalPages
    )
  }

  private def calculateTotalPages(totalRecords: Int): Int =
    Math.ceil(totalRecords.toDouble / config.recordsPerPage).toInt

  private def validateCurrentPage(currentPage: Int, totalPages: Int): Int =
    Math.max(1, Math.min(currentPage, totalPages))

  private def calculatePageIndices(currentPage: Int, totalRecords: Int): (Int, Int) = {
    val startIndex = (currentPage - 1) * config.recordsPerPage
    val endIndex   = Math.min(startIndex + config.recordsPerPage, totalRecords)
    (startIndex, endIndex)
  }

  private def createPaginationViewModel(
    currentPage: Int,
    totalPages: Int,
    baseUrl: String
  ): PaginationViewModel =
    if (totalPages <= 1) {
      PaginationViewModel()
    } else {
      val items    = generatePageItems(currentPage, totalPages, baseUrl)
      val previous = createPreviousLink(currentPage, baseUrl)
      val next     = createNextLink(currentPage, totalPages, baseUrl)

      PaginationViewModel(
        items = items,
        previous = previous,
        next = next
      )
    }

  private def createPreviousLink(currentPage: Int, baseUrl: String): Option[PaginationLinkViewModel]              =
    if (currentPage > 1) {
      Some(PaginationLinkViewModel(s"$baseUrl?page=${currentPage - 1}").withText("site.pagination.previous"))
    } else {
      None
    }
  private def createNextLink(currentPage: Int, totalPages: Int, baseUrl: String): Option[PaginationLinkViewModel] =
    if (currentPage < totalPages) {
      Some(PaginationLinkViewModel(s"$baseUrl?page=${currentPage + 1}").withText("site.pagination.next"))
    } else {
      None
    }

  private def generatePageItems(
    currentPage: Int,
    totalPages: Int,
    baseUrl: String
  ): Seq[PaginationItemViewModel] = {
    val pageRange = calculatePageRange(currentPage, totalPages)
    val items     = scala.collection.mutable.ListBuffer[PaginationItemViewModel]()

    if (pageRange.head > 1) {
      items += PaginationItemViewModel("1", s"$baseUrl?page=1").withCurrent(1 == currentPage)
      if (pageRange.head > 2) {
        items += PaginationItemViewModel.ellipsis()
      }
    }

    pageRange.foreach { page =>
      items += PaginationItemViewModel(
        number = page.toString,
        href = s"$baseUrl?page=$page"
      ).withCurrent(page == currentPage)
    }

    if (pageRange.last < totalPages) {
      if (pageRange.last < totalPages - 1) {
        items += PaginationItemViewModel.ellipsis()
      }
      items += PaginationItemViewModel(totalPages.toString, s"$baseUrl?page=$totalPages").withCurrent(
        totalPages == currentPage
      )
    }

    items.toSeq
  }

  private def calculatePageRange(currentPage: Int, totalPages: Int): Range = {
    val halfVisible = config.maxVisiblePages / 2
    val startPage   = Math.max(1, currentPage - halfVisible)
    val endPage     = Math.min(totalPages, startPage + config.maxVisiblePages - 1)

    val adjustedStartPage = if (endPage - startPage < config.maxVisiblePages - 1) {
      Math.max(1, endPage - config.maxVisiblePages + 1)
    } else startPage

    adjustedStartPage to endPage
  }
}
