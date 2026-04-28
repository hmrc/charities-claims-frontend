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

package forms

import models.GasdsClaimType
import play.api.data.Form
import play.api.data.Forms.*
import models.GasdsClaimTypeCheckBox.*

import javax.inject.Inject

class GasdsClaimTypeFormProvider @Inject() extends Mappings {

  def apply(): Form[GasdsClaimType] =
    Form(
      "value" -> set(text("gasdsClaimType.error.required"))
        .verifying(nonEmptySet("gasdsClaimType.error.required"))
        .transform[GasdsClaimType](fromSet, toSet)
    )

  private def fromSet(values: Set[String]): GasdsClaimType =
    GasdsClaimType(
      topUp = values.contains(topUp.toString),
      communityBuildings = values.contains(communityBuildings.toString),
      connectedCharity = values.contains(connectedCharity.toString)
    )

  def toSet(model: GasdsClaimType): Set[String] =
    Set(
      Option.when(model.topUp)(topUp.toString),
      Option.when(model.communityBuildings)(communityBuildings.toString),
      Option.when(model.connectedCharity)(connectedCharity.toString)
    ).flatten
}
