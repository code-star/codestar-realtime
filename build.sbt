name := "CodestarRealtime"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "com.google.transit" % "gtfs-realtime-bindings" % "0.0.4"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.1.0"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.21" // "1.6.4"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.18"

cancelable in Global := true

//val json4sVersion = "3.3.0"
//libraryDependencies ++= Seq(
//  "org.json4s" % "json4s-core" % json4sVersion,
//  "org.json4s" % "json4s-native" % json4sVersion,
//  "org.json4s" % "json4s-jackson" % json4sVersion
//)

//resolvers += "Sonatype (releases)" at "https://oss.sonatype.org/content/repositories/releases/"
//
//libraryDependencies += "org.zeromq" % "zeromq-scala-binding_2.11.0-M3" % "0.0.7"

//resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
//libraryDependencies += "com.typesafe.play" % "play-json" % "2.6.20"


// https://mvnrepository.com/artifact/com.googlecode.protobuf-java-format/protobuf-java-format
libraryDependencies += "com.googlecode.protobuf-java-format" % "protobuf-java-format" % "1.2"
libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.6.1"
libraryDependencies += "com.google.protobuf" % "protobuf-java-util" % "3.6.1" // for JsonFormat

