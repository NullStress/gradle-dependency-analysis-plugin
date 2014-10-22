gradle-dependency-analysis-plugin
=================================

A gradle dependency analysis plugin where much of the code is a port of the maven dependency plugin to gradle. Currently a work in progress project.
The roadmap ahead is to improve the test coverage and give better output of the results.

Usage:

Download the project and install to maven local with: gradle install

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
