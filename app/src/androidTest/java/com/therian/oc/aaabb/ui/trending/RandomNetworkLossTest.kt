package com.character.ocmaker.creator.ui.trending

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.character.ocmaker.creator.core.helper.InternetHelper
import com.character.ocmaker.creator.data.model.custom.SuggestionModel
import com.character.ocmaker.creator.dialog.YesNoDialog
import com.character.ocmaker.creator.ui.random_character.RandomCharacterViewModel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RandomNetworkLossTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun offlineWhileLoadingClearsOldResultAndRejectsLateImage() {
        // This test temporarily changes connectivity only on an emulator.
        assumeTrue(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish")
        val context = instrumentation.targetContext
        assumeTrue(InternetHelper.isInternetAvailable(context))
        val wifiEnabled = shell("settings get global wifi_on").trim() == "1"
        val dataEnabled = shell("settings get global mobile_data").trim() == "1"
        val pending = AtomicReference<DataFetcher.DataCallback<in InputStream>>()
        val uri = "random-test://${System.nanoTime()}"
        Glide.get(context).registry.prepend(String::class.java, InputStream::class.java,
            object : ModelLoaderFactory<String, InputStream> {
                override fun build(multiFactory: MultiModelLoaderFactory) = object : ModelLoader<String, InputStream> {
                    override fun handles(model: String) = model == uri
                    override fun buildLoadData(model: String, width: Int, height: Int, options: Options) =
                        ModelLoader.LoadData(ObjectKey(model), object : DataFetcher<InputStream> {
                            override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
                                pending.set(callback)
                            }
                            override fun cleanup() = Unit
                            override fun cancel() = Unit // Deliberately allow a late callback after cancellation.
                            override fun getDataClass() = InputStream::class.java
                            override fun getDataSource() = DataSource.REMOTE
                        })
                }
                override fun teardown() = Unit
            })
        val monitor = ActivityLifecycleMonitorRegistry.getInstance()
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity is TrendingActivity && stage == Stage.PRE_ON_CREATE) {
                field("hasStartedDataInitialization").setBoolean(activity, true)
            }
        }
        onMain { monitor.addLifecycleCallback(callback) }
        var activity: TrendingActivity? = null
        try {
            context.startActivity(Intent(context, TrendingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            await {
                activity = monitor.getActivitiesInStage(Stage.RESUMED).filterIsInstance<TrendingActivity>().firstOrNull()
                activity != null
            }
            val screen = activity!!
            onMain {
                field("currentSuggestion").set(screen, SuggestionModel(avatarPath = "old-avatar"))
                screen.binding.imvImage.setImageBitmap(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888))
                screen.binding.btnEdit.isEnabled = true
                ViewModelProvider(screen)[RandomCharacterViewModel::class.java].randomList.apply {
                    clear()
                    add(SuggestionModel(pathSelectedList = arrayListOf(uri)))
                }
                TrendingActivity::class.java.getDeclaredMethod("handleGenerate").apply { isAccessible = true }.invoke(screen)
                assertNull(field("currentSuggestion").get(screen))
                assertNull(screen.binding.imvImage.drawable)
                assertFalse(screen.binding.btnEdit.isEnabled)
            }
            await { pending.get() != null }
            shell("input keyevent KEYCODE_HOME")
            await { !screen.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) }
            shell("svc wifi disable")
            shell("svc data disable")
            await { !InternetHelper.isInternetAvailable(context) && !field("isAnimating").getBoolean(screen) }
            context.startActivity(Intent(context, TrendingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            await { (field("noInternetDialog").get(screen) as? YesNoDialog)?.isShowing == true }
            onMain {
                assertNull(field("currentSuggestion").get(screen))
                assertNull(screen.binding.imvImage.drawable)
                assertFalse(screen.binding.btnEdit.isEnabled)
                assertTrue(screen.binding.btnGenerate.isEnabled)
            }
            val bytes = ByteArrayOutputStream().also { stream ->
                Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(Color.RED)
                    compress(Bitmap.CompressFormat.PNG, 100, stream)
                    recycle()
                }
            }.toByteArray()
            pending.get().onDataReady(ByteArrayInputStream(bytes))
            SystemClock.sleep(500)
            onMain {
                assertNull(field("currentSuggestion").get(screen))
                assertNull(screen.binding.imvImage.drawable)
                assertFalse(screen.binding.btnEdit.isEnabled)
            }
            shell("svc wifi ${if (wifiEnabled) "enable" else "disable"}")
            shell("svc data ${if (dataEnabled) "enable" else "disable"}")
            await { InternetHelper.isInternetAvailable(context) }
            val layer = File.createTempFile("random-network-test-", ".png", context.cacheDir)
            try {
                layer.writeBytes(bytes)
                val recovered = SuggestionModel(avatarPath = "new-avatar", pathSelectedList = arrayListOf(layer.path))
                onMain {
                    (field("noInternetDialog").get(screen) as YesNoDialog).dismiss()
                    ViewModelProvider(screen)[RandomCharacterViewModel::class.java].randomList.apply {
                        clear()
                        add(recovered)
                    }
                    TrendingActivity::class.java.getDeclaredMethod("handleGenerate").apply { isAccessible = true }.invoke(screen)
                }
                await { !field("isAnimating").getBoolean(screen) }
                onMain {
                    assertSame(recovered, field("currentSuggestion").get(screen))
                    assertNotNull(screen.binding.imvImage.drawable)
                    assertTrue(screen.binding.btnEdit.isEnabled)
                    assertTrue(recovered.pathInternalRandom.isNotEmpty())
                }
                File(recovered.pathInternalRandom).delete()
            } finally {
                layer.delete()
            }
        } finally {
            shell("svc wifi ${if (wifiEnabled) "enable" else "disable"}")
            shell("svc data ${if (dataEnabled) "enable" else "disable"}")
            onMain {
                monitor.removeLifecycleCallback(callback)
                activity?.let {
                    (field("noInternetDialog").get(it) as? YesNoDialog)?.dismiss()
                    it.finish()
                }
            }
        }
    }

    private fun field(name: String) = TrendingActivity::class.java.getDeclaredField(name).apply { isAccessible = true }
    private fun onMain(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command)
    ).bufferedReader().use { it.readText() }

    private fun await(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 15_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            onMain { ready = condition() }
            if (ready) return
            SystemClock.sleep(25)
        }
        fail("Random did not reach the expected network/loading state")
    }
}
