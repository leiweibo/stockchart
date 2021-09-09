package com.wblei.chartlib

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.wblei.chartlib.util.LogUtil

class StockChartView(context: Context?, attributes: AttributeSet) : RelativeLayout(context, attributes), GestureDetector.OnGestureListener {
  
  val TAG: String = StockChartView::class.java.simpleName
  
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    return super.onTouchEvent(event)
  }
  
  override fun onDown(e: MotionEvent?): Boolean {
    return false
  }
  
  override fun onShowPress(e: MotionEvent?) {
  
  }
  
  override fun onSingleTapUp(e: MotionEvent?): Boolean {
    LogUtil.d(TAG, "OnSingleTapUp")
    return false
  }
  
  override fun onScroll(
    e1: MotionEvent?,
    e2: MotionEvent?,
    distanceX: Float,
    distanceY: Float
  ): Boolean {
    
    return false
  }
  
  override fun onLongPress(e: MotionEvent?) {
    LogUtil.d(TAG, "onLongPress")
  }
  
  override fun onFling(
    e1: MotionEvent?,
    e2: MotionEvent?,
    velocityX: Float,
    velocityY: Float
  ): Boolean {
    
    LogUtil.d(TAG, "OnFling")
    return true;
  }
}