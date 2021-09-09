package com.wblei.chartlib.util

import android.util.Log

object LogUtil {
  fun d(tag: String, msg: String) {
    Log.d(tag, msg)
  }
  
  fun e(tag: String, msg: String) {
    Log.e(tag, msg)
  }
}