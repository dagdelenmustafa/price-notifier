val Http4sVersion          = "0.23.16"
val CirceVersion           = "0.14.3"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"
val Mongo4CatsVersion      = "0.6.5"

lazy val root = (project in file("."))
  .settings(
    organization := "com.dagdelenmustafa",
    startYear    := Some(2022),
    name         := "product-notifier",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.10",
    developers := List(
      Developer("dagdelenmustafa",
                "Mustafa Dağdelen",
                "mustafadagdelen@protonmail.com",
                url("https://github.com/dagdelenmustafa")
      )
    ),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),
    libraryDependencies ++= Seq(
      "org.http4s"         %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"         %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"         %% "http4s-circe"        % Http4sVersion,
      "org.http4s"         %% "http4s-dsl"          % Http4sVersion,
      "io.circe"           %% "circe-generic"       % CirceVersion,
      "io.github.kirill5k" %% "mongo4cats-core"     % Mongo4CatsVersion,
      "io.github.kirill5k" %% "mongo4cats-circe"    % Mongo4CatsVersion,
      "com.google.guava"    % "guava"               % "31.1-jre",
      "net.ruippeixotog"   %% "scala-scraper"       % "3.0.0",
      "io.github.kirill5k" %% "mongo4cats-embedded" % Mongo4CatsVersion      % Test,
      "org.scalameta"      %% "munit"               % MunitVersion           % Test,
      "org.typelevel"      %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"      % "logback-classic"     % LogbackVersion         % Runtime,
      "org.scalameta"      %% "svm-subs"            % "20.2.0"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
