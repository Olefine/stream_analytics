package ru.egorodov

import java.io.{BufferedReader, InputStreamReader}
import java.net.{InetAddress, ServerSocket, Socket}

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import java.nio.charset.StandardCharsets

import scala.concurrent.{Future, Promise}

class Callback extends Serializable {
  def promise = Promise[Unit]
}

class ServerSocketReceiver(port: Int, initializedCallback: Callback = null) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) {

  private var socket: ServerSocket = null

  def onStart() {
    new Thread("Socket Receiver") {
      override def run() { receive() }
    }.start()
  }

  def onStop(): Unit = {
    socket.close()
  }

  private def receive() {
    var userInput: String = null
    try {
      socket = new ServerSocket(port)
      val s: Socket = socket.accept()

      invokeServerSocketInitialized()

      val reader = new BufferedReader(new InputStreamReader(s.getInputStream, StandardCharsets.UTF_8))

      userInput = reader.readLine()

      while(!isStopped && userInput != null) {
        store(userInput)
        userInput = reader.readLine()
      }

      reader.close()
    } catch {
      case e: java.net.ConnectException =>
        restart("Error connecting to " + "localhost:" + port, e)
      case t: Throwable =>
        restart("Error receiving data", t)
    }
  }

  private def invokeServerSocketInitialized() = {
    if (initializedCallback != null)
      initializedCallback.promise success ()
  }
}
