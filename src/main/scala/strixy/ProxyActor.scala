package strixy

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.IO
import spray.can.Http
import spray.http.HttpHeaders.Origin
import spray.http.{HttpHeaders, HttpRequest, SomeOrigins, Uri}

object ProxyActor {
  def props() = Props[ProxyActor]()
}

class ProxyActor extends Actor with ActorLogging {

  import context.system

  val io = IO(Http)

  override def receive = {
    case _: Http.Connected =>
      sender() ! Http.Register(self)
    case incoming: HttpRequest =>
      val incomingUri = incoming.uri
      log.info("Incoming URL: " + incomingUri)

      val url =
        if (incomingUri.path.startsWith(Uri.Path./)) {
          incomingUri.path.dropChars(1).toString()
        } else incomingUri.path.toString()

      val outgoingUri =
        Uri(url)
          .withQuery(incomingUri.query)
          .withFragment(incomingUri.fragment.getOrElse(""))

      log.info("Outgoing URL: " + outgoingUri)

      val headers = incoming.headers filter {
        case _: HttpHeaders.Host => false
        case _ => true
      }
      val outgoing = incoming.copy(uri = outgoingUri, headers = headers)

      val origins = SomeOrigins(incoming.header[Origin].get.originList)

      val responseActor = context.actorOf(ResponseActor.props(sender(), origins, incoming.method))
      io.tell(outgoing, responseActor)
    case Http.Closed =>
    case msg =>
      log.warning("Unhandled message: {}", msg)
  }
}
