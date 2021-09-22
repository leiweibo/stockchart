package com.wblei.chartlib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.wblei.chartlib.R.color
import com.wblei.chartlib.util.ChartUtil
import com.wblei.chartlib.util.LogUtil

/**
 * K线图
 */
class KChartView(context: Context?, attributes: AttributeSet) : View(context, attributes),
  GestureDetector.OnGestureListener {
  
  val TAG: String = KChartView::class.java.simpleName
  
  // 手势检测
  private var gestureDetector: GestureDetector = GestureDetector(context, this)
  
  private var colorRed = ContextCompat.getColor(context?.applicationContext!!, color.color_ff4c4f)
  private var colorGreen = ContextCompat.getColor(context?.applicationContext!!, color.color_1dbf69)
  // 画笔
  private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
  // 最小移动距离
  private val MINI_MOVE_DISTANCE: Int
  
  // 放大缩小比例
  private var zoom: Float = 1f
  
  private var startPointerDistance:Float = 0.0f
  
  private val datas: MutableList<Kline> = mutableListOf()
  
  // 数据开始的位置
  private var start: Int = 100
  // 数据结束的位置
  private var end: Int = 150
  
  private var spaceWidth: Int
  
  // K线图长方形个数
  private var size = 50
  
  private var stickWidth: Double = 0.0
  
  // 图形的宽高
  private var chartWidth: Int = 0
  private var chartHeight: Int = 0
  
  private var f12: Int = ChartUtil.dp2px(context!!.applicationContext, 12f)
  
  init {
    MINI_MOVE_DISTANCE = if (width / 40 < 5) 5 else width / 50
    spaceWidth = ChartUtil.dp2px(context!!.applicationContext, 1.0f)
    setBackgroundResource(android.R.color.darker_gray)
  }
  
  fun setupData(klineList: MutableList<Kline>) {
    datas.clear()
    datas.addAll(klineList)
    invalidate()
  }
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    chartWidth = measuredWidth - paddingLeft - paddingRight
    chartHeight = measuredHeight - paddingTop - paddingBottom
  
    stickWidth = (chartWidth - (size - 1) * spaceWidth).toDouble() / size
  }
  
  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    if (datas.size == 0) {
      return;
    }
    
    val (min, max) = getMinAndMax(datas, start, end)
    
    for (i in start until end) {
      val bean = datas[i]
      val left = (i - start) * (stickWidth + spaceWidth)
      val right = left + stickWidth
      val top = generateY(max, min, bean.close)
      val bottom = generateY(max, min, bean.open)
      
      if (bean.close > bean.open) { // 阳线，红色不填充
        paint.color = colorRed
        paint.style = Paint.Style.STROKE
      } else  {  // 阴线，绿色填充
        paint.color = colorGreen
        paint.style = Paint.Style.FILL
      }
      
      canvas?.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
      
      var startX = (left.toFloat() + (stickWidth / 2.0f)).toFloat()
      var startY = generateY(max, min, bean.high)
      var stopX = startX
      var stopY = top
      // 画上影线
      canvas?.drawLine(startX, startY.toFloat(), stopX, stopY.toFloat(), paint)
      
      startY = bottom
      stopY = generateY(max, min, bean.low)
      // 画下影线
      canvas?.drawLine(startX, startY.toFloat(), stopX, stopY.toFloat(), paint)
    }
  }
  
  private fun generateY(max: Double, min: Double, y: Double):Double {
    return (max - y) / (max - min) * chartHeight
  }
  
  /**
   * 获取当前位置数组中，最大值和最小值，用来计算画蜡烛图的最高和最低点
   */
  private fun getMinAndMax(datas: MutableList<Kline>, start: Int, end: Int): Pair<Double, Double> {
    var min = Double.MAX_VALUE
    var max = Double.MIN_VALUE
    for (i in start until end) {
      val bean = datas[i]
      max = max.coerceAtLeast(bean.close.coerceAtLeast(bean.open))
      min = min.coerceAtMost(bean.close.coerceAtMost(bean.open))
    }
    return Pair(min - 0.16* (max - min), max + 0.16* (max - min))
  }
  
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    
    when (event?.action) {
      MotionEvent.ACTION_MOVE -> onScale(event)
      MotionEvent.ACTION_UP -> onActionUp(event)
    }
    
    gestureDetector.onTouchEvent(event)
    return true
  }
  
  /**
   * 放大缩小处理，这个在onTouch事件里面，根据pointerCount来判断是否双指缩放的操作
   */
  private fun onScale(event: MotionEvent?) {
    if (event?.pointerCount == 2) {
      val moveDistance = ChartHelper.calculateSpace(event)
      
      if (moveDistance < MINI_MOVE_DISTANCE) return
      
      zoom *= if (startPointerDistance > 0) {
        moveDistance / startPointerDistance
      } else  {
        1.0f
      }
      startPointerDistance = moveDistance
      invalidate()
      LogUtil.d(TAG, "onScale $moveDistance, zoom: $zoom")
    }
  }
  
  private fun onActionUp(event: MotionEvent?) {
    LogUtil.d(TAG, "onActionUp")
    startPointerDistance = 0.0f;
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