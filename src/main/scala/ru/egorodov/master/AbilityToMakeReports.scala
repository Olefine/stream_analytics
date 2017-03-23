package ru.egorodov.master

import java.io.PrintWriter
import java.net.Socket

import org.apache.spark.streaming.dstream.DStream

trait AbilityToMakeReports {
  def report(stream: DStream[Int]) = {
    stream.foreachRDD {rdd =>
      rdd.foreachPartition { partition =>
        val socket = new Socket("localhost", 9902)
        val out = new PrintWriter(socket.getOutputStream, true)

        partition.foreach(record => out.write(record.toString + "\n"))

        out.close()
      }
    }
  }
}
