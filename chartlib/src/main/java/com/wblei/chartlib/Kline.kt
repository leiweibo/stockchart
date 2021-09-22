package com.wblei.chartlib

data class Kline(
  val close: Double,
  val high: Double,
  val holding: Double,
  val low: Double,
  val open: Double,
  val time: Long,
  val volume: Double
)
