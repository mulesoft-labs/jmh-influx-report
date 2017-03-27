package org.mule.jmh.report.influx;


import java.util.Map;

public class JMHResult {

  private String name;
  private Map<String,Object> meassures;

  public JMHResult() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getMeassures() {
    return meassures;
  }

  public void setMeassures(Map<String, Object> meassures) {
    this.meassures = meassures;
  }

  @Override
  public String toString() {
    return "JMHResult{" +
            "name='" + name + '\'' +
            ", meassures=" + meassures +
            '}';
  }
}
