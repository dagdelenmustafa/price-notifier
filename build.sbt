ThisBuild / organization := "com.dagdelenmustafa"
ThisBuild / scalaVersion := "2.13.10"

lazy val dependencies = new {
  val Http4sVersion          = "0.23.16"
  val CirceVersion           = "0.14.3"
  val CirceRefinedVersion    = "0.14.3"
  val MunitVersion           = "0.7.29"
  val LogbackVersion         = "1.2.11"
  val MunitCatsEffectVersion = "1.0.7"
  val Mongo4CatsVersion      = "0.6.5"
  val GoogleGuavaVersion     = "31.1-jre"
  val SvmMetaVersion         = "20.2.0"
  val RefinedVersions        = "0.10.1"
  val Fs2Version             = "3.4.0"
  val Fs2RabbitVersion       = "5.0.0"
  val FUUIDVersion           = "0.8.0-M2"
  val SendgridVersion        = "4.9.3"

  val http4sEmberServer = "org.http4s"         %% "http4s-ember-server" % Http4sVersion
  val http4sEmberClient = "org.http4s"         %% "http4s-ember-client" % Http4sVersion
  val http4sCirce       = "org.http4s"         %% "http4s-circe"        % Http4sVersion
  val http4sDsl         = "org.http4s"         %% "http4s-dsl"          % Http4sVersion
  val circeGeneric      = "io.circe"           %% "circe-generic"       % CirceVersion
  val circeRefined      = "io.circe"           %% "circe-refined"       % CirceRefinedVersion
  val mongo4catsCore    = "io.github.kirill5k" %% "mongo4cats-core"     % Mongo4CatsVersion
  val mongo4catsCirce   = "io.github.kirill5k" %% "mongo4cats-circe"    % Mongo4CatsVersion
  val guava             = "com.google.guava"    % "guava"               % GoogleGuavaVersion
  val refined           = "eu.timepit"         %% "refined"             % RefinedVersions
  val fs2               = "co.fs2"             %% "fs2-core"            % Fs2Version
  val fs2Rabbit         = "dev.profunktor"     %% "fs2-rabbit"          % Fs2RabbitVersion
  val fs2RabbitCirce    = "dev.profunktor"     %% "fs2-rabbit-circe"    % Fs2RabbitVersion
  val FUUID             = "io.chrisdavenport"  %% "fuuid"               % FUUIDVersion
  val FUUIDCirce        = "io.chrisdavenport"  %% "fuuid-circe"         % FUUIDVersion
  val FUUIDHttp4s       = "io.chrisdavenport"  %% "fuuid-http4s"        % FUUIDVersion
  val sendgrid          = "com.sendgrid"        % "sendgrid-java"       % SendgridVersion

  val mongo4catsEmbedded = "io.github.kirill5k" %% "mongo4cats-embedded" % Mongo4CatsVersion      % Test
  val munit              = "org.scalameta"      %% "munit"               % MunitVersion           % Test
  val munitCatsEffect    = "org.typelevel"      %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test
  val logback            = "ch.qos.logback"      % "logback-classic"     % LogbackVersion         % Runtime
  val svmMeta            = "org.scalameta"      %% "svm-subs"            % SvmMetaVersion
}

lazy val commonDependencies = Seq(
  dependencies.http4sEmberClient,
  dependencies.http4sCirce,
  dependencies.circeGeneric,
  dependencies.munit,
  dependencies.munitCatsEffect,
  dependencies.mongo4catsCore,
  dependencies.mongo4catsCirce,
  dependencies.circeRefined,
  dependencies.refined,
  dependencies.logback,
  dependencies.mongo4catsEmbedded,
  dependencies.svmMeta,
  dependencies.FUUID,
  dependencies.FUUIDCirce,
  dependencies.fs2,
  dependencies.fs2Rabbit,
  dependencies.fs2RabbitCirce
)

lazy val notifierRestServiceDependencies = commonDependencies ++ Seq(
  dependencies.http4sEmberServer,
  dependencies.http4sDsl,
  dependencies.guava,
  dependencies.FUUIDHttp4s
)

lazy val notifierNotificationSenderDependencies = commonDependencies ++ Seq(
  dependencies.sendgrid
)

lazy val root = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    common,
    notifierRestService
  )

lazy val common = project
  .in(file("modules/common"))
  .settings(
    description := "Common components for the notifier app.",
    name        := "common",
    version     := "0.0.1-SNAPSHOT",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val notifierRestService = project
  .in(file("modules/notifier-rest-service"))
  .settings(
    description := "Rest service of the notifier app",
    name        := "notifier-rest-service",
    version     := "0.0.1-SNAPSHOT",
    settings,
    libraryDependencies ++= notifierRestServiceDependencies,
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .disablePlugins(AssemblyPlugin)
  .dependsOn(common)

lazy val notifierPriceChecker = project
  .in(file("modules/notifier-price-checker"))
  .settings(
    description := "Price checker",
    name        := "notifier-price-checker",
    version     := "0.0.1-SNAPSHOT",
    settings,
    libraryDependencies ++= commonDependencies,
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .disablePlugins(AssemblyPlugin)
  .dependsOn(common)

lazy val notifierNotificationSender = project
  .in(file("modules/notifier-notification-sender"))
  .settings(
    description := "A service that sends notifications to end users.",
    name        := "notifier-notification-sender",
    version     := "0.0.1-SNAPSHOT",
    settings,
    libraryDependencies ++= notifierNotificationSenderDependencies,
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .disablePlugins(AssemblyPlugin)
  .dependsOn(common)

lazy val settings =
  commonSettings

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions := compilerOptions,
  startYear     := Some(2022),
  developers := List(
    Developer("dagdelenmustafa",
              "Mustafa Da??delen",
              "mustafadagdelen@protonmail.com",
              url("https://github.com/dagdelenmustafa")
    )
  ),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
)

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := name.value + ".jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)
