package org.mule.jmh


class InfluxReportExtension {

    String reportPath = "reports/jmh/results.json";
    String dbName;
    String dbUrl = "http://localhost:8086";
    String dbUserName;
    String dbUserPassword;


    @Override
    public String toString() {
        return "InfluxReportExtension{" +
                "reportPath='" + reportPath + '\'' +
                ", dbName='" + dbName + '\'' +
                ", host='" + host + '\'' +
                ", dbUserName='" + dbUserName + '\'' +
                ", dbUserPassword='" + dbUserPassword + '\'' +
                '}';
    }
}

