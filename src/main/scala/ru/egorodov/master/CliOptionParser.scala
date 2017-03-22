package ru.egorodov.master

/**
  * Created by egorgorodov on 3/22/17.
  */
object CliOptionParser {
  def isMonitoringEnabled(args: Array[String]): Boolean = {
    args.exists(_.startsWith("--monitoring"))
  }
}
