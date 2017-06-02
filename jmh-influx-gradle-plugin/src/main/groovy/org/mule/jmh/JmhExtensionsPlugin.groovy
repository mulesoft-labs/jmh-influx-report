package org.mule.jmh

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.mule.jmh.report.influx.InfluxConnectionProperties
import org.mule.jmh.report.influx.InfluxReporter

class JmhExtensionsPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('influxReport', InfluxReportExtension)
        def task = project.task('generateJmhInfluxReport') << {
            InfluxReportExtension configuration = project.influxReport
            configuration.dbName = configuration.dbName ?: project.name + "_db"
            project.logger.info 'Using this configuration:\n{}', configuration
            new InfluxReporter().createReport(project.buildDir.absolutePath + "/" + configuration.reportPath, configuration.dbName, new InfluxConnectionProperties(configuration.dbUrl, configuration.dbUserName, configuration.dbUserPassword))
        }
        task.group = "jmh"
        task.description = "Parses the json result and inserts it inside an influx db."

    }
}
