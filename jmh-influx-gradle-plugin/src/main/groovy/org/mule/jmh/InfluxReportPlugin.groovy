package org.mule.jmh

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.mule.jmh.report.influx.InfluxConnectionProperties
import org.mule.jmh.report.influx.InfluxReporter

class InfluxReportPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('jmhInflux', InfluxReportExtension)
        def task = project.task('generateJmhInfluxReport') << {
            InfluxReportExtension configuration = project.jmhInflux
            configuration.dbName = configuration.dbName ?: project.name + "_db"
            project.logger.info 'Using this configuration:\n{}', configuration
            new InfluxReporter().createReport(configuration.reportPath, configuration.dbName, new InfluxConnectionProperties(configuration.dbUrl, configuration.dbUserName, configuration.dbUserPassowrd))
        }
        task.group = "jmh"
        task.description = "Parse the json result and inserts it inside an influx db."

    }
}
