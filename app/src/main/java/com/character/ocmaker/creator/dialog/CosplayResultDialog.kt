package com.character.ocmaker.creator.dialog

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseDialog
import com.character.ocmaker.creator.core.extensions.hideNavigation
import com.character.ocmaker.creator.core.extensions.loadImage
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.databinding.DialogResultBinding

class CosplayResultDialog(
    private val context: Activity,
    private val playerBitmap: Bitmap,
    private val samplePath: String,
    private val progress: Int
) : BaseDialog<DialogResultBinding>(context, maxWidth = true, maxHeight = true) {

    override val layoutId: Int = R.layout.dialog_result
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNextClick: () -> Unit = {}

    override fun initView() {
        context.hideNavigation()
        binding.btnImage.setImageBitmap(playerBitmap)
        if (samplePath.isNotEmpty()) {
            loadImage(context, samplePath, binding.btnSamplePhoto)
        }
        updateProgressDisplay()
        binding.tvTitle.isSelected = true
    }

    private fun updateProgressDisplay() {
        val safeProgress = progress.coerceIn(0, 100)
        binding.tvRef.text = "$safeProgress%"

        binding.ctnProgress.post {
            val progressRatio = safeProgress / 100f
            val containerWidth = binding.ctnProgress.width.toFloat()
            val thumbWidth = binding.icThumb.width.toFloat()
            val thumbLeft = progressRatio * (containerWidth - thumbWidth).coerceAtLeast(0f)
            val revealRight = (thumbLeft + thumbWidth / 2f)
                .coerceIn(0f, containerWidth)
                .toInt()

            binding.icThumb.translationX = thumbLeft
            binding.sbProgressFull.clipBounds = Rect(
                0,
                0,
                revealRight,
                binding.sbProgressFull.height
            )
        }
    }

    override fun initAction() {
        binding.btnNext.tap { onNextClick() }
    }

    override fun onDismissListener() = Unit
}
