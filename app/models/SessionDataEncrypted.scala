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

package models

import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

final case class SessionDataEncrypted(
  data: SensitiveWrapper[SessionData]
) {

  def toSessionData: SessionData = data.decryptedValue
}

object SessionDataEncrypted {

  def fromSessionData(sessionData: SessionData): SessionDataEncrypted =
    SessionDataEncrypted(SensitiveWrapper(sessionData))

  given reads(using crypto: Encrypter & Decrypter): Reads[SessionDataEncrypted] =
    (__ \ "data")
      .read(using
        SensitiveWrapper.reads[SessionData](using
          summon[Reads[SessionData]],
          crypto
        )
      )
      .map(SessionDataEncrypted.apply)

  given writes(using crypto: Encrypter & Decrypter): OWrites[SessionDataEncrypted] =
    (__ \ "data")
      .write(using
        SensitiveWrapper.writes[SessionData](using
          summon[Writes[SessionData]],
          crypto
        )
      )
      .contramap[SessionDataEncrypted](_.data)

  given format(using crypto: Encrypter & Decrypter): OFormat[SessionDataEncrypted] =
    OFormat(reads, writes)
}
