package com.character.ocmaker.creator.core.custom.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.character.ocmaker.creator.R
import kotlin.math.ceil

/** Keeps the existing background bounds; the soft shadow extends outside them. */
class ColorListShadowDrawable(context: Context) : Drawable() {
    private val blurRadius = context.resources.getDimension(R.dimen.dp_2)
    private val offsetY = context.resources.getDimension(R.dimen.dp_2)
    private val outset = ceil(blurRadius * 2f + offsetY).toInt()
    private val content = requireNotNull(
        ContextCompat.getDrawable(context, R.drawable.bg_color_list_shape)
    ).mutate()
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        setShadowLayer(blurRadius, 0f, offsetY, 0x80FFFFFF.toInt())
    }
    private var cachedBitmap: Bitmap? = null
    private val dirtyBounds = Rect()

    override fun onBoundsChange(bounds: Rect) {
        cachedBitmap = null
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        if (width <= 0 || height <= 0) return

        val bitmap = cachedBitmap ?: Bitmap.createBitmap(
            width + outset * 2,
            height + outset * 2,
            Bitmap.Config.ARGB_8888
        ).also { bitmap ->
            // A bitmap Canvas supports blurred shape shadows on older Android too.
            val bitmapCanvas = Canvas(bitmap)
            bitmapCanvas.drawRect(
                outset.toFloat(),
                outset.toFloat(),
                (outset + width).toFloat(),
                (outset + height).toFloat(),
                shadowPaint
            )
            content.setBounds(outset, outset, outset + width, outset + height)
            content.draw(bitmapCanvas)
            cachedBitmap = bitmap
        }
        canvas.drawBitmap(
            bitmap,
            (bounds.left - outset).toFloat(),
            (bounds.top - outset).toFloat(),
            bitmapPaint
        )
    }

    override fun getDirtyBounds(): Rect {
        dirtyBounds.set(bounds)
        dirtyBounds.inset(-outset, -outset)
        return dirtyBounds
    }

    override fun setAlpha(alpha: Int) {
        bitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        bitmapPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Suppress("DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
