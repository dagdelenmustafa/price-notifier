ThisBuild / organization := "com.dagdelenmustafa"
ThisBuild / scalaVersion := "2.13.10"

lazy val dependencies = new {
  val Http4sVersion          = "0.23.16"
  val CirceVersion           = "0.14.3"
  val MunitVersion           = "0.7.29"
  val LogbackVersion         = "1.2.11"
  val MunitCatsEffectVersion = "1.0.7"
  val Mongo4CatsVersion      = "0.6.5"
  val GoogleGuavaVersion     = "31.1-jre"
  val SvmMetaVersion         = "20.2.0"

  val http4sEmberServer  = "org.http4s"         %% "http4s-ember-server" % Http4sVersion
  val http4sEmberClient  = "org.http4s"         %% "http4s-ember-client" % Http4sVersion
  val http4sCirce        = "org.http4s"         %% "http4s-circe"        % Http4sVersion
  val http4sDsl          = "org.http4s"         %% "http4s-dsl"          % Http4sVersion
  val circeGeneric       = "io.circe"           %% "circe-generic"       % CirceVersion
  val mongo4catsCore     = "io.github.kirill5k" %% "mongo4cats-core"     % Mongo4CatsVersion
  val mongo4catsCirce    = "io.github.kirill5k" %% "mongo4cats-circe"    % Mongo4CatsVersion
  val guava              = "com.google.guava"    % "guava"               % GoogleGuavaVersion
  val mongo4catsEmbedded = "io.github.kirill5k" %% "mongo4cats-embedded" % Mongo4CatsVersion      % Test
  val munit              = "org.scalameta"      %% "munit"               % MunitVersion           % Test
  val munitCatsEffect    = "org.typelevel"      %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test
  val logback            = "ch.qos.logback"      % "logback-classic"     % LogbackVersion         % Runtime
  val svmMeta            = "org.scalameta"      %% "svm-subs"            % SvmMetaVersion
}

lazy val commonDependencies = Seq(
  dependencies.circeGeneric,
  dependencies.munit,
  dependencies.munitCatsEffect,
  dependencies.logback,
  dependencies.svmMeta
)

lazy val notifierRestServiceDependencies = commonDependencies ++ Seq(
  dependencies.http4sEmberServer,
  dependencies.http4sEmberClient,
  dependencies.http4sCirce,
  dependencies.http4sDsl,
  dependencies.mongo4catsCore,
  dependencies.mongo4catsCirce,
  dependencies.guava,
  dependencies.mongo4catsEmbedded
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
  .settings(
    description := "Common components for the notifier app.",
    name        := "common",
    version     := "0.0.1-SNAPSHOT",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val notifierRestService = project
  .in(file("notifier-rest-service"))
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
              "Mustafa Dağdelen",
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
