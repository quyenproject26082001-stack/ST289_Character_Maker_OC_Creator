package com.character.ocmaker.creator.core.custom.layout

import android.content.Context
import android.text.Layout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import com.character.ocmaker.creator.R

/** Keeps the center title centered on the bar while reserving space for visible actions. */
class ActionBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var startBarrier: Barrier
    private lateinit var endBarrier: Barrier
    private lateinit var startActions: List<View>
    private lateinit var endActions: List<View>
    private lateinit var centerTitle: TextView
    private var marqueeWidth = -1
    private val visibilityListener = ViewTreeObserver.OnPreDrawListener {
        // INVISIBLE <-> VISIBLE can invalidate without requesting a new measurement.
        val startChanged = updateBarrier(startBarrier, R.id.titleStartInset, startActions)
        val endChanged = updateBarrier(endBarrier, R.id.titleEndInset, endActions)
        if (startChanged || endChanged) {
            requestLayout()
            false
        } else if (updateCenterTitleInsets()) {
            false
        } else {
            updateTitleMarquee()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnPreDrawListener(visibilityListener)
    }

    override fun onDetachedFromWindow() {
        centerTitle.isSelected = false
        marqueeWidth = -1
        viewTreeObserver.removeOnPreDrawListener(visibilityListener)
        super.onDetachedFromWindow()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        startBarrier = findViewById(R.id.titleStartBarrier)
        endBarrier = findViewById(R.id.titleEndBarrier)
        centerTitle = findViewById(R.id.tvCenter)
        startActions = listOf(
            R.id.btnActionBarLeft,
            R.id.btnActionBarLeftText,
            R.id.btnReset,
            R.id.cvLogo
        ).map { findViewById(it) }
        endActions = listOf(
            R.id.btnActionBarRight,
            R.id.btnActionBarNextRight,
            R.id.btnActionBarNextRight1,
            R.id.btnActionBarRightText,
            R.id.btnActionBarRightLang,
            R.id.btnShare
        ).map { findViewById(it) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Barrier ignores GONE optionally, but still counts INVISIBLE actions by default.
        updateBarrier(startBarrier, R.id.titleStartInset, startActions)
        updateBarrier(endBarrier, R.id.titleEndInset, endActions)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun updateBarrier(barrier: Barrier, insetId: Int, actions: List<View>): Boolean {
        val ids = intArrayOf(insetId) + actions
            .filter { it.visibility == View.VISIBLE }
            .map { it.id }
            .toIntArray()
        if (!barrier.referencedIds.contentEquals(ids)) {
            barrier.referencedIds = ids
            return true
        }
        return false
    }

    private fun updateCenterTitleInsets(): Boolean {
        if (width == 0 || centerTitle.visibility == View.GONE) return false
        val inset = maxOf(
            resources.getDimensionPixelSize(R.dimen.dp_20),
            minOf(startBarrier.left, endBarrier.left),
            width - maxOf(startBarrier.left, endBarrier.left)
        ).coerceAtMost(width / 2)
        val params = centerTitle.layoutParams as LayoutParams
        if (params.marginStart == inset && params.marginEnd == inset) return false
        // Symmetric margins keep the title midpoint fixed even with actions on only one side.
        params.marginStart = inset
        params.marginEnd = inset
        centerTitle.layoutParams = params
        return true
    }

    private fun updateTitleMarquee(): Boolean {
        if (centerTitle.visibility != View.VISIBLE) {
            centerTitle.isSelected = false
            marqueeWidth = -1
            return true
        }
        if (centerTitle.isLayoutRequested || centerTitle.layout == null) return true
        val availableWidth = centerTitle.width - centerTitle.compoundPaddingLeft -
            centerTitle.compoundPaddingRight
        if (availableWidth <= 0) return true
        val displayText = centerTitle.transformationMethod
            ?.getTransformation(centerTitle.text, centerTitle) ?: centerTitle.text
        val overflows = Layout.getDesiredWidth(displayText, centerTitle.paint) > availableWidth
        val ellipsize = if (overflows) TextUtils.TruncateAt.MARQUEE else TextUtils.TruncateAt.END

        if (centerTitle.ellipsize != ellipsize) {
            centerTitle.isSelected = false
            centerTitle.scrollTo(0, 0)
            centerTitle.ellipsize = ellipsize
            marqueeWidth = -1
            // Start marquee on the next traversal, once the new text layout is ready.
            return false
        }
        if (marqueeWidth != availableWidth || centerTitle.isSelected != overflows) {
            centerTitle.isSelected = false
            centerTitle.scrollTo(0, 0)
            centerTitle.isSelected = overflows
            marqueeWidth = availableWidth
        }
        return true
    }
}
