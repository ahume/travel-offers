scalaVersion := "2.8.1"

resolvers in ThisBuild ++= Seq(
  "Github repository" at "http://mvn.github.com/repository"
)

seq(webSettings :_*)

libraryDependencies ++= {
    val liftVersion = "2.4-M4"
    Seq(
        "net.liftweb" %% "lift-common" % liftVersion,
	    "net.liftweb" %% "lift-util" % liftVersion,
	    "net.liftweb" %% "lift-webkit" % liftVersion,
	    "appengine-helpers" %% "cache" % "1.3-SNAPSHOT",
	    "appengine-helpers" %% "urlfetcher" % "1.5-SNAPSHOT",
	    "com.google.appengine" % "appengine-api-1.0-sdk" % "1.5.1" % "provided",
	    "org.scalatest" % "scalatest" % "1.3" % "test",
	    "org.scala-tools.time" % "time_2.8.1" % "0.4",
	    "org.eclipse.jetty" % "jetty-webapp" % "7.3.1.v20110307" % "jetty"
    )
}
