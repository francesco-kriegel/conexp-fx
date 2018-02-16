package conexp.fx.core.context.probabilistic

import conexp.fx.core.util.UnicodeSymbols

abstract class PropositionalFormula[M] {

  def mapVar[N](t: M => N): PropositionalFormula[N]
  def visit(t: PropositionalFormula[M] => Unit)
  def hasModel(v: M => Boolean): Boolean

  def hasModel2(valuation: M => Boolean): Boolean = {
    (this flatten) mapVar[Boolean] valuation fold(
        false,
        true,
        !_, //(v: Boolean) => !v,
        _.forall(_==true), //(vs: Set[Boolean]) => vs.foldLeft(true)((x: Boolean, y: Boolean) => x && y),
        _.exists(_==true) //(vs: Set[Boolean]) => vs.foldLeft(false)((x: Boolean, y: Boolean) => x || y)
    )
  }

  def conjunctiveNormalForm: PropositionalFormula[M] = {
    this map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae applyDeMorganRule) map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae removeDoubleNegations) map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae applyDistributivityLawForOr) map (PropositionalFormulae flattenSingletonAndsAndOrs)
  }

  def cnf = conjunctiveNormalForm

  def disjunctiveNormalForm: PropositionalFormula[M] = {
    this map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae applyDeMorganRule) map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae removeDoubleNegations) map (PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae applyDistributivityLawForAnd) map (PropositionalFormulae flattenSingletonAndsAndOrs)
  }

  def dnf = disjunctiveNormalForm

  def map(t: PropositionalFormula[M] => PropositionalFormula[M]): PropositionalFormula[M] = {
    val that = t(this)
    that match {
      case Not(f)  => Not(f map t)
      case And(fs) => And(fs map (_ map t))
      case Or(fs)  => Or(fs map (_ map t))
      case default => that
    }
  }

  def fold(vBot: M, vTop: M, fNot: M => M, fAnd: Set[M] => M, fOr: Set[M] => M): M = {
    val that = this map ((f: PropositionalFormula[M]) => {
      f match {
        case Bot()   => Var(vBot)
        case Top()   => Var(vTop)
        case Var(m)  => this
        case Not(g)  => Var(fNot(g.fold(vBot, vTop, fNot, fAnd, fOr)))
        case And(gs) => Var(fAnd(gs map (_ fold (vBot, vTop, fNot, fAnd, fOr))))
        case Or(gs)  => Var(fOr(gs map (_ fold (vBot, vTop, fNot, fAnd, fOr))))
      }
    })
    that.asInstanceOf[Var[M]].m
  }

  def flatten = map(PropositionalFormulae flattenSingletonAndsAndOrs) map (PropositionalFormulae removeDoubleNegations)

}

case class Bot[M]() extends PropositionalFormula[M] {
  override def toString = UnicodeSymbols.BOT
  override def mapVar[N](t: M => N) = Top[N]()
  override def visit(t: PropositionalFormula[M] => Unit) { t(this) }
  override def hasModel(v: M => Boolean): Boolean = false
}

case class Top[M]() extends PropositionalFormula[M] {
  override def toString = UnicodeSymbols.TOP
  override def mapVar[N](t: M => N) = Top[N]()
  override def visit(t: PropositionalFormula[M] => Unit) { t(this) }
  override def hasModel(v: M => Boolean): Boolean = true
}

case class Var[M](m: M) extends PropositionalFormula[M] {
  override def toString = m.toString
  override def mapVar[N](t: M => N) = Var(t(m))
  override def visit(t: PropositionalFormula[M] => Unit) { t(this) }
  override def hasModel(v: M => Boolean): Boolean = v(m)
}
case class Not[M](f: PropositionalFormula[M]) extends PropositionalFormula[M] {
  override def toString = UnicodeSymbols.NEG + "(" + f + ")"
  override def mapVar[N](t: M => N) = Not(f mapVar t)
  override def visit(t: PropositionalFormula[M] => Unit) {
    t(this)
    f visit t
  }
  override def hasModel(v: M => Boolean): Boolean = !(f hasModel v)
}
case class And[M](fs: Set[PropositionalFormula[M]]) extends PropositionalFormula[M] {

  def this(f:PropositionalFormula[M], g:PropositionalFormula[M]){
    this(Set(f,g))
  }

  override def toString = fs.mkString("(", UnicodeSymbols.WEDGE, ")")
  override def mapVar[N](t: M => N) = And(fs map (_ mapVar t))
  override def visit(t: PropositionalFormula[M] => Unit) {
    t(this)
    fs map (_ visit t)
  }
  override def hasModel(v: M => Boolean): Boolean = fs.forall(_ hasModel v)
}
case class Or[M](fs: Set[PropositionalFormula[M]]) extends PropositionalFormula[M] {

  def this(f:PropositionalFormula[M], g:PropositionalFormula[M]){
    this(Set(f,g))
  }

  override def toString = fs.mkString("(", UnicodeSymbols.VEE, ")")
  override def mapVar[N](t: M => N) = Or(fs map (_ mapVar t))
  override def visit(t: PropositionalFormula[M] => Unit) {
    t(this)
    fs map (_ visit t)
  }
  override def hasModel(v: M => Boolean): Boolean = fs.exists(_ hasModel v)
}

