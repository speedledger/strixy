import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import sbtdocker.Plugin.DockerKeys._
import sbtdocker._

name := "strixy"

version := "1.0"

libraryDependencies := Dependencies.All

resolvers := Dependencies.Resolvers.all

packageArchetype.java_application

sbtdocker.Plugin.dockerSettings

docker <<= (docker dependsOn (stage in Universal))

dockerfile in docker := {
  val stageDir = (stagingDirectory in Universal).value
  val appName = name.value
  new Dockerfile {
    from("dockerfile/java")
    expose(8080)
    add(stageDir, "/srv/" + appName)
    workDir("/srv/" + appName)
    run("chmod", "+x", "/srv/" + appName + "/bin/" + appName)
    entryPoint("/srv/" + appName + "/bin/" + appName)
  }
}

imageName in docker := ImageName(name.value)
