name := "MiLiVi"

version := "1.0"

scalaVersion := "2.11.8"

// Add dependency on ScalaFX library
libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.92-R10",
  "net.sourceforge.plantuml" % "plantuml" % "8040",
  "org.asciidoctor" % "asciidoctorj" % "1.5.4",
  "org.pegdown" % "pegdown" % "1.6.0"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true