//object PropositionalFormulaCompiler extends RegexParsers {
//  
//  override def skipWhitespace = true
//  override val whiteSpace = "[ \t\r\f\n]+".r
//
//  sealed trait PropositionalToken
//  case object AND extends PropositionalToken
//  case object OR extends PropositionalToken
//  case object NOT extends PropositionalToken
//  case object IMPLIES extends PropositionalToken
//  case object IF extends PropositionalToken
//  case object IFF extends PropositionalToken
//  case object TRUE extends PropositionalToken
//  case object FALSE extends PropositionalToken
//  case class VAR(name:String) extends PropositionalToken
//
//  val t_and: Parser[PropositionalToken] = ("and" | "AND" | UnicodeSymbols.WEDGE ) ^^ { _ => AND }
//  val t_or = ("or" | "OR" | UnicodeSymbols.VEE) ^^ { _=>OR }
//  val t_not = ("not" | "NOT" | UnicodeSymbols.NEG) ^^ {_=>NOT}
//  val t_implies = ("implies" | "IMPLIES" | "only if" | "ONLY IF" | UnicodeSymbols.TO) ^^{_=>IMPLIES}
//  val t_if = ("if" | "IF" | UnicodeSymbols.FROM) ^^{_=>IF}
//  val t_iff = ("iff" | "IFF" | "if and only if" | "IF AND ONLY IF" |UnicodeSymbols.FROMTO)^^{_=>IFF}
//  val t_true = ("true"|"TRUE"|"top"|"TOP"|"1"| UnicodeSymbols.TOP) ^^ { _=>TRUE }
//
//}

object PropositionalFormulae {

  def Implication[M](premise:PropositionalFormula[M],conclusion:PropositionalFormula[M]): PropositionalFormula[M] = {
    new Or(Not(premise),conclusion)
  }

  def Biimplication[M](f: PropositionalFormula[M],g:PropositionalFormula[M]):PropositionalFormula[M] ={
    new And(Implication(f,g),Implication(g,f))
  }

  def Xor[M](f:PropositionalFormula[M],g:PropositionalFormula[M]):PropositionalFormula[M] = {
    new Or(new And(f,Not(g)),new And(Not(f),g))
  }

  def Nand[M](f:PropositionalFormula[M],g:PropositionalFormula[M]):PropositionalFormula[M] = {
    Not(new And(f,g))
  }

  def Nor[M](f:PropositionalFormula[M],g:PropositionalFormula[M]):PropositionalFormula[M] = {
    Not(new Or(f,g))
  }

  def Xnor[M](f:PropositionalFormula[M],g:PropositionalFormula[M]):PropositionalFormula[M] = {
    Not(Xor(f,g))
  }

  def parsePropositionalFormula(string: String): PropositionalFormula[String] = {
    val input: String = (string trim)
    val _input: String = (input toLowerCase)
    val inbraces: String = input.substring(input.indexOf("(") + 1, input.lastIndexOf(")"))
    if (_input startsWith "not") {
      new Not(parsePropositionalFormula(inbraces))
    } else if (_input startsWith "and") {
      val conjuncts: List[String] = List()

      new And(null)
    } else if (_input startsWith "or") {

    } else {
      new Var(input)
    }
    null
  }

  /**
   * applies the rule "not ( f and g ) -> (not f) or (not g)
   * and the according rule resulting by exchaning and and or
   * with the goal to push negations inside
   */
  def applyDeMorganRule[M](f: PropositionalFormula[M]): PropositionalFormula[M] = f match {
    case Not(g) => g match {
      case And(hs) => {
        new Or(hs.map(new Not[M](_)))
      }
      case Or(hs) => {
        new And(hs.map(new Not[M](_)))
      }
      case default => f
    }
    case default => f
  }

