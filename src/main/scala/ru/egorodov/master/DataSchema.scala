package ru.egorodov.master

/**
  * Created by egorgorodov on 3/22/17.
  */
case object DataSchema {
  class LatLong(val lat: Int, val long: Int) extends Tuple2[Int, Int](lat, long)
}
