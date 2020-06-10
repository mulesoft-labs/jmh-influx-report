package org.mule.jmh.report.influx;


import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.mule.weave.v2.parser.ast.variables.NameIdentifier;
import org.mule.weave.v2.parser.exception.LocatableException;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScript;
import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.mule.weave.v2.runtime.ScriptingBindings;
import org.mule.weave.v2.sdk.ParsingContextFactory;
import org.mule.weave.v2.sdk.WeaveResource;
import org.mule.weave.v2.sdk.WeaveResourceFactory;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class InfluxReporter {

  /**
   * Inserts the repo inside the
   *
   * @param jsonReport           JMH json file path
   * @param dbName               The data base name where the measurements are being stored
   * @param connectionProperties The influx connection properties
   * @throws FileNotFoundException
   */
  public void createReport(String jsonReport, String dbName, InfluxConnectionProperties connectionProperties) throws FileNotFoundException {
    InfluxDB influxDB = InfluxDBFactory.connect(connectionProperties.getUrl(), connectionProperties.getUserName(), connectionProperties.getPassword());
    List<String> databases = influxDB.describeDatabases();

    //We create the data base if it doesn't exists
    if (!databases.contains(dbName)) {
      influxDB.createDatabase(dbName);
    }

    BatchPoints batchPoints = BatchPoints
            .database(dbName)
            .tag("async", "true")
            .retentionPolicy("autogen")
            .consistency(InfluxDB.ConsistencyLevel.ALL)
            .build();

    final WeaveResource weaveFile = WeaveResourceFactory.fromUrl(getClass().getClassLoader().getResource("report_processor.dwl"));
    final DataWeaveScriptingEngine dataWeaveScriptingEngine = new DataWeaveScriptingEngine();
    final DataWeaveScript compile = dataWeaveScriptingEngine.compile(weaveFile.content(), "report_processor");

    try {
      String gitHash = calculateGitHash();
      String commitMessage = calculateGitCommitMessage();
      final DataWeaveResult result = compile.write(new ScriptingBindings().addBinding("in0", new File(jsonReport), "application/json"));
      final List<JMHResult> results = (List<JMHResult>) result.getContent();
      for (JMHResult jmhResult : results) {
        Point.Builder builder = Point.measurement(jmhResult.getName()).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        Set<java.util.Map.Entry<String, Object>> fields = jmhResult.getMeassures().entrySet();
        for (java.util.Map.Entry<String, Object> field : fields) {
          if (field.getValue() instanceof Double) {
            builder.addField(field.getKey(), (Double) field.getValue());
          } else if (field.getValue() instanceof Integer) {
            //We always use double
            builder.addField(field.getKey(), ((Integer) field.getValue()).doubleValue());
          } else if (field.getValue() instanceof Boolean) {
            builder.addField(field.getKey(), (Boolean) field.getValue());
          } else {
            builder.addField(field.getKey(), field.getValue().toString());
          }
        }
        builder.addField("git", gitHash);
        builder.addField("commit.msg", commitMessage);
        batchPoints.point(builder.build());
        //To avoid two influx entries with same time
        Thread.sleep(2);
      }

      influxDB.write(batchPoints);
    } catch (Exception e) {
      if (e instanceof LocatableException) {
        System.err.println(((LocatableException) e).formatErrorLine());
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Read until the end of the stream.
   */
  private String readLines(BufferedReader reader) throws IOException, InterruptedException {
    // buffer for storing file contents in memory
    StringBuilder stringBuffer = new StringBuilder("");
    String line;
    while ((line = reader.readLine()) != null) {
      // keep appending last line read to buffer
      stringBuffer.append(line);
    }
    return stringBuffer.toString();
  }

  private String exec(String cmd) throws IOException, InterruptedException {
    Process exec = Runtime.getRuntime().exec(cmd);
    exec.waitFor();
    InputStream output = exec.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(output));
    return readLines(reader);
  }

  private String calculateGitHash() throws IOException, InterruptedException {
    return exec("git rev-parse HEAD");
  }

  private String calculateGitCommitMessage() throws IOException, InterruptedException {
    return exec("echo \"`git log -1`\"");
  }

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length == 5) {
      final String reportPath = args[0];
      final String dataBaseName = args[1];
      final String url = args[2];
      final String username = args[3];
      final String password = args[4];
      new InfluxReporter().createReport(reportPath, dataBaseName, new InfluxConnectionProperties(url, username, password));
      System.out.print("Result inserted successfully.");
    } else {
      System.err.println("Expecting 5 parameters <reportPath> <dataBaseName> <influxUrl> <influxUserName> <influxPassword>");
    }

  }
}


