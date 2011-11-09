scalaVersion := "2.8.1"

resolvers in ThisBuild ++= Seq(
  "Github repository" at "http://mvn.github.com/repository"
)

seq(appengineSettings: _*)

libraryDependencies ++= {
    val liftVersion = "2.4-M4"
    Seq(
        "net.liftweb" %% "lift-common" % liftVersion,
	    "net.liftweb" %% "lift-util" % liftVersion,
	    "net.liftweb" %% "lift-webkit" % liftVersion,
	    "appengine-helpers" %% "cache" % "1.3-SNAPSHOT",
	    "appengine-helpers" %% "urlfetcher" % "1.5-SNAPSHOT",
	    "org.scalatest" % "scalatest" % "1.3" % "test",
	    "org.scala-tools.time" % "time_2.8.1" % "0.4",
	    "org.mortbay.jetty" % "jetty" % "6.1.22" % "jetty"
    )
}
