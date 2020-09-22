import play.core.PlayVersion.current
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

name := "cds-file-upload-frontend"
majorVersion := 0

PlayKeys.devSettings := Seq("play.server.http.port" -> "6793")

resolvers += Resolver.bintrayRepo("wolfendale", "maven")

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(scalaVersion := "2.12.12")
  .settings(publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    // concatenate js
    Concat.groups := Seq(
      "javascripts/cdsfileuploadfrontend-app.js" ->
        group(Seq("javascripts/cdsfileuploadfrontend.js")),
      "javascripts/analytics.min.js" ->
        group(Seq("javascripts/analytics.js"))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    pipelineStages in Assets := Seq(concat,uglify),
    // only compress files generated by concat
    includeFilter in uglify := GlobFilter("cdsfileuploadfrontend-*.js")
  )
  .settings(silencerSettings)

val httpComponentsVersion = "4.5.11"

val compileDependencies = Seq(
  "uk.gov.hmrc"                   %% "govuk-template"           % "5.54.0-play-26",
  "uk.gov.hmrc"                   %% "play-ui"                  % "8.9.0-play-26",
  "uk.gov.hmrc"                   %% "bootstrap-play-26"        % "1.7.0",
  "uk.gov.hmrc"                   %% "auth-client"              % "3.0.0-play-26",
  "com.github.pureconfig"         %% "pureconfig"               % "0.12.3",
  "org.apache.httpcomponents"     %  "httpclient"               % httpComponentsVersion,
  "org.apache.httpcomponents"     %  "httpmime"                 % httpComponentsVersion,
  "com.typesafe.play"             %% "play-json"                % "2.6.0",
  "uk.gov.hmrc"                   %% "play-whitelist-filter"    % "3.3.0-play-26",
  "uk.gov.hmrc"                   %% "crypto"                   % "5.6.0",
  "uk.gov.hmrc"                   %% "simple-reactivemongo"     % "7.26.0-play-26"
)

val testDependencies = Seq(
  "org.scalatest"             %% "scalatest"                % "3.0.5"     % "test",
  "org.jsoup"                 % "jsoup"                     % "1.11.3"    % "test",
  "com.typesafe.play"         %% "play-test"                % current     % "test",
  "org.pegdown"               % "pegdown"                   % "1.6.0"     % "test",
  "org.scalatestplus.play"    %% "scalatestplus-play"       % "3.1.2"     % "test",
  "org.mockito"               % "mockito-core"              % "2.27.0"    % "test",
  "org.scalacheck"            %% "scalacheck"               % "1.14.0"    % "test",
  "wolfendale"                %% "scalacheck-gen-regexp"    % "0.1.1"     % "test",
  "com.github.tomakehurst"    % "wiremock-standalone"       % "2.22.0"    % "test"
)

libraryDependencies ++= compileDependencies ++ testDependencies

lazy val silencerSettings: Seq[Setting[_]] = {
  val silencerVersion = "1.7.0"
  Seq(
    libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full)),
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )
}