  def removeDoubleNegations[M](f: PropositionalFormula[M]): PropositionalFormula[M] = f match {
    case Not(Not(g)) => g
    //    case Not(g) => g match {
    //      case Not(h)  => h
    //      case default => f
    //    }
    case default     => f
  }

  def flattenSingletonAndsAndOrs[M](f: PropositionalFormula[M]): PropositionalFormula[M] = f match {
    case And(fs) => {
      if (fs.isEmpty)
        Top[M]()
      else if (fs.size == 1)
        fs.head
      else
        f
    }
    case Or(fs) => {
      if (fs.isEmpty)
        Bot[M]()
      else if (fs.size == 1)
        fs.head
      else
        f
    }
    case default => f
  }

  /**
   * applies the general distributivity law:
   * (f11 or ... or f1n) and ... and (fm1 or ... or fmn)
   * = (f11 and f21 and ... and fm1) or ...
   * and the according rule when and and or are interchanged
   */
  def applyDistributivityLaw[M](f: PropositionalFormula[M]): PropositionalFormula[M] = f match {
    case And(gs) =>
      {
        val disjunctions: Set[Or[M]] = {
          gs.filter(_.isInstanceOf[Or[M]]).map(_.asInstanceOf[Or[M]])
        }
        if (disjunctions.isEmpty)
          f
        else {
          val others = gs.filter(!_.isInstanceOf[Or[M]])
          new Or[M](cartesianProduct(disjunctions.map(_.fs)).map(conjuncts => new And[M](conjuncts ++ others)))
        }
      }
    case Or(gs) =>
      {
        val conjunctions: Set[And[M]] = {
          gs.filter(_.isInstanceOf[And[M]]).map(_.asInstanceOf[And[M]])
        }
        if (conjunctions.isEmpty)
          f
        else {
          val others = gs.filter(!_.isInstanceOf[And[M]])
          new And[M](cartesianProduct(conjunctions.map(_.fs)).map(disjuncts => new Or[M](disjuncts ++ others)))
        }
      }
    case default => f
  }

  /**
   * pushes and inside and fetches or outside
   */
  def applyDistributivityLawForAnd[M](f: PropositionalFormula[M]): PropositionalFormula[M] = f match {
    case And(gs) =>
      {
        val disjunctions: Set[Or[M]] = {
          gs.filter(_.isInstanceOf[Or[M]]).map(_.asInstanceOf[Or[M]])
        }
        if (disjunctions.isEmpty)
          f
        else {
          val others = gs.filter(!_.isInstanceOf[Or[M]])
          new Or[M](cartesianProduct(disjunctions.map(_.fs)).map(conjuncts => new And[M](conjuncts ++ others)))
        }
      }
    case default => f
  }

  /**
   * pushes or inside and fetches and outside
   */
  def applyDistributivityLawForOr[M](f: PropositionalFormula[M]): PropositionalFormula[M] = f match {
    case Or(gs) =>
      {
        val conjunctions: Set[And[M]] = {
          gs.filter(_.isInstanceOf[And[M]]).map(_.asInstanceOf[And[M]])
        }
        if (conjunctions.isEmpty)
          f
        else {
          val others = gs.filter(!_.isInstanceOf[And[M]])
          new And[M](cartesianProduct(conjunctions.map(_.fs)).map(disjuncts => new Or[M](disjuncts ++ others)))
        }
      }
    case default => f
  }

  /**
   * returns the set of those sets which contain exactly one element from each set in the input set.
   */
  def cartesianProduct[T](sets: Set[Set[T]]): Set[Set[T]] = {
    if (sets.isEmpty)
      Set.empty[Set[T]]
    else {
      val seed: Set[Set[T]] = Set(Set())
      val foldFunction: ((Set[Set[T]], Set[T]) => Set[Set[T]]) = {
        (xs: Set[Set[T]], ys: Set[T]) => xs.flatMap((x: Set[T]) => ys.map((y: T) => x + y))
      }
      sets.foldLeft(seed)(foldFunction)
    }
  }

  def cartesianProduct[A, B](as: Set[A], bs: Set[B]): Set[(A, B)] = {
    as.flatMap(a => bs.map(b => (a, b)))
  }

  def cartesianProduct[A, B, C](as: Set[A], bs: Set[B], cs: Set[C]) = {
    as.flatMap(a => bs.flatMap(b => cs.map(c => (a, b, c))))
  }

}