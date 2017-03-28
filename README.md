[![Build Status](https://travis-ci.org/Olefine/stream_analytics.svg?branch=master)](https://travis-ci.org/Olefine/stream_analytics)

# Analyze Apache Spark streams Proof Of Concept

NOTE: You need run spark in cluster mode with YARN/MESOS as cluster manager

## How it works

### Main usecase
Let's say you have running spark streaming application(I'm calling it master application or master), we can simplify things and say that in input we getting TSV int values like

1\t2\n
2\t34\n
...

Main class is https://github.com/Olefine/stream_analytics/blob/master/src/main/scala/ru/egorodov/master/TsvProcessor.scala

As an output application returns Listr of 0 and 1. 0 returning if sum of two numbers we got is odd, and 1 if sum of two number is even

One day you decided to start monitor your master application.
You need to submit https://github.com/Olefine/stream_analytics/blob/master/src/main/scala/ru/egorodov/monitoring/Monitor.scala job to cluster and the restart master application with option ```--monitoring```. 

Monitoring application is also spark streaming application. As and input it's getting 0's and 1's from master application and saves ratio ```number_of_zeros / total_number_of_number``` into the file.

Also there is a python spark application https://github.com/Olefine/stream_analytics/blob/master/src/main/python/Monitoring.py it is a replacement of scala's monitoring application, when developing this proof of concept I tried to create some kind of DSL using python to allow QA/users who played with spark not well create monitoring jobs, but I decided to postpone this feature, becase I can create spark job wrapper(where I can hide initialization of sparkContext/streamingContext), but in ideal world I need to hide all spark API's including ```foreachRDD, checkpoint``` etc, we can avoid usage of spark API two ways - 1) use structured streaming concept, 2) try to write more wrappers :), but these ways require time to implement.

So, at this point we have two spark streaming application connected together using ```java.io.Socket```
Now we need mechanism to stop master application if we have bad ratio between zeros and ones..

For this purpose I created additional application https://github.com/Olefine/stream_analytics_orchestration
It is an akka-http orchestration application that exposes internal spark API to submit/kill jobs by driverID.

Full flow will look like this

Submit monitor
```
curl -X POST 0.0.0.0:3000/submissions --header "Content-Type:application/json;charset=UTF-8" --data '{
  "sparkMasterUi" : "http://0.0.0.0:6066",
  "appResource" : "file:/Users/egorgorodov/dev/scala/stream_analytics/target/scala-2.11/stream_analytics-assembly-1.0.jar",
  "clientSparkVersion" : "2.1.0",
  "mainClass" : "ru.egorodov.monitoring.Monitor",
  "sparkJars" : "file:/Users/egorgorodov/dev/scala/stream_analytics/target/scala-2.11/stream_analytics-assembly-1.0.jar",
  "sparkAppName" : "Monitor",
  "sparkMaster" : "spark://Egors-Mac-mini.local:7077"
}'
```

Submit master application
```
curl -X POST 0.0.0.0:3000/submissions --header "Content-Type:application/json;charset=UTF-8" --data '{
  "sparkMasterUi" : "http://0.0.0.0:6066",
  "appResource" : "file:/Users/egorgorodov/dev/scala/stream_analytics/target/scala-2.11/stream_analytics-assembly-1.0.jar",
  "clientSparkVersion" : "2.1.0",
  "mainClass" : "ru.egorodov.master.TsvProcessor",
  "sparkJars" : "file:/Users/egorgorodov/dev/scala/stream_analytics/target/scala-2.11/stream_analytics-assembly-1.0.jar",
  "sparkAppName" : "TsvProcessor",
  "sparkMaster" : "spark://Egors-Mac-mini.local:7077"
}'
```

Send data to master application using netcat

```
nc 127.0.0.1 9999
1 2
10  20
...
```

And then, if ratio will be more then max ratio from config, application you passed from monitoring spark application will be killed

So, I tested it locally 1 application at 1 time, because I don't have enough resources to setup mesos/yarn cluster and it seems to be worked
