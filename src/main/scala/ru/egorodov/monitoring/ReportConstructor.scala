package ru.egorodov.monitoring

import org.apache.spark.SparkContext

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
    val totalCount = dataToReport.foldLeft(0l)(_ + _._2)

    s"Ratio(unsuccess/total) = ${dataToReport(0)}/$totalCount"
  }
}
