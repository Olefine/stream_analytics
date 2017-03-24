package ru.egorodov.master.transformations

import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import ru.egorodov.master.DataSchema
import ru.egorodov.master.DataSchema.LatLong

object TsvOperations {
  private object SumTransformation extends Function1[LatLong, Int]{
    def apply(latlong: LatLong): Int = {
      if (List(latlong.lat, latlong.long).sum % 2 == 0) 1
      else 0
    }
  }

  def applySumTrasformation(inputData: DStream[String]): DStream[Int] = {
    inputData.print()
    val tsvRows = inputData.flatMap(_.split("\n"))
    val numberPairs = tsvRows.map(_.split("\t")).map(pair =>
      new DataSchema.LatLong(Integer.parseInt(pair(0)), Integer.parseInt(pair(1)))
    )

    numberPairs.map(SumTransformation(_))
  }
}
