package ru.egorodov.master

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import ru.egorodov.ServerSocketReceiver
import ru.egorodov.master.transformations.SumTransformation
import ru.egorodov.util.CommunicationSettings

object TsvProcessor extends AbilityToMakeReports {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("TsvProcessor")
    val ssc = new StreamingContext(conf, Seconds(5))

    val inputData = ssc.receiverStream(new ServerSocketReceiver(CommunicationSettings.masterPort))

    val tsvRows = inputData.flatMap(_.split("\n"))
    val numberPairs = tsvRows.map(_.split("\t")).map(pair =>
      new DataSchema.LatLong(Integer.parseInt(pair(0)), Integer.parseInt(pair(1)))
    )

    val streamWithAppliedTransformation: DStream[Int] = numberPairs.map(SumTransformation(_))

    if (CliOptionParser.isMonitoringEnabled(args))
      report(streamWithAppliedTransformation)

    streamWithAppliedTransformation.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
