name := "zio-kafka"

version := "0.1"

scalaVersion := "2.13.8"
lazy val zioVersion           = "1.0.12"

lazy val kafkaVersion         = "3.1.0"
lazy val embeddedKafkaVersion = "3.1.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-kafka" % "0.15.0",
  "dev.zio" %% "zio-json"    % "0.1.5",
  "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
"org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
"com.lmax" % "disruptor" % "3.4.2",
  "dev.zio"                   %% "zio-streams"             % zioVersion,
  "dev.zio"                   %% "zio-test"                % zioVersion % Test,
  "dev.zio"                   %% "zio-test-sbt"            % zioVersion % Test,
  "org.apache.kafka"           % "kafka-clients"           % kafkaVersion,
  "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion % Test
)



