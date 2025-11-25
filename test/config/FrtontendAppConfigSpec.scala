/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.charitiesclaims.config

import util.BaseSpec

import scala.concurrent.duration.Duration

class FrontendAppConfigSpec extends BaseSpec {

  "FrontendAppConfig" - {
    "return appName" in {
      testFrontendAppConfig.appName shouldBe "foo-bar-frontend"
    }

    "return mongoDbTTL" in {
      testFrontendAppConfig.mongoDbTTL shouldBe Duration.apply(15, "minutes")
    }

    "return loginUrl" in {
      testFrontendAppConfig.loginUrl shouldBe "http://foo.com/login"
    }

    "return loginContinueUrl" in {
      testFrontendAppConfig.loginContinueUrl shouldBe "http://foo.com/bar"
    }

    "return signOutUrl" in {
      testFrontendAppConfig.signOutUrl shouldBe "http://foo.com/sign-out"
    }

    "return betaFeedbackUrl" in {
      testFrontendAppConfig.betaFeedbackUrl shouldBe "http://foo.com/beta-feedback"
    }

    "return researchUrl" in {
      testFrontendAppConfig.researchUrl shouldBe "http://foo.com/research"
    }

    "return enableLanguageSwitching" in {
      testFrontendAppConfig.enableLanguageSwitching shouldBe true
    }

    "return timeoutInSeconds" in {
      testFrontendAppConfig.timeoutInSeconds shouldBe 10
    }

    "return countdownInSeconds" in {
      testFrontendAppConfig.countdownInSeconds shouldBe 5
    }

    "return title with service name" in {
      testFrontendAppConfig.pageTitleWithServiceName(
        "Test Page",
        "Test Service"
      ) shouldBe "Test Page - Test Service - GOV.UK"
    }

    "return title with error prefix when there are errors" in {
      testFrontendAppConfig.pageTitleWithServiceNameAndError(
        "Test Page",
        "Test Service",
        "Error:",
        true
      ) shouldBe "Error: Test Page - Test Service - GOV.UK"
    }

    "return title with error prefix when there are no errors" in {
      testFrontendAppConfig.pageTitleWithServiceNameAndError(
        "Test Page",
        "Test Service",
        "Error: ",
        false
      ) shouldBe "Test Page - Test Service - GOV.UK"
    }

  }

}
