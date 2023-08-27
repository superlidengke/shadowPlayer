package com.example.simplemusic.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import me.rosuh.libmpg123.MPG123
import java.io.File
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Duration: 26.46204, 0.01 frame number: 2641, Sample rate: 44100,Channel: 1
 */

class MyView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var mPts: FloatArray
    private var audioDuration: Float = 0f
    private val screenWidth: Float;
    private val screenHeight: Float;
    private val wavePanelHeight = 600f;
    private val logTag = "MyView"
    var soundPath: String? = null

    // let 0.01 second as one frame
    private val frameNumPerSecond = 100
    private val timePerFrame = 1f / frameNumPerSecond
    private val rowHeight = 300
    private val waveRowHeight = 250
    private val textRowHeight = rowHeight - waveRowHeight
    private val waveRowTime = 5 // seconds
    private val marginTop = 200
    private val marginLeft = 10
    private val marginRight = 30


    init {
        val metrics = Resources.getSystem().displayMetrics
        screenWidth = metrics.widthPixels.toFloat();
        screenHeight = metrics.heightPixels.toFloat()

        Log.d(logTag, "screen: $screenWidth,  $screenHeight")
        buildPoints()
    }

    override fun onDraw(canvas: Canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas)
        //使用Canvas绘图
        //画布移动到(10,10)位置
        canvas.translate(10f, 10f)
        canvas.drawColor(Color.WHITE)
        //创建红色画笔，使用3单像素宽度，绘制直线
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 3f
        paint.isAntiAlias = true;//抗锯齿功能
        canvas.drawLines(mPts, paint)
        //创建蓝色画笔，宽度为1(in hairline mode)，绘制相关点
        paint.color = Color.BLUE
        paint.strokeWidth = 0f
        val samplePoint = getWaveData()
        if (samplePoint.isNotEmpty()) {
            canvas.drawLines(waveLines(samplePoint), paint)
        }
        this.drawTime(canvas, this.audioDuration.toFloat())
        // pause point
        this.drawPause(canvas, samplePoint)

        //创建Path, 并沿着path显示文字信息
        val rect = RectF(20f, 50f, 890f, 330f)
        val path = Path()
        path.addArc(rect, -180f, 180f)
        paint.textSize = 68f
        paint.color = Color.BLUE
        canvas.drawTextOnPath(
            "Audio Waves",
            path,
            0f,
            0f,
            paint
        )
    }

    fun timeToPos(time: Float): Pair<Float, Float> {
        val rowPointNum = this.waveRowTime * this.frameNumPerSecond
        val lineWidth =
            (screenWidth - this.marginLeft - this.marginRight) / rowPointNum

        val idx = (time * frameNumPerSecond).toInt()
        val rowOffset = idx / rowPointNum + 1
        val columnIdx = idx % rowPointNum
        val y =
            rowOffset * this.rowHeight + this.marginTop
        var x = columnIdx * lineWidth + this.marginLeft
        return Pair(x, y.toFloat())
    }

    /**
     * Get the average abs volume in this frame
     */
    fun convertToWaveForm(array: ShortArray): Int {
        return array.map { abs(it.toInt()) }.average().toInt()
    }

    fun getWaveData(): List<Int> {
//        var mp3Path = "/storage/emulated/0/Documents/audio1/320.mp3"
        if (soundPath == null) {
            Log.e(logTag, "soundPath is null")
            return listOf()
        }
        val mp3Path = soundPath!!
        if (!File(mp3Path).exists()) {
            Log.e(logTag, "$mp3Path doesn't exist")
            return listOf()
        }
        if (!File(mp3Path).canRead()) {
            Log.e(logTag, "$mp3Path is not readable")
            return listOf()
        }

        val decoder = MPG123(mp3Path)
        this.audioDuration = decoder.duration
        val pointNumOfFrame = decoder.rate / this.frameNumPerSecond
        var frame = decoder.readFrame()
        println("frame length: ${frame.size}")
        val samplePoints = mutableListOf<Int>()
        val bufferPoints = mutableListOf<Short>()
        bufferPoints.addAll(frame.toList())
        while (true) {
            if (bufferPoints.size < pointNumOfFrame) {
                frame = decoder.readFrame()
                if (frame == null || frame.isEmpty()) {
                    break
                }
                bufferPoints.addAll(frame.toList())
            } else {
                val subList = bufferPoints.subList(0, pointNumOfFrame)
                samplePoints.add(convertToWaveForm(subList.toShortArray()))
                subList.clear()
            }
        }
        if (bufferPoints.size > 0) {
            Log.d("MyView", "Last buffer size: ${bufferPoints.size}")
            samplePoints.add(convertToWaveForm(bufferPoints.toShortArray()))
        }
        Log.d(
            "MyView",
            "Duration: ${decoder.duration}, frame number: ${samplePoints.size}, Sample rate: ${decoder.rate},Channel: ${decoder.numChannels}"
        )
        return samplePoints
    }


    fun waveLines(samplePoint: List<Int>): FloatArray {
        if (samplePoint.isEmpty()) {
            return FloatArray(0)
        }
        val maxSample = samplePoint.max()
        val waveHeights =
            samplePoint.map { it * this.waveRowHeight / maxSample }
        val rowPointNum = this.waveRowTime * this.frameNumPerSecond
        val lineWidth =
            (screenWidth - this.marginLeft - this.marginRight) / rowPointNum

        val linePoints = FloatArray(waveHeights.size * 4)
        Log.i(
            logTag,
            "rowPointNum: $rowPointNum, waveHeights size: ${waveHeights.size}"
        )
        for (i in waveHeights.indices) {
            val rowOffset = i / rowPointNum + 1
            val columnIdx = i % rowPointNum
            val yBase =
                rowOffset * this.rowHeight - this.textRowHeight + this.marginTop
            var x = columnIdx * lineWidth + this.marginLeft

            linePoints[i * 4 + X] = x
            linePoints[i * 4 + Y] = (yBase - waveHeights[i]).toFloat()
            linePoints[i * 4 + X + 2] = x
            linePoints[i * 4 + Y + 2] = yBase.toFloat()
        }
        return linePoints
    }

    fun drawTime(canvas: Canvas, duration: Float) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = this.textRowHeight.toFloat()
        paint.textAlign = Paint.Align.CENTER
        (0..ceil(duration).toInt()).forEach {
            val (x, y) = this.timeToPos(it.toFloat())
            canvas.drawText(it.toString(), x - 3, y, paint)
        }
        paint.strokeWidth = 3f
        (0..10 * ceil(duration).toInt()).forEach {
            val (x, yRow) = this.timeToPos(it / 10f)
            val y = yRow - this.textRowHeight
            var lineHeight = 0f
            val baseHeight = this.textRowHeight / 10f
            if (it % 10 == 0) {
                paint.color = Color.RED
                lineHeight = baseHeight * 4
            } else if (it % 5 == 0) {
                paint.color = Color.RED
                lineHeight = baseHeight * 4
            } else {
                paint.color = Color.BLACK
                lineHeight = baseHeight * 2
            }
            canvas.drawLine(x, y, x, y + lineHeight, paint)
        }
    }

    fun drawPause(canvas: Canvas, datas: List<Int>) {
        val paint = Paint()
        paint.style = Paint.Style.FILL

        val lineWidth = screenWidth / datas.size
        getPausePoint(datas).forEach { pausePair ->
            Log.d(logTag, "pause: $pausePair")
            //TODO change to start + 0.3 seconds
            val pauseStartTime = pausePair.first
            val (x, y) = this.timeToPos(pauseStartTime)
            paint.color = Color.RED
            canvas.drawCircle(x, y, 10f, paint)
            val (x2, y2) = this.timeToPos(pausePair.second)
            paint.color = Color.YELLOW
            canvas.drawCircle(x2, y2, 10f, paint);
        }

    }

    fun getPausePoint(
        datas: List<Int>,
        filterLessThan: Float = 0.15f
    ): List<Pair<Float, Float>> {
        val slientV = 10
        val pausePos: MutableList<Pair<Int, Int>> = mutableListOf()
        var newStart = true
        datas.forEachIndexed { idx, value ->
            if (newStart && value <= slientV) {
                pausePos.add(Pair(idx, idx))
                newStart = false
            } else if (!newStart && value <= slientV) {
                pausePos[pausePos.lastIndex] = Pair(pausePos.last().first, idx)
            } else if (!newStart && value > slientV) {
                newStart = true
            }
        }
        return pausePos.map { it ->
            Pair(
                it.first * this.timePerFrame,
                it.second * this.timePerFrame
            )
        }.filter { it.second - it.first > filterLessThan }
    }

    private fun buildPoints() {
        //生成一系列点
        val ptCount = (SEGS + 1) * 2
        mPts = FloatArray(ptCount * 2)
        var value = 0f
        val delta = SIZE / SEGS
        for (i in 0..SEGS) {
            mPts[i * 4 + X] = SIZE - value
            mPts[i * 4 + Y] = 0f
            mPts[i * 4 + X + 2] = 0f
            mPts[i * 4 + Y + 2] = value
            value += delta
        }
    }

    companion object {
        private const val SIZE = 200f
        private const val SEGS = 10
        private const val X = 0
        private const val Y = 1
    }
}