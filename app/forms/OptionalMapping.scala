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

import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint

case class OptionalMapping[T](wrapped: Mapping[T], constraints: Seq[Constraint[Option[T]]] = Nil)
    extends Mapping[Option[T]] {
  override val format: Option[(String, Seq[Any])] = wrapped.format

  val key = wrapped.key

  /** Constructs a new Mapping based on this one, by adding new constraints.
    *
    * For example:
    * {{{
    *   import play.api.data._
    *   import validation.Constraints._
    *
    *   Form("phonenumber" -> text.verifying(required) )
    * }}}
    *
    * @param addConstraints
    *   the constraints to add
    * @return
    *   the new mapping
    */
  def verifying(addConstraints: Constraint[Option[T]]*): Mapping[Option[T]] =
    this.copy(constraints = constraints ++ addConstraints.toSeq)

  /** Binds this field, i.e. constructs a concrete value from submitted data.
    *
    * @param data
    *   the submitted data
    * @return
    *   either a concrete value of type `T` or a set of error if the binding failed
    */
  def bind(data: Map[String, String]): Either[Seq[FormError], Option[T]] =
    data.keys
      .filter(p => p == key || p.startsWith(s"$key.") || p.startsWith(s"$key["))
      .map(k => data.get(k).filterNot(_.trim.isEmpty))
      .collectFirst { case Some(v) => v.trim }
      .map(_ => wrapped.bind(data).map(Some(_)))
      .getOrElse(Right(None))
      .flatMap(applyConstraints)

  /** Unbinds this field, i.e. transforms a concrete value to plain data.
    *
    * @param value
    *   the value to unbind
    * @return
    *   the plain data
    */
  def unbind(value: Option[T]): Map[String, String] =
    value.map(wrapped.unbind).getOrElse(Map.empty)

  /** Unbinds this field, i.e. transforms a concrete value to plain data, and applies validation.
    *
    * @param value
    *   the value to unbind
    * @return
    *   the plain data and any errors in the plain data
    */
  def unbindAndValidate(value: Option[T]): (Map[String, String], Seq[FormError]) = {
    val errors = collectErrors(value)
    value.map(wrapped.unbindAndValidate).map(r => r._1 -> (r._2 ++ errors)).getOrElse(Map.empty -> errors)
  }

  /** Constructs a new Mapping based on this one, adding a prefix to the key.
    *
    * @param prefix
    *   the prefix to add to the key
    * @return
    *   the same mapping, with only the key changed
    */
  def withPrefix(prefix: String): Mapping[Option[T]] =
    copy(wrapped = wrapped.withPrefix(prefix))

  /** Sub-mappings (these can be seen as sub-keys). */
  val mappings: Seq[Mapping[?]] = wrapped.mappings
}
