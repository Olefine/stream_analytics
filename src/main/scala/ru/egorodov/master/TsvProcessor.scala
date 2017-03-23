package ru.egorodov.master

import java.io.PrintWriter
import java.net.Socket

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import ru.egorodov.ServerSocketReceiver
import ru.egorodov.master.transformations.SumTransformation

object TsvProcessor {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("TsvProcessor")
    val ssc = new StreamingContext(conf, Seconds(5))

    val inputData = ssc.receiverStream(new ServerSocketReceiver(9999))

    val tsvRows = inputData.flatMap(_.split("\n"))
    val numberPairs = tsvRows.map(_.split("\t")).map(pair =>
      new DataSchema.LatLong(Integer.parseInt(pair(0)), Integer.parseInt(pair(1)))
    )

    val streamWithAppliedTransformation = numberPairs.map(SumTransformation(_))

    //for now monitoring server should be running before master
    if (CliOptionParser.isMonitoringEnabled(args))
      streamWithAppliedTransformation.foreachRDD {rdd =>
        rdd.foreachPartition { partition =>
          val socket = new Socket("localhost", 9902)
          val out = new PrintWriter(socket.getOutputStream, true)

          partition.foreach(record => out.write(record.toString + "\n"))

          out.close()
        }
      }

    streamWithAppliedTransformation.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
