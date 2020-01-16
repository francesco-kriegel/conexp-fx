package conexp.fx.core.dl

import scala.collection.JavaConverters.asJavaCollectionConverter
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.NoPosition
import scala.util.parsing.input.Position
import scala.util.parsing.input.Reader

import org.semanticweb.owlapi.model.IRI

trait ELParsingError
case class ELLexerError(msg: String) extends ELParsingError
case class ELParserError(msg: String) extends ELParsingError

sealed trait ELToken
case class CNAME(value: String) extends ELToken
case class RNAME(value: String) extends ELToken
case object TOP extends ELToken
case object BOT extends ELToken
case object AND extends ELToken
case object EXISTS extends ELToken
case object SOME extends ELToken
case object DOT extends ELToken
case object OPENBRACE extends ELToken
case object CLOSEBRACE extends ELToken

object ELLexer extends RegexParsers {

  override def skipWhitespace = true
  override val whiteSpace = "[ \t\r\f\n]+".r

  def cname: Parser[CNAME] = "[A-Z][a-zA-Z0-9]*".r ^^ { value ⇒ CNAME(value) }
  def rname: Parser[RNAME] = "[a-z][a-zA-Z0-9]*".r ^^ { value ⇒ RNAME(value) }
  def top: Parser[ELToken] = ("TOP" | "top" | "Top" | "THING" | "thing" | "Thing" | "⊤") ^^ { _ ⇒ TOP }
  def bot: Parser[ELToken] = ("BOT" | "bot" | "Bot" | "NOTHING" | "nothing" | "Nothing" | "⊥") ^^ { _ ⇒ BOT }
  def and: Parser[ELToken] = ("AND" | "and" | "And" | "⊓") ^^ { _ ⇒ AND }
  def exists: Parser[ELToken] = ("EXISTS" | "exists" | "Exists" | "∃") ^^ { _ ⇒ EXISTS }
  def some: Parser[ELToken] = ("SOME" | "some" | "Some") ^^ { _ ⇒ SOME }
  def dot: Parser[ELToken] = "." ^^ { _ ⇒ DOT }
  def openbrace: Parser[ELToken] = "(" ^^ { _ ⇒ OPENBRACE }
  def closebrace: Parser[ELToken] = ")" ^^ { _ ⇒ CLOSEBRACE }

  def tokens: Parser[List[ELToken]] = phrase(rep1(and | exists | some | dot | openbrace | closebrace | top | bot | cname | rname))

  def apply(source: String): Either[ELLexerError, List[ELToken]] = {
    parse(tokens, source) match {
      case NoSuccess(msg, next)  ⇒ Left(ELLexerError(msg + " at line " + next.pos.line + ", column " + next.pos.column + "\r\n" + next.pos.longString))
      case Success(result, next) ⇒ Right(result)
    }
  }

}

object ELParser extends Parsers {

  override type Elem = ELToken

  class ELTokenReader(tokens: Seq[ELToken]) extends Reader[ELToken] {
    override def first: ELToken = tokens.head
    override def atEnd: Boolean = tokens.isEmpty
    override def pos: Position = NoPosition
    override def rest: Reader[ELToken] = new ELTokenReader(tokens.tail)
  }

  private def top: Parser[ELConceptDescription] = accept("top", { case TOP ⇒ ELConceptDescription.top() })

  private def bot: Parser[ELConceptDescription] = accept("bot", { case BOT ⇒ ELConceptDescription.bot() })

  private def cname: Parser[ELConceptDescription] = accept("cname", { case CNAME(value) ⇒ ELConceptDescription.conceptName(IRI.create(value)) })

  private def rname: Parser[IRI] = accept("rname", { case RNAME(value) ⇒ IRI.create(value) })

  private def conjunction: Parser[ELConceptDescription] = {
    (top | bot | cname | existentialRestriction) ~ rep(AND ~> (top | bot | cname | existentialRestriction)) ^^ {
      case c ~ cs ⇒ ELConceptDescription.conjunction((c :: cs).asJavaCollection)
    }
  }

  private def existentialRestriction: Parser[ELConceptDescription] = {
    (EXISTS ~ rname ~ DOT ~ top) ^^ {
      case _ ~ r ~ _ ~ a ⇒ ELConceptDescription.existentialRestriction(r, a)
    } | (EXISTS ~ rname ~ DOT ~ bot) ^^ {
      case _ ~ r ~ _ ~ a ⇒ ELConceptDescription.existentialRestriction(r, a)
    } | (EXISTS ~ rname ~ DOT ~ cname) ^^ {
      case _ ~ r ~ _ ~ a ⇒ ELConceptDescription.existentialRestriction(r, a)
    } | (EXISTS ~ rname ~ DOT ~ existentialRestriction) ^^ {
      case _ ~ r ~ _ ~ c ⇒ ELConceptDescription.existentialRestriction(r, c)
    } | (EXISTS ~ rname ~ DOT ~ OPENBRACE ~ conjunction ~ CLOSEBRACE) ^^ {
      case _ ~ r ~ _ ~ _ ~ c ~ _ ⇒ ELConceptDescription.existentialRestriction(r, c)
    } | (OPENBRACE ~ rname ~ SOME ~ top ~ CLOSEBRACE) ^^ {
      case _ ~ r ~ _ ~ a ~ _ ⇒ ELConceptDescription.existentialRestriction(r, a)
    } | (OPENBRACE ~ rname ~ SOME ~ bot ~ CLOSEBRACE) ^^ {
      case _ ~ r ~ _ ~ a ~ _ ⇒ ELConceptDescription.existentialRestriction(r, a)
    } | (OPENBRACE ~ rname ~ SOME ~ cname ~ CLOSEBRACE) ^^ {
      case _ ~ r ~ _ ~ a ~ _ ⇒ ELConceptDescription.existentialRestriction(r, a)
    } | (OPENBRACE ~ rname ~ SOME ~ existentialRestriction ~ CLOSEBRACE) ^^ {
      case _ ~ r ~ _ ~ c ~ _ ⇒ ELConceptDescription.existentialRestriction(r, c)
    } | (OPENBRACE ~ rname ~ SOME ~ OPENBRACE ~ conjunction ~ CLOSEBRACE ~ CLOSEBRACE) ^^ {
      case _ ~ r ~ _ ~ _ ~ c ~ _ ~ _ ⇒ ELConceptDescription.existentialRestriction(r, c)
    }
  }

  def apply(tokens: Seq[ELToken]): Either[ELParserError, ELConceptDescription] = {
    val reader = new ELTokenReader(tokens)
    phrase(conjunction)(reader) match {
      case NoSuccess(msg, next)  ⇒ Left(ELParserError(msg + " at line " + next.pos.line + ", column " + next.pos.column + "\r\n" + next.pos.longString))
      case Success(result, next) ⇒ Right(result)
    }
  }

  implicit def read(code: String): ELConceptDescription = {
    val lexerResult = ELLexer(code)
    if (lexerResult.isLeft) throw new RuntimeException(lexerResult.left.get.msg)
    else {
      val parserResult = ELParser(lexerResult.right.get)
      if (parserResult.isLeft) throw new RuntimeException(parserResult.left.get.msg)
      else {
        return parserResult.right.get
      }
    }
  }

}
