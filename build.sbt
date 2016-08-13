name := """test-reports-agregator"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"
libraryDependencies += "net.sourceforge.htmlunit" % "htmlunit" % "2.23"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "2.53.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test"
libraryDependencies ++= Seq()
