lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.11"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "nl.codestar",
      scalaVersion    := "2.12.4"
    )),
    name := "CodestarRealtime",
    version := "0.1",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka"  %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka"  %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka"  %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"      %% "scalatest"            % "3.0.1"         % Test,

      "org.slf4j"           % "slf4j-simple"          % "1.7.21", // "1.6.4"
      "org.apache.kafka"    % "kafka-clients"         % "0.10.1.0",
//      "com.typesafe.akka"  %% "akka-stream"          % "2.5.18",
      "com.google.transit"  % "gtfs-realtime-bindings" % "0.0.4",

      // https://mvnrepository.com/artifact/com.googlecode.protobuf-java-format/protobuf-java-format
//        "com.googlecode.protobuf-java-format" % "protobuf-java-format" % "1.2",
//      "com.google.protobuf" % "protobuf-java"       % "3.6.1",
//      "com.google.protobuf" % "protobuf-java-util"  % "3.6.1" // for JsonFormat

      "org.zeromq"          % "jeromq"                % "0.4.0"
    )
  )

cancelable in Global := true
