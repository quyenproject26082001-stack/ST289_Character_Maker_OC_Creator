package com.character.ocmaker.creator.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.util.Admob
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseActivity
import com.character.ocmaker.creator.core.extensions.checkInternet
import com.character.ocmaker.creator.core.extensions.rateApp
import com.character.ocmaker.creator.core.extensions.showInterAll
import com.character.ocmaker.creator.core.extensions.startIntentRightToLeft
import com.character.ocmaker.creator.core.helper.LanguageHelper
import com.character.ocmaker.creator.core.utils.state.RateState
import com.character.ocmaker.creator.databinding.ActivityHomeBinding
import com.character.ocmaker.creator.ui.SettingsActivity
import com.character.ocmaker.creator.ui.my_creation.MyCreationActivity
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.core.extensions.strings
import com.character.ocmaker.creator.ui.cosplay.CosplayRandomActivity
import com.character.ocmaker.creator.ui.trending.TrendingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        sharePreference.setCountBack(sharePreference.getCountBack() + 1)
        binding.tv1.isSelected = true
        //  binding.tv3.isSelected = true
        binding.tv2.isSelected = true
        binding.tv3.isSelected = true
        binding.tv4.isSelected = true
        binding.btnSettings.setImageResource(R.drawable.ic_setting)

        // Apply elastic bounce animation to app name
        val elasticBounce = AnimationUtils.loadAnimation(this, R.anim.elastic_bounce)
        // binding.imvAppName.startAnimation(elasticBounce)
    }

    override fun viewListener() {
        binding.apply {
            btnSettings.tap(800) { startIntentRightToLeft(SettingsActivity::class.java) }
            btnRandom.tap(800) { checkInternet { startIntentRightToLeft(TrendingActivity::class.java) } }
            btnMyCreation.tap(800) { showInterAll { startIntentRightToLeft(MyCreationActivity::class.java) } }
            btnCosPlay.tap(800) { checkInternet { startIntentRightToLeft(CosplayRandomActivity::class.java) } }
            btnPlay.tap(800) { checkInternet { startIntentRightToLeft(com.character.ocmaker.creator.ui.choose_character.ChooseCharacterActivity::class.java) } }

        }
    }

    override fun initText() {
        super.initText()
        //binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
//        binding.actionBar.apply {
//            setImageActionBar(btnActionBarRight, R.drawable.ic_settings)
//        }
    }

    // Enable background music for HomeActivity
    override fun shouldPlayBackgroundMusic(): Boolean = false

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 == 0) {
            rateApp(sharePreference) { state ->
                if (state != RateState.CANCEL) {
                    showToast(R.string.have_rated)
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        exitProcess(0)
                    }
                }
            }
        } else {
            exitProcess(0)
        }
    }

    private fun updateText() {
        binding.apply {
            tv1.text = strings(R.string.character_maker_)
            tv2.text = strings(R.string.challenge_character)
            tv3.text = strings(R.string.character_random)
            tv4.text = strings(R.string.my_creation)
        }
    }

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        updateText()
//        initNativeCollab()
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            startStaggeredAnimations()
//        }
//    }

    private fun startStaggeredAnimations() {
        // Card 1: Slide from right (no delay)
        val slideFromRight1 = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_home)
        binding.btnCosPlay.startAnimation(slideFromRight1)
        binding.tv1.startAnimation(slideFromRight1)


        // Card 2: Slide from left (200ms delay)
        val slideFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left_home)
        binding.btnRandom.postDelayed({
            binding.btnRandom.startAnimation(slideFromLeft)
            binding.tv2.startAnimation(slideFromLeft)
        }, 200)

        // Card 3: Slide from right (400ms delay)
        val slideFromRight2 = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_home)
        binding.btnMyCreation.postDelayed({
            binding.btnMyCreation.startAnimation(slideFromRight2)
            binding.tv3.startAnimation(slideFromRight2)
        }, 400)
    }

    fun initNativeCollab() {
//        Admob.getInstance().loadNativeCollapNotBanner(
//            this,
//            getString(R.string.native_cl_home),
//            binding.flNativeCollab
//        )
    }

    override fun initAds() {
//        initNativeCollab()
        Admob.getInstance().loadInterAll(this, getString(R.string.inter_all))
//        Admob.getInstance().loadNativeAll(this, getString(R.string.native_all))
    }
}
