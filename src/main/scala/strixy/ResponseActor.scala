package strixy

import Implicits._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import spray.http.HttpHeaders._
import spray.http._

object ResponseActor {
  def props(destination: ActorRef, origins: SomeOrigins, method: HttpMethod) =
    Props(classOf[ResponseActor], destination, origins, method)
}

class ResponseActor(destination: ActorRef, origins: SomeOrigins, method: HttpMethod) extends Actor with ActorLogging {
  def receive = {
    case incoming: HttpResponse =>
      val outgoing = incoming |> removeHeaders |> addCorsHeaders(origins) |> fakeAuthorizedOptionsRequest(method)

      destination ! outgoing
      context.stop(self)
    case msg =>
      log.warning("Unhandled message: {}", msg)
      context.stop(self)
  }

  def removeHeaders(response: HttpResponse) = {
    val headers = response.headers.filter {
      case _: `Access-Control-Allow-Origin` => false
      case _: `Access-Control-Allow-Methods` => false
      case _: `Access-Control-Allow-Headers` => false
      case _: `Access-Control-Allow-Credentials` => false
      case header if header.name == "X-Frame-Options" => false
      case _ => true
    }
    response.copy(headers = headers)
  }

  def addCorsHeaders(origins: SomeOrigins)(response: HttpResponse) = {
    val headers = response.headers ++ corsHeaders(origins)
    response.copy(headers = headers)
  }

  def corsHeaders(origins: SomeOrigins) = {
    List(
      `Access-Control-Allow-Origin`(origins),
      `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.OPTIONS, HttpMethods.HEAD, HttpMethods.DELETE, HttpMethods.PATCH, HttpMethods.PUT),
      `Access-Control-Allow-Headers`("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, " +
        "Accept-Language, Host, Referer, User-Agent, Overwrite, Destination, Depth, X-Token, X-File-Size, " +
        "If-Modified-Since, X-File-Name, Cache-Control, Cookie, Authorization"),
      `Access-Control-Allow-Credentials`(allow = true),
      `Access-Control-Max-Age`(3600))
  }

  // In some browsers OPTIONS requests will not contain credentials in their calls. Which may be required by some servers.
  def fakeAuthorizedOptionsRequest(method: HttpMethod)(response: HttpResponse) = {
    if (response.status == StatusCodes.Unauthorized && method == HttpMethods.OPTIONS) {
      response.copy(status = StatusCodes.OK)
    } else response
  }
}
