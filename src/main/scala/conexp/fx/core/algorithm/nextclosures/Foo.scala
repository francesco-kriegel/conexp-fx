package conexp.fx.core.algorithm.nextclosures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import conexp.fx.core.context.MatrixContext

object Foo extends App {
  override def main(args: Array[String]) {
    println("Hello Foo!")
    new MatrixContext(false)
    val c = new Complex(2.3, 4.5)
    println(c.re)
    val t:Tree = Sum(Sum(Var("x"), Var("x")), Sum(Const(7), Var("y")))
    val e:Environment = { case "x"=>5 case"y"=>7 }
    println(t)
    println(eval(t, e))
    println(derive(t,"x"))
    println(derive(t,"y"))
    println(new Date(1,2,3)<new Date(1,2,4))
    
    val xs = for {
      i <- 1 to 5
      j <- 10 to 11
    } yield i+j
    println(xs)
    
    val f = Future { 
      Thread sleep 2345
      42.18
      }
    f.onComplete {
      case Success(x) => println(x)
      case Failure(e) => println("sys.error")
    }
    oncePerSecond(timeFlies)
    //    oncePerSecond(()=>print("."))
  }

  def oncePerSecond(callback: => Unit) {
    while (true) {
      callback
      Thread sleep 1000
    }
  }

  def timeFlies {
    println("Time flies!")
  }

  class Complex(real: Double, imaginary: Double) {
    def re = real
    def im() = imaginary
  }

  abstract class Tree
  case class Sum(l: Tree, r: Tree) extends Tree
  case class Var(n: String) extends Tree
  case class Const(v: Int) extends Tree

  type Environment = String => Int

  def eval(t: Tree, e: Environment): Int = t match {
    case Sum(l, r) => eval(l, e) + eval(r, e)
    case Var(n) => e(n)
    case Const(v) => v
  }

  def derive(t: Tree, m: String): Tree = t match {
    case Sum(l, r) => Sum(derive(l, m), derive(r, m))
    case Var(n) if (m == n) => Const(1)
    case _ => Const(0)
  }
  
  trait Ord {
    def < (that: Any): Boolean
    def <=(that: Any): Boolean = this==that || this<that
    def > (that: Any): Boolean = !(this<=that)
    def >=(that: Any): Boolean = !(this<that)
  }
  
  class Date(y: Int, m:Int,d:Int) extends Ord{
    def year=y
    def month=m
    def day=d
    
    override def toString():String = day+"."+month+"."+year
    
    override def equals(that:Any):Boolean = that.isInstanceOf[Date] && {val o=that.asInstanceOf[Date]
    this.year==o.year&&this.month==o.month&&this.day==o.day}
    
    def <(that:Any):Boolean = {
      if (!that.isInstanceOf[Date])
        sys.error("foo")
      val o = that.asInstanceOf[Date]
      this.year<o.year||(this.year==o.year&&this.month<o.month)||(this.year==o.year && this.month==o.month && this.day<o.day)
    }
  }
  
}