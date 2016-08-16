name := """test-reports-agregator"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"
updateOptions := updateOptions.value.withCachedResolution(true)
libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.23"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.53.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test"
libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % "2.5.4"

coverageExcludedPackages := """controllers\..*Reverse.*;"""//router.Routes.*;"""
libraryDependencies ++= Seq()
