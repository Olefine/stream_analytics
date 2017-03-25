import sbt.Keys._

name := "stream_analytics"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.1.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.1.0"
libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.startsWith("META-INF") => MergeStrategy.discard
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList("org", "apache", xs @ _*) => MergeStrategy.first
  case PathList("org", "jboss", xs @ _*) => MergeStrategy.first
  case "about.html"  => MergeStrategy.rename
  case "reference.conf" => MergeStrategy.concat
  case PathList("org", "datanucleus", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
  }
}
