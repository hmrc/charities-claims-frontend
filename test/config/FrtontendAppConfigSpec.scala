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

  "AppConfig" - {
    "return appName" in {
      testFrontendAppConfig.appName mustBe "foo-bar-frontend"
    }

    "return mongoDbTTL" in {
      testFrontendAppConfig.mongoDbTTL mustBe Duration.apply(15, "minutes")
    }

    "return loginUrl" in {
      testFrontendAppConfig.loginUrl mustBe "http://foo.com/login"
    }

    "return loginContinueUrl" in {
      testFrontendAppConfig.loginContinueUrl mustBe "http://foo.com/bar"
    }

    "return signOutUrl" in {
      testFrontendAppConfig.signOutUrl mustBe "http://foo.com/sign-out"
    }
  }

}
