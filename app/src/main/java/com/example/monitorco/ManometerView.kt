package com.example.monitorco

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class ManometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentValue = 0f
    private val totalValue = 35f

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 80f
    }

    private val needlePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
    }

    private val markingPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK // Cor do texto
        textSize = 60f // Tamanho do texto
        textAlign = Paint.Align.CENTER // Alinha o texto ao centro
    }

    private lateinit var arcBounds: RectF

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Define os limites do arco com altura e largura iguais
        val padding = 80f
        val diameter = w - 2 * padding // Use o mesmo valor para largura e altura
        arcBounds = RectF(padding, h / 2 - diameter / 2, w - padding, h / 2 + diameter / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Desenhar as faixas coloridas
        drawColorArc(canvas, 0f, 10f, Color.GREEN)
        drawColorArc(canvas, 10f, 15f, Color.YELLOW)
        drawColorArc(canvas, 15f, 35f, Color.RED)

        // Desenhar as marcações
        drawMarkings(canvas)

        // Desenhar a agulha
        drawNeedle(canvas)

        // Desenhar o valor no centro
        drawValue(canvas)
    }

    private fun drawColorArc(canvas: Canvas, start: Float, end: Float, color: Int) {
        val startAngle = 180f + (start / totalValue * 180f)
        val sweepAngle = (end - start) / totalValue * 180f

        paint.color = color
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, paint)
    }

    private fun drawNeedle(canvas: Canvas) {
        val angle = 180f + (currentValue / totalValue * 180f)
        val radian = Math.toRadians(angle.toDouble()).toFloat()

        // Comprimento da parte visível da agulha (ajuste o valor para recuar)
        val needleLength = 60f // Reduzido para recuar a agulha
        val centerX = width / 2
        val centerY = height / 2

        // Posição da ponta da agulha
        val needleX = centerX + cos(radian.toDouble()) * (arcBounds.width() / 2 - needleLength)
        val needleY = centerY + sin(radian.toDouble()) * (arcBounds.height() / 2 - needleLength)

        // Desenhar a agulha
        canvas.drawLine(
            (centerX + cos(radian.toDouble()) * (arcBounds.width() / 2)).toFloat(),
            (centerY + sin(radian.toDouble()) * (arcBounds.height() / 2)).toFloat(),
            needleX.toFloat(),
            needleY.toFloat(),
            needlePaint
        )
    }

    private fun drawMarkings(canvas: Canvas) {
        val step = 5f
        val totalSteps = (totalValue / step).toInt()

        for (i in 0..totalSteps) {
            val value = i * step
            val angle = 180f + (value / totalValue * 180f)
            val radian = Math.toRadians(angle.toDouble()).toFloat()

            // Posição de início e fim da marcação
            val startX = (width / 2 + cos(radian.toDouble()) * (arcBounds.width() / 2 + 10)).toFloat()
            val startY = (height / 2 + sin(radian.toDouble()) * (arcBounds.height() / 2 + 10)).toFloat()
            val endX = (width / 2 + cos(radian.toDouble()) * (arcBounds.width() / 2 + 30)).toFloat()
            val endY = (height / 2 + sin(radian.toDouble()) * (arcBounds.height() / 2 + 30)).toFloat()

            // Desenhar a marcação
            canvas.drawLine(startX, startY, endX, endY, markingPaint)
        }
    }

    private fun drawValue(canvas: Canvas) {
        val valueText = currentValue.toInt().toString() // Converte o valor atual para String
        val circleRadius = 70f // Raio do círculo
        val centerX = width / 2
        val centerY = height / 2

        // Determina a cor do círculo com base no valor
        val circleColor = when {
            currentValue <= 10 -> Color.GREEN
            currentValue <= 15 -> Color.YELLOW
            else -> Color.RED
        }

        // Desenhar o círculo
        val circlePaint = Paint().apply {
            color = circleColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), circleRadius, circlePaint)

        // Desenhar a borda do círculo
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5f // Largura da borda
        }
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), circleRadius, borderPaint)

        // Desenhar o texto sobre o círculo
        canvas.drawText(valueText, centerX.toFloat(), centerY + textPaint.textSize / 3, textPaint)
    }

    fun updateValue(value: Float) {
        currentValue = value.coerceIn(0f, totalValue)
        invalidate() // Atualiza a visualização
    }

    fun animateValue(from: Float, to: Float) {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = 1000 // Duração da animação em milissegundos
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            updateValue(animatedValue)
        }
        animator.start()
    }
}
