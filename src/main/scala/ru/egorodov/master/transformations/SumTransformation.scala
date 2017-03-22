package ru.egorodov.master.transformations

import ru.egorodov.master.DataSchema.LatLong

object SumTransformation extends Function1[LatLong, Int]{
  def apply(latlong: LatLong): Int = {
    if (List(latlong.lat, latlong.long).sum % 2 == 0) 1
    else 0
  }
}
