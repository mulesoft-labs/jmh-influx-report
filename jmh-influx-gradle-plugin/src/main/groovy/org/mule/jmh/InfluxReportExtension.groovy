package org.mule.jmh


class InfluxReportExtension {

    String reportPath = "build/reports/jmh/results.json";
    String dbName;
    String dbUrl = "http://localhost:8086";
    String dbUserName;
    String dbUserPassowrd;


    @Override
    public String toString() {
        return "InfluxReportExtension{" +
                "reportPath='" + reportPath + '\'' +
                ", dbName='" + dbName + '\'' +
                ", host='" + host + '\'' +
                ", dbUserName='" + dbUserName + '\'' +
                ", dbUserPassowrd='" + dbUserPassowrd + '\'' +
                '}';
    }
}

