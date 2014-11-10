gradle-dependency-analysis-plugin
=================================

A gradle dependency analysis plugin inspired by the maven dependency plugin and https://gist.github.com/kellyrob99/4334483.

Usage:

Download the project and install to maven local repo with: gradle install

Include the following in the top of your project:

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



To run use: gradle analyze
