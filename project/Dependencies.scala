import sbt._

object Dependencies {
  val akkaVersion = "2.3.6"
  val sprayVersion = "1.3.1"

  val sprayCan = "io.spray" %% "spray-client" % sprayVersion
  val sprayClient = "io.spray" %% "spray-client" % sprayVersion

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

  val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"

  val All = Seq(sprayCan, sprayClient, akkaActor, akkaSlf4j, logback)

  object Resolvers {
    val spray = "spray repo" at "http://repo.spray.io"

    val all = Seq(spray)
  }

}
