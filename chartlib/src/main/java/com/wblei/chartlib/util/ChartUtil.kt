package com.wblei.chartlib.util

import android.content.Context

object ChartUtil {
  fun dp2px(context: Context, dpValue: Float): Int {
    val scale: Float = context.getResources().getDisplayMetrics().density
    return (dpValue * scale + 0.5f).toInt()
  }
}