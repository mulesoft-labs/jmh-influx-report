package org.mule.jmh;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mule.jmh.report.influx.InfluxConnectionProperties;
import org.mule.jmh.report.influx.InfluxReporter;

import java.io.FileNotFoundException;


@Mojo(name = "generate-report", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(goal = "generate-report")
public class InfluxReportGeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.build.outputDirectory}/reports/jmh/results.json")
  private String reportPath;

  @Parameter(defaultValue = "${project.artifactId}_db")
  private String dbName;

  @Parameter(defaultValue = "http://localhost:8086")
  private String dbUrl;

  @Parameter
  private String dbUserName;

  @Parameter
  private String dbUserPassowrd;

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      new InfluxReporter().createReport(reportPath, dbName, new InfluxConnectionProperties(dbUrl, dbUserName, dbUserPassowrd));
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("Report : " + reportPath + " was not found.", e);
    }
  }
}
