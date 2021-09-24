package com.wblei.chartlib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import com.wblei.chartlib.bean.ChartConfig
import com.wblei.chartlib.util.LogUtil
import kotlin.math.abs

/**
 * K线图
 */
class KChartView(context: Context?, attributes: AttributeSet) : View(context, attributes), OnGestureListener {
  
  val TAG: String = KChartView::class.java.simpleName
  
  private val chartConfig: ChartConfig = ChartConfig(context)
  private var startXPos: Float = 0.0f
  private val MINI_MOVE_DISTANCE: Int = if (width / 40 < 5) 5 else width / 50
  private val gestureDetector: GestureDetector = GestureDetector(context, this)

  
  init {
    setBackgroundResource(android.R.color.white)
  }
  
  fun setupData(klineList: MutableList<Kline>) {
    chartConfig.dataList = klineList
    invalidate()
  }
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    chartConfig.chartWidth = measuredWidth - paddingLeft - paddingRight
    chartConfig.chartHeight = measuredHeight - paddingTop - paddingBottom
    
    chartConfig.stickWidth =
      (chartConfig.chartWidth - (chartConfig.dataSize - 1) * chartConfig.spaceWidth).toDouble() / chartConfig.dataSize
  }
  
  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    if (chartConfig.dataList.size == 0) {
      return;
    }
    
    val (min, max) = chartConfig.getMinAndMax()
    
    for (i in chartConfig.dataStartPos until chartConfig.dataEndPos) {
      val bean = chartConfig.dataList[i]
      if (chartConfig.zoom < 10) {
        val left = (i - chartConfig.dataStartPos) * (chartConfig.stickWidth + chartConfig.spaceWidth)
        val right = left + chartConfig.stickWidth
        val top = ChartHelper.generateY(max, min, bean.close, chartConfig.chartHeight)
        val bottom = ChartHelper.generateY(max, min, bean.open, chartConfig.chartHeight)
  
        if (bean.close > bean.open) { // 阳线，红色不填充
          chartConfig.paintColor = R.color.color_ff4c4f
          chartConfig.paintStyle = Paint.Style.STROKE
        } else {  // 阴线，绿色填充
          chartConfig.paintColor = R.color.color_1dbf69
          chartConfig.paintStyle = Paint.Style.FILL
        }
  
        canvas?.drawRect(
          left.toFloat(),
          top.toFloat(),
          right.toFloat(),
          bottom.toFloat(),
          chartConfig.paint
        )
  
        var startX = (left.toFloat() + (chartConfig.stickWidth / 2.0f)).toFloat()
        var startY = ChartHelper.generateY(max, min, bean.high, chartConfig.chartHeight)
        var stopX = startX
        var stopY = top
        // 画上影线
        canvas?.drawLine(startX, startY.toFloat(), stopX, stopY.toFloat(), chartConfig.paint)
  
        startY = bottom
        stopY = ChartHelper.generateY(max, min, bean.low, chartConfig.chartHeight)
        // 画下影线
        canvas?.drawLine(startX, startY.toFloat(), stopX, stopY.toFloat(), chartConfig.paint)
      } else {
        if (i - chartConfig.dataStartPos > 0) {
          val preBean = chartConfig.dataList[i - 1]
          var startX = (i - chartConfig.dataStartPos - 1) * (chartConfig.stickWidth + chartConfig.spaceWidth).toFloat()
          var stopX = (i - chartConfig.dataStartPos) * (chartConfig.stickWidth + chartConfig.spaceWidth).toFloat()
          var startY = ChartHelper.generateY(max, min, preBean.close, chartConfig.chartHeight).toFloat()
          var stopY = ChartHelper.generateY(max, min, bean.close, chartConfig.chartHeight).toFloat()
          canvas?.drawLine(startX, startY, stopX, stopY, chartConfig.paint)
        }
      }
    }
    if (chartConfig.showCrossLine) {
      // 横十字线
      val horStartX = 0
      val horStartY = ChartHelper.generateY(max, min, chartConfig.dataList[chartConfig.crossIndex].close, chartConfig.chartHeight)
      val horStopX = chartConfig.chartWidth
      var horStopY = ChartHelper.generateY(max, min, chartConfig.dataList[chartConfig.crossIndex].close, chartConfig.chartHeight)
        LogUtil.d(TAG, "画 --- 十字线")
        canvas?.drawLine(
          horStartX.toFloat(),
          horStartY!!.toFloat(),
          horStopX?.toFloat(),
          horStopY!!.toFloat(),
          chartConfig.paint
        )
    
      LogUtil.d(TAG, "画 ||| 十字线")
      // 竖十字线
      var verStartX = chartConfig.crossPointX
      val verStartY = 0.0f
      var verStopX = chartConfig.crossPointX
      val verStopY = chartConfig.chartHeight
      canvas?.drawLine(
        verStartX!!.toFloat(),
        verStartY,
        verStopX!!.toFloat(),
        verStopY.toFloat(),
        chartConfig.paint
      )
    }
  }
  
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    
    when (event?.action) {
      MotionEvent.ACTION_DOWN -> onActionDown(event)
      MotionEvent.ACTION_MOVE -> onActionMove(event)
      MotionEvent.ACTION_UP -> onActionUp(event)
    }
    gestureDetector.onTouchEvent(event)
    return true
  }
  
  private fun onActionDown(event: MotionEvent?) {
    startXPos = event?.x ?: 0.0f
  }
  
  /**
   * 放大缩小处理，这个在onTouch事件里面，根据pointerCount来判断是否双指缩放的操作
   */
  private fun onActionMove(event: MotionEvent?) {
    if (chartConfig.showCrossLine) {
      if (event?.pointerCount == 1) {
        val dragDistance = event.x - startXPos
        var dragRectCnt = (dragDistance / chartConfig.stickWidth).toInt()
        if (abs(dragRectCnt) >= 1) {
          chartConfig.crossPointX = event?.x.toDouble()
          startXPos = event.x
          LogUtil.d(TAG, "execute invalidate in onAction move, showCross and point count is 1")
          invalidate()
        }
      }
    } else {
      if (event?.pointerCount == 1) {
    
        // 拖动距离
        val dragDistance = event.x - startXPos
        var dragRectCnt = (dragDistance / chartConfig.stickWidth).toInt()
        if (abs(dragRectCnt) >= 1) {
          chartConfig.dataEndPos -= dragRectCnt
          chartConfig.dataStartPos = chartConfig.dataEndPos - chartConfig.dataSize
          LogUtil.d(
            TAG,
            "Dragging the chart， distance: $dragDistance, dragRectCnt: $dragRectCnt, dataStartPos: ${chartConfig.dataStartPos}, dataEndPos: ${chartConfig.dataEndPos}"
          )
          // 对于上次已经计算过的点，重新赋值
          startXPos = event.x
          LogUtil.d(TAG, "execute invalidate in onAction move, drag the chat and point count is 1")
          invalidate()
        }
      } else if (event?.pointerCount == 2) {
        val moveDistance = ChartHelper.calculateDistance(event)
    
        if (moveDistance < MINI_MOVE_DISTANCE) return
    
        if (chartConfig.zoomStartPoint > 0 && moveDistance > chartConfig.zoomStartPoint) {
          // 放大，zoom--操作
          chartConfig.zoom = chartConfig.zoom - 1
          if (chartConfig.zoom < 1) {
            chartConfig.zoom = chartConfig.zoom.coerceAtLeast(1)
            // todo break the loop.
          }
        } else if (moveDistance < chartConfig.zoomStartPoint) {
          // 缩小，zoom++操作
          chartConfig.zoom = chartConfig.zoom + 1
          if (chartConfig.zoom > 10) {
            chartConfig.zoom = chartConfig.zoom.coerceAtMost(10)
            // todo break the loop.
          }
        }
        chartConfig.zoomStartPoint = moveDistance
        LogUtil.d(
          TAG,
          "onScale $moveDistance, zoom: ${chartConfig.zoom}, dataSize: ${chartConfig.dataSize}, dataStartPos: ${chartConfig.dataStartPos}, dataEndPos: ${chartConfig.dataEndPos}"
        )
        LogUtil.d(TAG, "execute invalidate in onAction move, drag the chat and point count is 2")
        invalidate()
      }
    }
  }
  
  private fun onActionUp(event: MotionEvent?) {
    LogUtil.d(TAG, "onActionUp")
    chartConfig.showCrossLine = false
    chartConfig.zoomStartPoint = 0.0f
    invalidate()
  }
  
  
  override fun onDown(e: MotionEvent?): Boolean {
    return false
  }
  
  override fun onShowPress(e: MotionEvent?) {
  
  }
  
  override fun onSingleTapUp(e: MotionEvent?): Boolean {
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
    LogUtil.d(TAG, "OnLongPress...")
    chartConfig.showCrossLine = true
    chartConfig.crossPointX = e?.x?.toDouble()
    invalidate()
    
  }
  
  override fun onFling(
    e1: MotionEvent?,
    e2: MotionEvent?,
    velocityX: Float,
    velocityY: Float
  ): Boolean {
    return false
  }
}
