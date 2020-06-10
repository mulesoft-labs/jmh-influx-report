import org.mule.jmh.report.influx.InfluxConnectionProperties;
import org.mule.jmh.report.influx.InfluxReporter;

import java.io.FileNotFoundException;


public class SimpleTest {

  public static final String INFLUX_URL = "http://localhost:8086";
  public static final String REPORT_PATH = "/Users/mdeachaval/labs/mulesoft-labs/jmh-influx-report/jmh-to-influx/src/main/resources/results.json";
  public static final String DB_NAME = "dw-benchmark-test";

  public static void main(String[] args) throws FileNotFoundException {
    InfluxReporter influxReporter = new InfluxReporter();
    InfluxConnectionProperties connectionProperties = new InfluxConnectionProperties(INFLUX_URL, "dataweave", "dataweave");
    influxReporter.createReport(REPORT_PATH, DB_NAME, connectionProperties);
  }
}
