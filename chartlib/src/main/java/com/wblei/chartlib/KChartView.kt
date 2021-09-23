package com.wblei.chartlib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.wblei.chartlib.bean.Chart
import com.wblei.chartlib.util.LogUtil
import kotlin.math.abs

/**
 * K线图
 */
class KChartView(context: Context?, attributes: AttributeSet) : View(context, attributes) {
  
  val TAG: String = KChartView::class.java.simpleName
  
  private val chart: Chart = Chart(context)
  private var startXPos: Float = 0.0f
  private val MINI_MOVE_DISTANCE: Int = if (width / 40 < 5) 5 else width / 50
  
  init {
    setBackgroundResource(android.R.color.white)
  }
  
  fun setupData(klineList: MutableList<Kline>) {
    chart.dataList = klineList
    invalidate()
  }
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    chart.chartWidth = measuredWidth - paddingLeft - paddingRight
    chart.chartHeight = measuredHeight - paddingTop - paddingBottom
    
    chart.stickWidth =
      (chart.chartWidth - (chart.dataSize - 1) * chart.spaceWidth).toDouble() / chart.dataSize
  }
  
  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    if (chart.dataList.size == 0) {
      return;
    }
    
    val (min, max) = chart.getMinAndMax()
    
    for (i in chart.dataStartPos until chart.dataEndPos) {
      val bean = chart.dataList[i]
      val left = (i - chart.dataStartPos) * (chart.stickWidth + chart.spaceWidth)
      val right = left + chart.stickWidth
      val top = ChartHelper.generateY(max, min, bean.close, chart.chartHeight)
      val bottom = ChartHelper.generateY(max, min, bean.open, chart.chartHeight)
      
      if (bean.close > bean.open) { // 阳线，红色不填充
        chart.paintColor = R.color.color_ff4c4f
        chart.paintStyle = Paint.Style.STROKE
      } else {  // 阴线，绿色填充
        chart.paintColor = R.color.color_1dbf69
        chart.paintStyle = Paint.Style.FILL
      }
      
      canvas?.drawRect(
        left.toFloat(),
        top.toFloat(),
        right.toFloat(),
        bottom.toFloat(),
        chart.paint
      )
      
      var startX = (left.toFloat() + (chart.stickWidth / 2.0f)).toFloat()
      var startY = ChartHelper.generateY(max, min, bean.high, chart.chartHeight)
      var stopX = startX
      var stopY = top
      // 画上影线
      canvas?.drawLine(startX, startY.toFloat(), stopX, stopY.toFloat(), chart.paint)
      
      startY = bottom
      stopY = ChartHelper.generateY(max, min, bean.low, chart.chartHeight)
      // 画下影线
      canvas?.drawLine(startX, startY.toFloat(), stopX, stopY.toFloat(), chart.paint)
    }
  }
  
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    
    when (event?.action) {
      MotionEvent.ACTION_DOWN -> onActionDown(event)
      MotionEvent.ACTION_MOVE -> onActionMove(event)
      MotionEvent.ACTION_UP -> onActionUp(event)
    }
    
    return true
  }
  
  private fun onActionDown(event: MotionEvent?) {
    startXPos = event?.x ?: 0.0f
  }
  
  /**
   * 放大缩小处理，这个在onTouch事件里面，根据pointerCount来判断是否双指缩放的操作
   */
  private fun onActionMove(event: MotionEvent?) {
    if (event?.pointerCount == 1) {
      
      // 拖动距离
      val dragDistance = event.x - startXPos
      var dragRectCnt = (dragDistance / chart.stickWidth).toInt()
      if (abs(dragRectCnt) >= 1) {
        chart.dataEndPos -= dragRectCnt
        chart.dataStartPos = chart.dataEndPos - chart.dataSize
        LogUtil.d(
          TAG,
          "Dragging the chart， distance: $dragDistance, dragRectCnt: $dragRectCnt, dataStartPos: ${chart.dataStartPos}, dataEndPos: ${chart.dataEndPos}"
        )
        // 对于上次已经计算过的点，重新赋值
        startXPos = event.x
      }
      
      invalidate()
    }
    if (event?.pointerCount == 2) {
      val moveDistance = ChartHelper.calculateSpace(event)

      if (moveDistance < MINI_MOVE_DISTANCE) return

      chart.zoom *= if (chart.zoomStartPoint > 0) {
        (moveDistance / chart.zoomStartPoint).toInt()
      } else  {
        1
      }
      chart.zoomStartPoint = moveDistance
      invalidate()
      LogUtil.d(TAG, "onScale $moveDistance, zoom: ${chart.zoom}")
    }
  }
  
  private fun onActionUp(event: MotionEvent?) {
    LogUtil.d(TAG, "onActionUp")
    chart.zoomStartPoint = 0.0f;
  }
  
}
