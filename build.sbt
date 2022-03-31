name := "zio-kafka"

version := "0.1"

scalaVersion := "2.13.8"


libraryDependencies ++= Seq(
  "dev.zio" %% "zio-kafka"   % "0.15.0",
)

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
)
