package strixy

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration._

object Strixy extends App {
  implicit val system = ActorSystem("clean-proxy")
  import system.dispatcher

  val config = ConfigFactory.load().getConfig("strixy")

  val proxyActor = system.actorOf(ProxyActor.props(), "proxy")
  implicit val timeout = Timeout(10.seconds)

  val eventualBind = IO(Http) ? Http.Bind(proxyActor, config.getString("interface"), config.getInt("port"))

  eventualBind foreach {
    case Http.Bound(address) =>
      println(s"Bound to $address")
    case other =>
      println(other)
  }
}
