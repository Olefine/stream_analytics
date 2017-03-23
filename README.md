# Analyze Apache Spark streams

It requires to run 2 spark-submit

1) ```bin/spark-submit --class ru.egorodov.monitoring.Monitor /Users/egorgorodov/dev/scala/stream_analytics/target/scala-2.11/stream_analytics_2.11-1.0.jar```
2) ```bin/spark-submit --class ru.egorodov.master.TsvProcessor /Users/egorgorodov/dev/scala/stream_analytics/target/scala-2.11/stream_analytics_2.11-1.0.jar```

Firstly we need to run monitoring job(1)
And then (2)

Master Job is very simple
Input - table separated 2 ints
Output - 0 or 1 for each pair of ints from input, depending of sum of this number we send 0 if sum odd, 1 if sum even.
