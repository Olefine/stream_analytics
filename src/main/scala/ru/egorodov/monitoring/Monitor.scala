package ru.egorodov.monitoring

import java.io.PrintWriter

import org.apache.spark.SparkConf
import java.net.Socket

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import ru.egorodov.ServerSocketReceiver
import ru.egorodov.util.CommunicationSettings

object Monitor {
  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("Monitor")
    val ssc = new StreamingContext(conf, Seconds(CommunicationSettings.monitoringDuration))

    val inputData = ssc.receiverStream(new ServerSocketReceiver(CommunicationSettings.monitoringPort))

    val result: DStream[(Int, Long)] = inputData.flatMap(_.split("\n")).map(Integer.parseInt(_)).countByValue()

    val windowStream: DStream[(Int, Long)] = result.window(Seconds(30))

    windowStream.foreachRDD { pairs =>
      val cachedPairs = pairs.collectAsMap()
      val emptyMapValues: Map[Int, Long] = Map(1 -> 0l, 0 -> 0l)


      val fullMap: collection.Map[Int, Long] = cachedPairs ++ emptyMapValues.map{ case (k,v) => k -> (v + cachedPairs.getOrElse(k, 0l)) }
      val totalCount = fullMap.foldLeft(0l)(_ + _._2)

      val report = new ReportConstructor(ssc.sparkContext, fullMap)
      report.send()
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
