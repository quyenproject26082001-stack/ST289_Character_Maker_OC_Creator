package com.character.ocmaker.creator.ui.cosplay

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseActivity
import com.character.ocmaker.creator.core.extensions.handleBackLeftToRight
import com.character.ocmaker.creator.core.extensions.loadImage
import com.character.ocmaker.creator.core.extensions.showInterAll
import com.character.ocmaker.creator.core.extensions.startIntentWithClearTop
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.core.extensions.visible
import com.character.ocmaker.creator.core.utils.key.IntentKey
import com.character.ocmaker.creator.databinding.ActivityCosplaySuccessfulBinding
import com.character.ocmaker.creator.ui.home.HomeActivity
import com.character.ocmaker.creator.ui.success.SuccessViewModel
import kotlinx.coroutines.launch

class CosplaySuccessfulActivity : BaseActivity<ActivityCosplaySuccessfulBinding>() {
    private var characterPosition = 0

    private val viewModel: SuccessViewModel by viewModels()

    private var finalProgress = 0

    override fun setViewBinding(): ActivityCosplaySuccessfulBinding {
        return ActivityCosplaySuccessfulBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val path = intent.getStringExtra(IntentKey.COSPLAY_RESULT_PATH_KEY) ?: ""
        finalProgress = intent.getIntExtra(IntentKey.COSPLAY_PROGRESS_KEY, 0)
        characterPosition = intent.getIntExtra(
            IntentKey.COSPLAY_CHARACTER_POSITION_KEY,
            0
        )

        binding.includeLayoutBottom.tvShare.isSelected = true
        viewModel.setPath(path)
        updateProgressDisplay()
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pathInternal.collect { path ->
                    if (path.isNotEmpty()) {
                        loadImage(this@CosplaySuccessfulActivity, path, binding.imvImage)
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRight.tap {
                showInterAll {
                    startIntentWithClearTop(HomeActivity::class.java)
                }

            }
            actionBar.btnActionBarLeft.tap {
                handleBackLeftToRight()
            }
            includeLayoutBottom.btnTryAgain.tap(800) {
                showInterAll {
                    val intent = Intent(
                        this@CosplaySuccessfulActivity,
                        CosplayCustomizeActivity::class.java
                    ).apply {
                        putExtra(
                            IntentKey.COSPLAY_CHARACTER_POSITION_KEY,
                            characterPosition
                        )
                    }

                    startActivity(intent)
                    finish()
                }
            }


        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarRight.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_home)
            tvCenter.visible()
            tvCenter.setText(R.string.cosplay)
        }
    }

    private fun updateProgressDisplay() {
        val progress = finalProgress.coerceIn(0, 100)
        binding.tvProgress.text = "$progress/100"

        binding.progressContainer.post {
            val containerWidth = binding.progressContainer.width.toFloat()
            val revealRight = (containerWidth * progress / 100f)
                .coerceIn(0f, containerWidth)
                .toInt()

            binding.progressBar.clipBounds = Rect(
                0,
                0,
                revealRight,
                binding.progressBar.height
            )

            val thumbWidth = binding.icThumb.width.toFloat()
            val thumbCenter = revealRight.toFloat().coerceIn(
                thumbWidth / 2f,
                containerWidth - thumbWidth / 2f
            )
            binding.icThumb.translationX = thumbCenter - containerWidth / 2f
        }

        val starRes = when {
            progress == 0 -> R.drawable.zero_star_ss
            progress < 20 -> R.drawable.one_star_ss
            progress < 40 -> R.drawable.two_star_ss
            progress < 60 -> R.drawable.three_star_ss
            progress < 80 -> R.drawable.four_star_ss
            else -> R.drawable.five_star_ss
        }
        binding.starSS.setImageResource(starRes)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }
}
