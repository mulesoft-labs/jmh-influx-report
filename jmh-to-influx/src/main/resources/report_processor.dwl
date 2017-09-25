%dw 2.0
input in0 application/json
output application/java
---
in0 map (result) -> {
  name: (result.benchmark splitBy ".")[-2],
  meassures: {
        testName: (result.benchmark splitBy ".")[-1],
        (result.mode) : result.primaryMetric.score,
        unit: result.primaryMetric.scoreUnit,
        error : result.primaryMetric.scoreError,
        iterations: result.measurementIterations
  } ++ (result.params default {}) ++
  (result.secondaryMetrics mapObject {
    ($$) : $.score ++ " " ++ $.scoreUnit
  })
} as Object {class: "org.mule.jmh.report.influx.JMHResult"}