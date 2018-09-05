package com.sidbola.cryptrack.features.shared.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sidbola.cryptrack.features.shared.model.ScrubbedCoinData

const val PRICE_INDICATOR_BOX_WIDTH = 200f
const val PRICE_INDICATOR_BOX_HEIGHT = 50f
const val PRICE_INDICATOR_BOX_OFFSET = 20f

const val PRICE_INDICATOR_POINTER_WIDTH = 22f
const val PRICE_INDICATOR_POINTER_HEGHT = 22f

class CoinGraphDetail(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paintLine: Paint = Paint()
    private var dataPoints: ArrayList<FloatPoint> = ArrayList()
    private var delta = 1f
    private var max = Float.MAX_VALUE
    private var numElements = -1
    val path = Path()

    private var pricePoints: ArrayList<Float> = ArrayList()

    private var indicatorPaintLine: Paint = Paint()
    private var pinPaintLine: Paint = Paint()

    private var textPaintLine: Paint = Paint()

    private var isTouching = false
    private var closestPoint = FloatPoint(0f, 0f)
    private var currentPrice = ""

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

        textPaintLine.color = Color.WHITE
        textPaintLine.isAntiAlias = true
        textPaintLine.style = Paint.Style.STROKE
        textPaintLine.textAlign = Paint.Align.CENTER
        textPaintLine.textSize = 20f

        indicatorPaintLine.isAntiAlias = true
        indicatorPaintLine.style = Paint.Style.FILL

        pinPaintLine.strokeWidth = context.resources.displayMetrics.density
        pinPaintLine.style = Paint.Style.FILL_AND_STROKE
        pinPaintLine.isAntiAlias = true

        // path.fillType = Path.FillType.EVEN_ODD
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val xMultiplier = measuredWidth / numElements
        val yMultiplier = measuredHeight / max

        if (delta < 0) {
            paintLine.color = Color.parseColor("#f14d3e")
            indicatorPaintLine.color = Color.parseColor("#f14d3e")
            pinPaintLine.color = Color.parseColor("#f14d3e")
        } else {
            paintLine.color = Color.parseColor("#3ef2b0")
            indicatorPaintLine.color = Color.parseColor("#2ba578")
            pinPaintLine.color = Color.parseColor("#2ba578")
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

        // Axis

        // Pointer/cursor logic
        // TODO: Refactor this working code to be more organized and readable
        if (isTouching) {
            path.reset()
            path.moveTo(closestPoint.x, closestPoint.y)

            if (priceIndicatorBoxIsOffscreenTop()) {

                when {
                    priceIndicatorBoxIsOffscreenLeft() -> {
                        canvas?.drawRoundRect(10f, closestPoint.y + 20,
                                210f, closestPoint.y + 70, 10f, 10f, indicatorPaintLine)

                        if (indicatorBoxPointerShouldScrollLeft()) {
                            path.lineTo(35f, closestPoint.y + 22)
                            path.lineTo(12f, closestPoint.y + 50)
                        } else {
                            path.lineTo(closestPoint.x + 10, closestPoint.y + 22)
                            path.lineTo(closestPoint.x - 10, closestPoint.y + 22)
                        }

                        canvas?.drawText(currentPrice, 110f, closestPoint.y + 50, textPaintLine)
                    }
                    priceIndicatorBoxIsOffscreenRight() -> {
                        canvas?.drawRoundRect(measuredWidth - 210f, closestPoint.y + 20,
                                measuredWidth - 10f, closestPoint.y + 70, 10f, 10f, indicatorPaintLine)

                        if (indicatorBoxPointerShouldScrollRight()) {
                            path.lineTo(measuredWidth - 35f, closestPoint.y + 22)
                            path.lineTo(measuredWidth - 12f, closestPoint.y + 50)
                        } else {
                            path.lineTo(closestPoint.x + 10, closestPoint.y + 22)
                            path.lineTo(closestPoint.x - 10, closestPoint.y + 22)
                        }

                        canvas?.drawText(currentPrice, measuredWidth - 110f, closestPoint.y + 50, textPaintLine)
                    }
                    else -> {
                        canvas?.drawRoundRect(closestPoint.x - 100, closestPoint.y + 20,
                                closestPoint.x + 100, closestPoint.y + 70, 10f, 10f, indicatorPaintLine)

                        path.lineTo(closestPoint.x + 10, closestPoint.y + 22)
                        path.lineTo(closestPoint.x - 10, closestPoint.y + 22)

                        canvas?.drawText(currentPrice, closestPoint.x, closestPoint.y + 50, textPaintLine)
                    }
                }
            } else {

                when {
                    priceIndicatorBoxIsOffscreenLeft() -> {
                        canvas?.drawRoundRect(10f, closestPoint.y - 70,
                                210f, closestPoint.y - 20, 10f, 10f, indicatorPaintLine)

                        if (indicatorBoxPointerShouldScrollLeft()) {
                            path.lineTo(35f, closestPoint.y - 22)
                            path.lineTo(12f, closestPoint.y - 50)
                        } else {
                            path.lineTo(closestPoint.x + 10, closestPoint.y - 22)
                            path.lineTo(closestPoint.x - 10, closestPoint.y - 22)
                        }

                        canvas?.drawText(currentPrice, 110f, closestPoint.y - 40, textPaintLine)
                    }
                    priceIndicatorBoxIsOffscreenRight() -> {
                        canvas?.drawRoundRect(measuredWidth - 210f, closestPoint.y - 70,
                                measuredWidth - 10f, closestPoint.y - 20, 10f, 10f, indicatorPaintLine)

                        if (indicatorBoxPointerShouldScrollRight()) {
                            path.lineTo(measuredWidth - 35f, closestPoint.y - 22)
                            path.lineTo(measuredWidth - 12f, closestPoint.y - 50)
                        } else {
                            path.lineTo(closestPoint.x + 10, closestPoint.y - 22)
                            path.lineTo(closestPoint.x - 10, closestPoint.y - 22)
                        }

                        canvas?.drawText(currentPrice, measuredWidth - 110f, closestPoint.y - 40, textPaintLine)
                    }
                    else -> {
                        canvas?.drawRoundRect(closestPoint.x - 100, closestPoint.y - 70,
                                closestPoint.x + 100, closestPoint.y - 20, 10f, 10f, indicatorPaintLine)

                        path.lineTo(closestPoint.x + 10, closestPoint.y - 22)
                        path.lineTo(closestPoint.x - 10, closestPoint.y - 22)

                        canvas?.drawText(currentPrice, closestPoint.x, closestPoint.y - 40, textPaintLine)
                    }
                }
            }

            path.close()

            canvas?.drawPath(path, pinPaintLine)
        }
    }

    private fun priceIndicatorBoxIsOffscreenLeft(): Boolean {
        return closestPoint.x < 110
    }

    private fun priceIndicatorBoxIsOffscreenRight(): Boolean {
        return closestPoint.x > measuredWidth - 110
    }

    private fun priceIndicatorBoxIsOffscreenTop(): Boolean {
        return closestPoint.y - 100 < 0
    }

    private fun indicatorBoxPointerShouldScrollLeft(): Boolean {
        return closestPoint.x < 20
    }

    private fun indicatorBoxPointerShouldScrollRight(): Boolean {
        return closestPoint.x > measuredWidth - 20
    }

    private fun closestInList(xCoordinate: Float): FloatPoint {

        val xMultiplier = measuredWidth / numElements
        val yMultiplier = measuredHeight / max

        var min = Math.abs(xCoordinate - (dataPoints[0].x * xMultiplier))
        var diff: Float
        var priceAtPoint = pricePoints[0]

        var cPoint = FloatPoint(dataPoints[0].x * xMultiplier, dataPoints[0].y * yMultiplier)

        for (point in dataPoints) {
            diff = Math.abs(xCoordinate - (point.x * xMultiplier))

            if (diff <= min) {
                min = diff
                cPoint = FloatPoint(point.x * xMultiplier, measuredHeight - (point.y * yMultiplier))
                priceAtPoint = pricePoints[dataPoints.indexOf(point)]
            }
        }

        currentPrice = "$ $priceAtPoint"
        return cPoint
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (!dataPoints.isEmpty()) {
            val x = event?.x

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    x?.let {
                        isTouching = true
                        closestPoint = closestInList(x)
                        invalidate()
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    x?.let {
                        closestPoint = closestInList(x)
                        invalidate()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    x?.let {
                        isTouching = false
                        closestPoint = closestInList(x)
                        invalidate()
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    isTouching = false
                }
            }
        }

        return true
    }

    fun setDataPoints(graphData: ScrubbedCoinData) {
        this.max = graphData.max
        this.delta = graphData.percentChange
        this.dataPoints = graphData.dataPoints
        this.numElements = graphData.dataPoints.size - 1
        this.pricePoints = graphData.pricePoints
        invalidate()
    }
}