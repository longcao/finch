package io.finch.response

import com.twitter.io.Buf
import com.twitter.io.Buf.Utf8

/**
 * An abstraction that is responsible for encoding the response of type `A`.
 */
trait EncodeResponse[-A] {
  def apply(rep: A): Buf
  def contentType: String
  def charset: Option[String] = Some("utf-8")
}

object EncodeResponse {

  /**
   * Convenience method for creating new [[io.finch.response.EncodeResponse EncodeResponse]] instances.
   */
  def apply[A](ct: String, cs: Option[String] = Some("utf-8"))(fn: A => Buf): EncodeResponse[A] =
    new EncodeResponse[A] {
      override def apply(rep: A): Buf = fn(rep)
      override def contentType: String = ct
      override def charset: Option[String] = cs
    }

  /**
   * Convenience method for creating new [[io.finch.response.EncodeResponse EncodeResponse]] instances
   * that treat String contents.
   */
  def fromString[A](ct: String, cs: Option[String] = Some("utf-8"))(fn: A => String): EncodeResponse[A] =
    apply(ct, cs)(fn andThen Utf8.apply)

  /**
   * Converts [[io.finch.response.EncodeAnyResponse EncodeAnyResponse]] into
   * [[io.finch.response.EncodeResponse EncodeResponse]].
   */
  implicit def anyToConcreteEncode[A](implicit e: EncodeAnyResponse): EncodeResponse[A] =
    new EncodeResponse[A] {
      def apply(rep: A): Buf = e(rep)
      def contentType: String = e.contentType
    }

  /**
   * Allows to pass raw strings to a [[ResponseBuilder]].
   */
  implicit val encodeString: EncodeResponse[String] =
    EncodeResponse.fromString[String]("text/plain")(identity)

  // TODO: Make it JSON-agnostic
  implicit val encodeUnit: EncodeResponse[Unit] =
    EncodeResponse("application/json")(_ => Buf.Empty)

  /**
   * Allows to pass `Buf` to a [[ResponseBuilder]].
   */
  implicit val encodeBuf: EncodeResponse[Buf] =
    EncodeResponse("application/octet-stream", None)(identity)
}

/**
 * An abstraction that is responsible for encoding the response of a generic type.
 */
trait EncodeAnyResponse {
  def apply[A](rep: A): Buf
  def contentType: String
}
