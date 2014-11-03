package org.syzygist

import org.parboiled2.{ ParseError, Parser, ParserInput, Rule1 }
import org.parboiled2.support.Unpack
import scala.language.experimental.macros
import scalaz.\/
import scalaz.concurrent.{ Future, Task }
import scalaz.syntax.either._
import shapeless.HList

package object parse {
  /**
   * A delivery scheme that returns the parse result in a `scalaz.\/`.
   */
  implicit def DisjunctionDeliveryScheme[L <: HList, Out](implicit
    unpack: Unpack.Aux[L, Out]
  ): Parser.DeliveryScheme[L] { type Result = ParseError \/ Out } =
    new Parser.DeliveryScheme[L] {
      type Result = ParseError \/ Out

      def success(result: L) = unpack(result).right
      def parseError(error: ParseError) = error.left
      def failure(error: Throwable) = throw error
    }

  /**
   * A parser that has a distinguished rule that returns a single value.
   */
  abstract class ValueParser(val input: ParserInput) extends Parser {
    type Value

    def value: Rule1[Value]
  }

  /**
   * Creates a parsing function given a parser type.
   */
  def parseWith[P <: ValueParser]: String => Task[P#Value] =
    macro ParserMacros.parseWithImpl[P]
}
