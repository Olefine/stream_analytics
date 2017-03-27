package ru.egorodov.monitoring

import org.apache.spark.SparkContext
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import ru.egorodov.util.CommunicationSettings

private[monitoring] class ReportConstructor(val sc: SparkContext, dataToReport: collection.Map[Int, Long]) {
  def send() = {
    val resultRDD = sc.parallelize(Seq(constructReport()), 1)
    resultRDD.saveAsTextFile(constructFilename())
  }

  private def constructFilename(): String = {
    val f = new java.text.SimpleDateFormat("dd-mm-yy")
    s"monitoring-report-${f.format(new java.util.Date())}"
  }

  private def constructReport(): String = {
    s"Ratio(unsuccess/total) = $ratio"
  }

  private def sendRatio() = {
    val postRequest = new HttpPost(CommunicationSettings.monitoringRatioCheckerServiceUrl)
    postRequest.setHeader("Content-type", "application/json")
    postRequest.setEntity(new StringEntity(constructRequestBody()))
    (new DefaultHttpClient).execute(postRequest)
  }

  private def constructRequestBody(): String = {
    //since this request is very simple, no need to use additional libs like com.google.gson
    s"""
      |{
      | "ratio" : $ratio,
      | "applicationName" : "${CommunicationSettings.masterApplicationName}"
      |}
    """.stripMargin
  }

  def ratio: Double = {
    val totalCount = dataToReport.foldLeft(0l)(_ + _._2)
    dataToReport(0) / totalCount
  }
}
