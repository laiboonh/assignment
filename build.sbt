name := "Assignment"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.9.7",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


scalacOptions in Test ++= Seq("-Yrangepos")
