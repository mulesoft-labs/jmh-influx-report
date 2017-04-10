package org.mule.jmh.report.influx;


import com.mulesoft.weave.module.json.reader.JsonReader;
import com.mulesoft.weave.module.reader.Reader;
import com.mulesoft.weave.module.reader.SourceProvider$;
import com.mulesoft.weave.module.reader.SourceReader$;
import com.mulesoft.weave.parser.ast.variables.NameIdentifier;
import com.mulesoft.weave.parser.exception.LocatableException;
import com.mulesoft.weave.parser.phase.PhaseResult;
import com.mulesoft.weave.runtime.CompilationResult;
import com.mulesoft.weave.runtime.ExecutableWeave;
import com.mulesoft.weave.runtime.WeaveCompiler;
import com.mulesoft.weave.sdk.ParsingContextFactory;
import com.mulesoft.weave.sdk.WeaveResource;
import com.mulesoft.weave.sdk.WeaveResourceFactory;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import scala.Tuple2;
import scala.collection.immutable.Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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

    final WeaveResource weaveFile = WeaveResourceFactory.fromUrl(getClass().getClassLoader().getResource("report_processor.wev"));
    final PhaseResult<CompilationResult> compile = WeaveCompiler.compile(weaveFile, NameIdentifier.anonymous(), ParsingContextFactory.createMappingParsingContext());
    final ExecutableWeave executable = compile.getResult().executable();
    final Reader jsonReader = new JsonReader(SourceReader$.MODULE$.apply(SourceProvider$.MODULE$.apply(new File(jsonReport), Charset.forName("UTF-8"))));
    final Map<String, Reader> payload = executable.write$default$2().$plus(Tuple2.apply("in0", jsonReader));
    try {
      String gitHash = calculateGitHash();
      final Tuple2<Object, Charset> result = executable.write(executable.write$default$1(), payload, executable.write$default$3(), executable.write$default$4());
      final List<JMHResult> results = (List<JMHResult>) result._1();
      for (JMHResult jmhResult : results) {
        Point.Builder builder = Point.measurement(jmhResult.getName())
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
        batchPoints.point(builder.build());
      }

      influxDB.write(batchPoints);
    } catch (Exception e) {
      if (e instanceof LocatableException) {
        System.err.println(((LocatableException) e).formatErrorLine());
      }
      throw new RuntimeException(e);
    }
  }

  private String calculateGitHash() throws IOException, InterruptedException {
    Process exec = Runtime.getRuntime().exec("git rev-parse HEAD");
    exec.waitFor();
    InputStream output = exec.getInputStream();
    return new BufferedReader(new InputStreamReader(output)).readLine();
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


