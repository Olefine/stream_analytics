package ru.egorodov.master

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import ru.egorodov.ServerSocketReceiver
import ru.egorodov.master.transformations.TsvOperations
import ru.egorodov.util.CommunicationSettings

object TsvProcessor extends AbilityToMakeReports {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(CommunicationSettings.masterApplicationName)
    val ssc = new StreamingContext(conf, Seconds(CommunicationSettings.masterDuration))

    val inputData = ssc.receiverStream(new ServerSocketReceiver(CommunicationSettings.masterPort))

    val streamWithAppliedTransformation: DStream[Int] = TsvOperations.applySumTrasformation(inputData)

    if (CliOptionParser.isMonitoringEnabled(args))
      report(streamWithAppliedTransformation)

    streamWithAppliedTransformation.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
