package com.character.ocmaker.creator.ui.my_creation

import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.utils.key.ValueKey
import com.character.ocmaker.creator.ui.my_creation.view_model.AlbumLoadState
import com.character.ocmaker.creator.ui.my_creation.view_model.MyAvatarViewModel
import com.character.ocmaker.creator.ui.my_creation.view_model.MyCreationViewModel
import com.character.ocmaker.creator.ui.my_creation.view_model.MyDesignViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyCreationTabsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun switchingTabsKeepsViewsScrollAndUsesLatestRequest() {
        // Observe readiness directly; continuous shimmer/marquee animations need not go idle.
        instrumentation.targetContext.startActivity(
            Intent(instrumentation.targetContext, MyCreationActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        var resumedActivity: MyCreationActivity? = null
        await {
            resumedActivity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED).filterIsInstance<MyCreationActivity>().firstOrNull()
            resumedActivity != null
        }
        val activity = resumedActivity!!
        var drawnFrames = 0
        onMain {
            activity.window.decorView.viewTreeObserver.addOnDrawListener { drawnFrames++ }
        }
        try {
            await {
                val avatar = activity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")
                val design = activity.supportFragmentManager.findFragmentByTag("MyDesignFragment")
                drawnFrames > 0 && avatar?.isVisible == true && design?.view != null &&
                    ViewModelProvider(design)[MyDesignViewModel::class.java].loadState.value != AlbumLoadState.Loading
            }
            lateinit var avatarView: View
            lateinit var designView: View
            lateinit var recycler: RecyclerView
            var position = 0
            var top = 0
            onMain {
                avatarView = activity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")!!.requireView()
                designView = activity.supportFragmentManager.findFragmentByTag("MyDesignFragment")!!.requireView()
                recycler = avatarView.findViewById(R.id.rcvMyAvatar)
                val target = minOf(8, (recycler.adapter!!.itemCount - 1).coerceAtLeast(0))
                (recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(target, 0)
            }
            await { recycler.isLaidOut && !recycler.isLayoutRequested }
            onMain {
                position = (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                top = recycler.getChildAt(0)?.top ?: 0
                activity.findViewById<View>(R.id.btnMyDesign).performClick()
            }
            await { activity.supportFragmentManager.findFragmentByTag("MyDesignFragment")!!.isVisible }
            onMain { activity.findViewById<View>(R.id.btnMyPixel).performClick() }
            await { activity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")!!.isVisible }
            await { recycler.isLaidOut && !recycler.isLayoutRequested }
            onMain {
                assertSame(avatarView, activity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")!!.view)
                assertSame(designView, activity.supportFragmentManager.findFragmentByTag("MyDesignFragment")!!.view)
                assertEquals(position, (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                assertEquals(top, recycler.getChildAt(0)?.top ?: 0)
                val vm = ViewModelProvider(activity)[MyCreationViewModel::class.java]
                vm.setTypeStatus(ValueKey.MY_DESIGN_TYPE)
                vm.setTypeStatus(ValueKey.AVATAR_TYPE)
                vm.setTypeStatus(ValueKey.MY_DESIGN_TYPE)
            }
            await { activity.supportFragmentManager.findFragmentByTag("MyDesignFragment")!!.isVisible }
            onMain {
                val avatar = activity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")!!
                val design = activity.supportFragmentManager.findFragmentByTag("MyDesignFragment")!!
                assertSame(avatarView, avatar.view)
                assertSame(designView, design.view)
                assertFalse(avatar.isVisible)
                val avatarState = ViewModelProvider(avatar)[MyAvatarViewModel::class.java].loadState.value
                val designState = ViewModelProvider(design)[MyDesignViewModel::class.java].loadState.value
                assertTrue(avatarState == AlbumLoadState.Content || avatarState == AlbumLoadState.Empty)
                assertTrue(designState == AlbumLoadState.Content || designState == AlbumLoadState.Empty)
                assertEquals(designState == AlbumLoadState.Empty,
                    designView.findViewById<View>(R.id.layoutNoItem).visibility == View.VISIBLE)
            }
        } finally {
            onMain { activity.finish() }
        }
    }

    private fun onMain(action: () -> Unit) = instrumentation.runOnMainSync(action)

    private fun await(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 15_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            onMain { ready = condition() }
            if (ready) return
            SystemClock.sleep(25)
        }
        fail("My Creation did not reach the expected tab state")
    }
}
