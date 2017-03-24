package ru.egorodov.master

import java.io.{BufferedWriter, PrintWriter, Serializable}
import java.net.Socket

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import ru.egorodov.{Callback, ServerSocketReceiver}
import ru.egorodov.master.transformations.TsvOperations

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

class TsvStreamingTest extends FlatSpec with Matchers with BeforeAndAfter with Eventually {
  var sc: SparkContext = _
  var ssc: StreamingContext = _

  override implicit val patienceConfig = PatienceConfig(timeout = scaled(Span(1000, Millis)))

  before {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("tsv-streaming-test")

    ssc = new StreamingContext(sparkConf, Seconds(1))
    sc = ssc.sparkContext
  }

  after {
    ssc.stop(stopSparkContext = true, stopGracefully = false)
  }

  behavior of "stream transformation"

  it should "apply transformations" in {
    var outputCollector = ListBuffer.empty[Array[Int]]

    val dataToSend: Seq[String] = Seq("1\t2\n", "2\t4\n")

    val callback = new Callback
    val inputStream = ssc.receiverStream(new ServerSocketReceiver(2001, callback))
    val outputStream = TsvOperations.applySumTrasformation(inputStream)

    callback.promise.future onSuccess { case _ =>
      sendToTestSocket(dataToSend)

      assertOutput(outputCollector, List(0, 1))
    }


    outputStream.foreachRDD(rdd => { outputCollector += rdd.collect() })

    ssc.start()
  }

  private def assertOutput(result: Iterable[Array[Int]], expected: List[Int]) =
    eventually {
      val compactedResult = (for { eachResult <- result; el <- eachResult} yield el).toSet
      compactedResult should equal(expected.toSet)
    }

  private def sendToTestSocket(data: Seq[String]): Unit = {
    try {
      val socket = new Socket("127.0.0.1", 2001)
      val out = new BufferedWriter(new PrintWriter(socket.getOutputStream))

      data.foreach {chunk =>
        out.write(chunk)
        out.flush()
      }

      out.close()
    } catch {
      case t: Throwable => println(t.getMessage)
    }
  }
}
