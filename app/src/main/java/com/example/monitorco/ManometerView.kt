package com.example.monitorco

import android.animation.ValueAnimator
import android.animation.AnimatorListenerAdapter
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
    attrs: AttributeSet? = null,
    maxValue: Int = 35 // Agora o valor máximo pode ser configurado
) : View(context, attrs) {

    private var currentValue = 0 // Valor atual que a agulha vai apontar
    private val totalValue = maxValue // Valor máximo da escala (total do manômetro)

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 80f // Largura do arco
    }

    private val needlePaint = Paint().apply {
        color = Color.BLACK // Cor da agulha
        strokeWidth = 8f // Espessura da agulha
    }

    private val markingPaint = Paint().apply {
        color = Color.BLACK // Cor das marcações
        strokeWidth = 5f // Espessura das marcações
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK // Cor do texto
        textSize = 60f // Tamanho do texto
        textAlign = Paint.Align.CENTER // Alinha o texto ao centro
    }

    private lateinit var arcBounds: RectF // Área do arco
    private var isAnimating = false // Controle de animações

    // Calcula o tamanho e define os limites do arco
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 80f
        val diameter = w - 2 * padding // Usa o mesmo valor para largura e altura
        arcBounds = RectF(padding, h / 2 - diameter / 2, w - padding, h / 2 + diameter / 2)
    }

    // Desenha todos os componentes do manômetro (arcos, agulha, marcações, valor)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Desenha as faixas coloridas
        drawColorArc(canvas, 0, 10, Color.GREEN)
        drawColorArc(canvas, 10, 15, Color.YELLOW)
        drawColorArc(canvas, 15, totalValue, Color.RED)

        // Desenha as marcações
        drawMarkings(canvas)

        // Desenha a agulha
        drawNeedle(canvas)

        // Desenha o valor no centro
        drawValue(canvas)
    }

    // Desenha os arcos coloridos para diferentes faixas de valor
    private fun drawColorArc(canvas: Canvas, start: Int, end: Int, color: Int) {
        val startAngle = 180f + (start / totalValue.toFloat() * 180f)
        val sweepAngle = (end - start) / totalValue.toFloat() * 180f

        paint.color = color
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, paint)
    }

    // Desenha a agulha do manômetro com base no valor atual
    private fun drawNeedle(canvas: Canvas) {
        val angle = 180f + (currentValue / totalValue.toFloat() * 180f)
        val radian = Math.toRadians(angle.toDouble()).toFloat()

        val needleLength = 60f // Comprimento da parte visível da agulha
        val centerX = width / 2
        val centerY = height / 2

        val needleX = centerX + cos(radian.toDouble()) * (arcBounds.width() / 2 - needleLength)
        val needleY = centerY + sin(radian.toDouble()) * (arcBounds.height() / 2 - needleLength)

        // Desenha a linha da agulha
        canvas.drawLine(
            (centerX + cos(radian.toDouble()) * (arcBounds.width() / 2)).toFloat(),
            (centerY + sin(radian.toDouble()) * (arcBounds.height() / 2)).toFloat(),
            needleX.toFloat(),
            needleY.toFloat(),
            needlePaint
        )
    }

    // Desenha as marcações do manômetro
    private fun drawMarkings(canvas: Canvas) {
        val step = 5 // Passo entre as marcações
        val totalSteps = (totalValue / step).toInt()

        for (i in 0..totalSteps) {
            val value = i * step
            val angle = 180f + (value / totalValue.toFloat() * 180f)
            val radian = Math.toRadians(angle.toDouble()).toFloat()

            val startX = (width / 2 + cos(radian.toDouble()) * (arcBounds.width() / 2 + 10)).toFloat()
            val startY = (height / 2 + sin(radian.toDouble()) * (arcBounds.height() / 2 + 10)).toFloat()
            val endX = (width / 2 + cos(radian.toDouble()) * (arcBounds.width() / 2 + 30)).toFloat()
            val endY = (height / 2 + sin(radian.toDouble()) * (arcBounds.height() / 2 + 30)).toFloat()

            // Desenha cada marcação
            canvas.drawLine(startX, startY, endX, endY, markingPaint)
        }
    }

    // Desenha o valor atual no centro do manômetro
    private fun drawValue(canvas: Canvas) {
        val valueText = currentValue.toString() // Converte o valor atual para String
        val circleRadius = 70f // Raio do círculo
        val centerX = width / 2
        val centerY = height / 2

        // Define a cor do círculo baseado no valor
        val circleColor = when {
            currentValue <= 10 -> Color.GREEN
            currentValue <= 15 -> Color.YELLOW
            else -> Color.RED
        }

        // Desenha o círculo colorido
        val circlePaint = Paint().apply {
            color = circleColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), circleRadius, circlePaint)

        // Desenha a borda do círculo
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5f // Largura da borda
        }
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), circleRadius, borderPaint)

        // Desenha o texto sobre o círculo
        canvas.drawText(valueText, centerX.toFloat(), centerY - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
    }

    // Atualiza o valor do manômetro
    fun updateValue(value: Int) {
        currentValue = value.coerceIn(0, totalValue) // Garante que o valor está dentro do intervalo
        invalidate() // Atualiza a visualização
    }

}
