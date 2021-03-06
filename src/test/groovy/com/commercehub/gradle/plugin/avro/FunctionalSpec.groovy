package com.commercehub.gradle.plugin.avro

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class FunctionalSpec extends Specification {
    protected static final String AVRO_VERSION = "1.7.7" // TODO: externalize

    @Rule
    TemporaryFolder testProjectDir

    File buildFile
    File avroDir
    File avroSubDir

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        avroDir = testProjectDir.newFolder("src", "main", "avro")
        avroSubDir = testProjectDir.newFolder("src", "main", "avro", "foo")

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

         // escape backslashes in Windows paths and assemble
        def pluginClasspath = pluginClasspathResource.readLines()*.replace('\\', '\\\\').collect { "'$it'" }.join(", ")

        // Add the logic under test to the test build
        buildFile << """
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
            apply plugin: "com.commercehub.gradle.plugin.avro"
            repositories { jcenter() }
            dependencies { compile "org.apache.avro:avro:${AVRO_VERSION}" }
        """
    }

    protected void copyResource(String name, File targetFolder) {
        def file = new File(targetFolder, name)
        file.parentFile.mkdirs()
        file << getClass().getResourceAsStream(name)
    }

    protected File projectFile(String path) {
        return new File(testProjectDir.root, path)
    }

    protected BuildResult run(String... args = ["build"]) {
        return GradleRunner.create().withProjectDir(testProjectDir.root).withArguments(args).build()
    }

    protected BuildResult runAndFail(String... args = ["build"]) {
        return GradleRunner.create().withProjectDir(testProjectDir.root).withArguments(args).buildAndFail()
    }
}
