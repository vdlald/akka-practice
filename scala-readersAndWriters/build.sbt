name := "scala-readersAndWriters"

version := "0.1"

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.8"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
