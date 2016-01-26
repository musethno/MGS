import sbt._
import Keys._
import play.Project._



object ApplicationBuild extends Build {

  val appName         = "museum"
  val appVersion      = "1.0-SNAPSHOT"
  val mysql       = "mysql" % "mysql-connector-java" % "5.1.20"
  val emails          = "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
  
  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    mysql,
    emails
  )

  
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here

  )
  
  



}


