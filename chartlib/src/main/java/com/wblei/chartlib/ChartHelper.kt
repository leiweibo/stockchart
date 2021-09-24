package com.wblei.chartlib

import android.view.MotionEvent

object ChartHelper {
  /**
   * 计算event两个点之间移动的距离，根据勾股定理去计算
   * c^2 = a^2 + b ^ 2
   */
  fun calculateDistance(event: MotionEvent?): Float {
    val x = event!!.getX(0) - event.getX(1)
    val y = event.getY(0) - event.getY(1)
    return Math.sqrt((x * x + y * y).toDouble()).toFloat()
  }
  
  fun generateY(max: Double, min: Double, y: Double, chartHeight: Int):Double {
    return (max - y) / (max - min) * chartHeight
  }
}