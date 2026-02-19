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

import util.BaseSpec
import models.{Donation, OtherIncome, ValidationError}

class PaginationServiceSpec extends BaseSpec {

  val testBaseUrl = "/test-url"

  def donations(n: Int): Seq[Donation] =
    Seq.fill(n)(
      Donation(
        donationDate = "2025-01-01",
        donationAmount = BigDecimal(100)
      )
    )

  def otherIncomes(n: Int): Seq[OtherIncome] =
    (1 to n).map(i =>
      OtherIncome(
        otherIncomeItem = i,
        payerName = "Mr Smith",
        paymentDate = "2025-01-01",
        grossPayment = BigDecimal(100),
        taxDeducted = BigDecimal(20)
      )
    )

  def errors(n: Int): Seq[ValidationError] =
    Seq.fill(n)(
      ValidationError(
        field = "donations[1]",
        error = "Error message"
      )
    )

  "PaginationService" - {

    "paginateDonations" - {

      "should paginate correctly and use default page 1" in {
        val result = PaginationService.paginateDonations(donations(25), baseUrl = testBaseUrl)

        result.totalRecords shouldEqual 25
        result.totalPages shouldEqual 3
        result.currentPage shouldEqual 1
        result.paginatedData.length shouldEqual 10
      }

      "should handle invalid page numbers" in {
        // page 0 defaults to page 1
        val resultPage0 = PaginationService.paginateDonations(donations(25), 0, testBaseUrl)
        resultPage0.currentPage shouldEqual 1

        // page exceeding total defaults to last page
        val resultPageExceeds = PaginationService.paginateDonations(donations(25), 10, testBaseUrl)
        resultPageExceeds.currentPage shouldEqual 3
      }
    }

    "paginateValidationErrors" - {

      "should paginate errors correctly" in {
        val result = PaginationService.paginateValidationErrors(errors(15), 2, testBaseUrl)

        result.totalRecords shouldEqual 15
        result.totalPages shouldEqual 2
        result.currentPage shouldEqual 2
        result.paginatedData.length shouldEqual 5
      }
    }

    "paginateOtherIncomes" - {

      "should paginate other income schedules correctly (max 200 rows = 20 pages)" in {
        val result = PaginationService.paginateOtherIncomes(otherIncomes(200), 10, testBaseUrl)

        result.totalRecords shouldEqual 200
        result.totalPages shouldEqual 20
        result.currentPage shouldEqual 10
        result.paginatedData.length shouldEqual 10
      }
    }

    "pagination view model" - {

      "should return empty pagination view model when only 1 page" in {
        val result = PaginationService.paginateDonations(donations(5), 1, testBaseUrl)

        result.paginationViewModel.items shouldEqual Seq.empty
        result.paginationViewModel.previous shouldEqual None
        result.paginationViewModel.next shouldEqual None
      }

      "should include previous/next links correctly" in {
        val page1 = PaginationService.paginateDonations(donations(25), 1, testBaseUrl)
        page1.paginationViewModel.previous shouldEqual None
        page1.paginationViewModel.next shouldBe defined

        val lastPage = PaginationService.paginateDonations(donations(25), 3, testBaseUrl)
        lastPage.paginationViewModel.previous shouldBe defined
        lastPage.paginationViewModel.next shouldEqual None
      }

      "should show page 1 link is still visible when on later pages further than 5 places away from 1" in {
        val result = PaginationService.paginateDonations(donations(80), 7, testBaseUrl)

        result.totalPages shouldEqual 8
        result.paginationViewModel.items.head.number shouldEqual "1"
      }

      "should show last page link when on earlier pages further than 5 places away" in {
        val result = PaginationService.paginateDonations(donations(80), 2, testBaseUrl)

        result.totalPages shouldEqual 8
        result.paginationViewModel.items.last.number shouldEqual "8"
      }
    }
  }
}
