package ru.egorodov.monitoring

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import ru.egorodov.ServerSocketReceiver

object Monitor extends App {
  val conf = new SparkConf().setAppName("Monitor")
  val ssc = new StreamingContext(conf, Seconds(5))

  val inputData = ssc.receiverStream(new ServerSocketReceiver(9902))

  val result: DStream[(Int, Long)] = inputData.flatMap(_.split("\n")).map(Integer.parseInt(_)).countByValue()

  result.print()

  ssc.start()
  ssc.awaitTermination()
}
