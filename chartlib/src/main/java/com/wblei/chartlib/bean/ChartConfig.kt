package com.wblei.chartlib.bean

import android.content.Context
import android.graphics.Paint
import android.graphics.Paint.Style
import androidx.core.content.ContextCompat
import com.wblei.chartlib.ChartHelper
import com.wblei.chartlib.Kline
import com.wblei.chartlib.util.ChartUtil
import kotlin.math.roundToInt

class ChartConfig(context: Context?) {
  
  var context1: Context? = context
  
  
  // 缩放比例
  var zoom: Int = 5
    set(value) {
      field = value
      dataSize = value * 10
      dataStartPos = dataEndPos - dataSize
      stickWidth = (chartWidth - (dataSize - 1) * spaceWidth).toDouble() / dataSize
    }
  
  // 缩放起始点
  var zoomStartPoint: Float = 0.0f
  
  // 一次加载到页面中的数据长度
  var dataSize: Int = zoom * 10
  
  // 数据结束的位置
  var dataEndPos: Int = 0
    set(value) {
      field = dataSize.coerceAtLeast(dataList.size.coerceAtMost(value))
    }
  
  // 数据开始的位置
  var dataStartPos: Int = 0
    set(value) {
      field = 0.coerceAtLeast(value).coerceAtMost(dataList.size)
    }
  
  // 数据列表
  var dataList: MutableList<Kline> = mutableListOf()
    set(value) {
      dataList.clear()
      dataList.addAll(value)
      dataEndPos = dataList.size
      dataStartPos = dataEndPos - dataSize
    }
  
  // 每个蜡烛图的宽度
  var stickWidth: Double = 0.0

  //图形的宽度
  var chartWidth: Int = 0
  
  // 图形的高度
  var chartHeight: Int = 0
  
  // 画图的颜色
  var paintColor: Int = 0
    set(value) {
      paint?.color = ContextCompat.getColor(context1?.applicationContext!!, value)
    }
  
  var paintStyle: Style = Style.FILL
    set(value) {
      paint.style = value
    }
  
  // 蜡烛图之间的宽度
  var spaceWidth: Int = ChartUtil.dp2px(context?.applicationContext!!, 1.0f)
  
  var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
  
  // 是否显示十字线，长按之后显示
  var showCrossLine = false
  
  // 横十字线X
  var crossPointX: Double? = 0.0
    set(value) {
      field = value
      var index = field?.div((stickWidth + spaceWidth))?.roundToInt()
      if (index != null) {
        crossIndex = index + dataStartPos
      }
    }
    get() {
      var index = field?.div((stickWidth + spaceWidth))?.roundToInt()
      return index?.times((stickWidth + spaceWidth))?.plus(stickWidth / 2)
    }
  
  var crossIndex: Int = 0
//  // 竖十字线X
//  var crossPointY: Double? = 0.0
//    get() {
//      var index = field?.div((stickWidth + spaceWidth))?.toInt()
//      return dataList[dataStartPos + index!!].close
//    }
//
  /**
   * 获取当前位置数组中，最大值和最小值，用来计算画蜡烛图的最高和最低点
   */
  fun getMinAndMax(): Pair<Double, Double> {
    var min = Double.MAX_VALUE
    var max = Double.MIN_VALUE
    for (i in dataStartPos until dataEndPos) {
      val bean = dataList[i]
      max = max.coerceAtLeast(bean.close.coerceAtLeast(bean.open))
      min = min.coerceAtMost(bean.close.coerceAtMost(bean.open))
    }
    return Pair(min - 0.5* (max - min), max + 0.5* (max - min))
  }
}
