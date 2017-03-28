package org.mule.jmh.report.influx;


public class InfluxConnectionProperties {

  private String url;
  private String userName;
  private String password;

  public InfluxConnectionProperties() {
    this("http://localhost:8086", null, null);
  }

  public InfluxConnectionProperties(String url, String userName, String password) {
    this.url = url;
    this.userName = userName;
    this.password = password;
  }

  public String getUrl() {
    return url;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
}
