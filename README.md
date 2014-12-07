# Gradle-dependency-analysis-plugin

A gradle dependency analysis plugin inspired by the maven dependency plugin and https://gist.github.com/kellyrob99/4334483.

## Usage:

Download the project and install to maven local repo with: gradle install

Include the following in the top of your project:

```groovy
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath group: 'org.lund.gradle', name: 'DependencyAnalysisPlugin', version: '1.0'
    }
}

apply plugin: 'dependencyAnalysis'
```

## Tasks
### `analyze`
The analyze task is the only task currently provided by this plugin.
It will scan your project and find used and unused artifacts split into declared and transitive dependencies.
The task throws an execption if you have unused declared artifacts (so your CI job will fail).
