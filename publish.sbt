ThisBuild / publishTo := Some(Resolver.file("file", new File("mavenRepository")))

ThisBuild / publishMavenStyle := true

ThisBuild / assemblyMergeStrategy := {
  case x if x.endsWith("module-info.class") => {
    MergeStrategy.discard
  }
  case PathList("META-INF", "substrate", "config", _*) => MergeStrategy.first
  case x => {
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
  }
}
