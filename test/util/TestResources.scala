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

package util

import scala.io.Source
import play.api.libs.json.Reads
import play.api.libs.json.Json

object TestResources {

  final def readTestResource[T](path: String): String =
    Source.fromInputStream(this.getClass.getResourceAsStream(path)).getLines().mkString("\n")

  final def readAndParseJson[T](path: String)(using Reads[T]): T =
    Json.parse(readTestResource(path)).as[T]

}
