package com.sidbola.cryptrack.features.shared.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sidbola.cryptrack.features.shared.model.ScrubbedCoinData

class CoinGraphSnapshot(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paintLine: Paint = Paint()
    private var dataPoints: ArrayList<FloatPoint> = ArrayList()
    private var delta = 1f
    private var max = Float.MAX_VALUE
    private var numElements = -1

    init {
        initializeView()
    }

    private fun initializeView() {
        paintLine.color = Color.GREEN
        paintLine.isAntiAlias = true
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeJoin = Paint.Join.ROUND
        paintLine.strokeCap = Paint.Cap.ROUND
        paintLine.strokeWidth = context.resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val xMultiplier = measuredWidth / numElements
        val yMultiplier = measuredHeight / max

        if (delta < 0) {
            paintLine.color = Color.parseColor("#f14d3e")
        } else {
            paintLine.color = Color.parseColor("#3ef2b0")
        }

        var i = 0
        while (i < dataPoints.size - 1) {

            val x1 = xMultiplier * dataPoints[i].x
            val x2 = xMultiplier * dataPoints[i + 1].x

            val y1 = measuredHeight - yMultiplier * dataPoints[i].y
            val y2 = measuredHeight - yMultiplier * dataPoints[i + 1].y

            canvas?.drawLine(x1, y1, x2, y2, paintLine)
            i++
        }
    }

    fun setDataPoints(graphData: ScrubbedCoinData) {
        this.max = graphData.max
        this.delta = graphData.percentChange
        this.dataPoints = graphData.dataPoints
        this.numElements = graphData.dataPoints.size - 1
        invalidate()
    }
}