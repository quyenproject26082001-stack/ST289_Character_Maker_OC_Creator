package com.character.ocmaker.creator.ui.my_creation

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.room.util.findColumnIndexBySuffix
import com.lvt.ads.util.Admob
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseActivity
import com.character.ocmaker.creator.core.extensions.checkPermissions
import com.character.ocmaker.creator.core.extensions.goToSettings
import com.character.ocmaker.creator.core.extensions.gone
import com.character.ocmaker.creator.core.extensions.hideNavigation
import com.character.ocmaker.creator.core.extensions.invisible
import com.character.ocmaker.creator.core.extensions.loadNativeCollabAds
import com.character.ocmaker.creator.core.extensions.requestPermission
import com.character.ocmaker.creator.core.extensions.select
import com.character.ocmaker.creator.core.extensions.setImageActionBar
import com.character.ocmaker.creator.core.extensions.setTextActionBar
import com.character.ocmaker.creator.core.extensions.tap

import com.character.ocmaker.creator.core.extensions.startIntentWithClearTop
import com.character.ocmaker.creator.core.extensions.visible
import com.character.ocmaker.creator.core.helper.LanguageHelper
import com.character.ocmaker.creator.core.helper.UnitHelper
import com.character.ocmaker.creator.core.utils.key.IntentKey
import com.character.ocmaker.creator.core.utils.key.RequestKey
import com.character.ocmaker.creator.core.utils.key.ValueKey
import com.character.ocmaker.creator.core.utils.share.whatsapp.WhatsappSharingActivity
import com.character.ocmaker.creator.core.utils.state.HandleState
import com.character.ocmaker.creator.databinding.ActivityAlbumBinding
import com.character.ocmaker.creator.dialog.YesNoDialog
import com.character.ocmaker.creator.ui.home.HomeActivity
import com.character.ocmaker.creator.ui.view.ViewActivity
import com.character.ocmaker.creator.dialog.CreateNameDialog
import com.character.ocmaker.creator.ui.my_creation.adapter.MyAvatarAdapter
import com.character.ocmaker.creator.ui.my_creation.adapter.TypeAdapter
import com.character.ocmaker.creator.ui.my_creation.fragment.MyAvatarFragment
import com.character.ocmaker.creator.ui.my_creation.fragment.MyDesignFragment
import com.character.ocmaker.creator.ui.my_creation.view_model.MyAvatarViewModel
import com.character.ocmaker.creator.ui.my_creation.view_model.MyCreationViewModel
import com.character.ocmaker.creator.ui.permission.PermissionViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlin.text.replace

class MyCreationActivity : WhatsappSharingActivity<ActivityAlbumBinding>() {
    private val viewModel: MyCreationViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private val viewActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")
        when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.resetSelectionMode()
            designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.resetSelectionMode()
        }
        exitSelectionMode()
    }

    fun launchViewActivity(intent: Intent, options: ActivityOptionsCompat? = null) {
        viewActivityLauncher.launch(intent, options)
    }

    private var myAvatarFragment: MyAvatarFragment? = null
    private var myDesignFragment: MyDesignFragment? = null
    private var displayedTab = -1
    private var isInSelectionMode = false
    private var isAllSelected = false
    private var pendingDownloadList: ArrayList<String>? = null

    override fun setViewBinding(): ActivityAlbumBinding {
        return ActivityAlbumBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val initialTab = intent.getIntExtra(IntentKey.TAB_KEY, ValueKey.AVATAR_TYPE)
        viewModel.setTypeStatus(initialTab)
        viewModel.setStatusFrom(intent.getBooleanExtra(IntentKey.FROM_SAVE, false))

        // Hide action bar buttons by default (only show in selection mode)
        binding.actionBar.apply {
            btnActionBarNextRight.gone()
            btnActionBarRight.gone()
            tvCenter.visible()
            tvCenter.setText(R.string.my_creation)
        }
        binding.flBottomView.gone()
        binding.lnlBottom.gone()
        binding.lnlBottom.isSelected = true
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            updateListBottomInsets()
        }

    }

    override fun dataObservable() {
        binding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            viewModel.typeStatus.collectLatest { type ->
                                if (type != -1) {
                                    showFragment(type)
                                    if (displayedTab != type) return@collectLatest
                                    when (type) {
                                        ValueKey.AVATAR_TYPE -> {
                                            imvTabBackground.setImageResource(R.drawable.bg_avatar_slt)
                                            setupSelectedTab(tvAvatar)
                                            setupUnselectedTab(tvMyDesign)
                                        }

                                        ValueKey.MY_DESIGN_TYPE -> {
                                            imvTabBackground.setImageResource(R.drawable.bg_design_slt)
                                            setupUnselectedTab(tvAvatar)
                                            setupSelectedTab(tvMyDesign)
                                        }
                                    }
                                    updateBottomButtonsVisibility()
                                    refreshBottomVisibility()
                                }
                            }
                        }
                    }
                    launch {
                        viewModel.downloadState.collect { state ->
                            when (state) {
                                HandleState.LOADING -> {
                                    showLoading()
                                }

                                HandleState.SUCCESS -> {
                                    dismissLoading()
                                    hideNavigation()
                                    showToast(R.string.download_success)
                                    if (isInSelectionMode) {
                                        binding.actionBar.btnActionBarLeft.performClick()
                                    }
                                }

                                else -> {
                                    dismissLoading()
                                    hideNavigation()
                                    showToast(R.string.download_failed_please_try_again_later)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap {
                    if (isInSelectionMode) {
                        val avatarFragment =
                            supportFragmentManager.findFragmentByTag("MyAvatarFragment")
                        val designFragment =
                            supportFragmentManager.findFragmentByTag("MyDesignFragment")
                        when {
                            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.resetSelectionMode()
                            designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.resetSelectionMode()
                        }
                    } else {
                        startIntentWithClearTop(HomeActivity::class.java)
                    }
                }

                // Delete button
                btnActionBarNextRight.tap {
                    handleDeleteSelectedFromCurrentFragment()
                }

                // Select All button
                btnActionBarRight.tap {
                    handleSelectAllFromCurrentFragment()
                }
            }

            btnMyPixel.setOnClickListener { viewModel.setTypeStatus(ValueKey.AVATAR_TYPE) }
            btnMyDesign.setOnClickListener { viewModel.setTypeStatus(ValueKey.MY_DESIGN_TYPE) }

            // WhatsApp, Telegram buttons in lnlBottom
            val layoutBottom = lnlBottom.getChildAt(0)
            layoutBottom.findViewById<View>(R.id.btnRight)?.tap(800) {
                val paths = getAllPathsFromCurrentFragment()
                val paths1 = getSelectedPathsFromCurrentFragment()

                handleAddToWhatsApp(if (!isInSelectionMode) paths else{paths1})
            }
            layoutBottom.findViewById<View>(R.id.btnLeft)?.tap(800) {
                val paths = getAllPathsFromCurrentFragment()
                val paths1 = getSelectedPathsFromCurrentFragment()
                handleAddToTelegram(if (!isInSelectionMode) paths else{paths1})
            }

            // Share/Download buttons in flBottomView (select mode)
            bottomView.btnWhatsapp.tap(2000) {
                val paths = getSelectedPathsFromCurrentFragment()
                handleShare(paths)
            }
            bottomView.btnTelegram.tap(2000) {
                val paths = getSelectedPathsFromCurrentFragment()
                if (paths.isEmpty()) {
                    showToast(R.string.please_select_an_image); return@tap
                }
                checkStoragePermissionForDownload(paths)
            }
        }
    }

    private fun handleShareFromCurrentFragment() {
        val selectedPaths = getSelectedPathsFromCurrentFragment()
        handleShare(selectedPaths)
    }

    private fun handleDownloadFromCurrentFragment() {
        val selectedPaths = getSelectedPathsFromCurrentFragment()
        if (selectedPaths.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }
        checkStoragePermissionForDownload(selectedPaths)
    }

    private fun checkStoragePermissionForDownload(list: ArrayList<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ không cần quyền WRITE_EXTERNAL_STORAGE
            handleDownload(list)
        } else {
            // Android 8-9 cần check quyền
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload(list)
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                // Lưu lại list để download sau khi được cấp quyền
                pendingDownloadList = list
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleSelectAllFromCurrentFragment() {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

        val doSelect: (() -> Unit)
        val doDeselect: (() -> Unit)

        when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> {
                doSelect = { avatarFragment.selectAllItems() }
                doDeselect = { avatarFragment.deselectAllItems() }
            }

            designFragment is MyDesignFragment && designFragment.isVisible -> {
                doSelect = { designFragment.selectAllItems() }
                doDeselect = { designFragment.deselectAllItems() }
            }

            else -> return
        }

        if (isAllSelected) {
            doDeselect()
            isAllSelected = false
            binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
        } else {
            doSelect()
            isAllSelected = true
            binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_select_all)
        }
    }

    private fun handleDeleteSelectedFromCurrentFragment() {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

        when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.deleteSelectedItems()
            designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.deleteSelectedItems()
        }
    }

    private fun getAllPathsFromCurrentFragment(): ArrayList<String> {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")
        return when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.getAllPaths()
            designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.getAllPaths()
            else -> arrayListOf()
        }
    }

    private fun getSelectedPathsFromCurrentFragment(): ArrayList<String> {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

        return when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.getSelectedPaths()
            designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.getSelectedPaths()
            else -> arrayListOf()
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {

            btnActionBarLeft.visible()

            btnActionBarNextRight.gone()

            // Delete All button - hidden initially, only shown in selection mode
            btnActionBarRight.setImageResource(R.drawable.ic_delete_creation)
            btnActionBarRight.layoutParams = btnActionBarRight.layoutParams.apply {
                width = resources.getDimensionPixelSize(R.dimen.dp_40)
                height = resources.getDimensionPixelSize(R.dimen.dp_40)
            }
            btnActionBarRight.translationX = -resources.getDimension(R.dimen.dp_0)
            btnActionBarNextRight.translationX = resources.getDimension(R.dimen.dp_0)
            btnActionBarRight.invisible()
        }
    }

    override fun initText() {
        binding.apply {
            tvAvatar.select()
            tvMyDesign.select()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                showToast(R.string.granted_storage)
                // Thực hiện download sau khi được cấp quyền
                pendingDownloadList?.let { list ->
                    handleDownload(list)
                    pendingDownloadList = null
                }
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
                pendingDownloadList = null
            }
        }
    }

    fun handleShare(list: ArrayList<String>) {
        if (list.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }
        viewModel.shareImages(this, list)
    }

    fun handleAddToTelegram(list: ArrayList<String>) {
        if (list.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }
        viewModel.addToTelegram(this, list)
        if (isInSelectionMode) {
            binding.actionBar.btnActionBarLeft.performClick()
        }
    }

    fun handleAddToWhatsApp(list: ArrayList<String>) {
        if (list.size < 3) {
            showToast(R.string.limit_3_items)
            return
        }
        if (list.size > 30) {
            showToast(R.string.limit_30_items)
            return
        }

        val dialog = CreateNameDialog(this)
        LanguageHelper.setLocale(this)
        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onNoClick = {
            dismissDialog()
        }
        dialog.onYesClick = { packageName ->
            dismissDialog()
            viewModel.addToWhatsapp(this, packageName, list) { stickerPack ->
                if (stickerPack != null) {
                    addToWhatsapp(stickerPack)
                    if (isInSelectionMode) {
                        binding.actionBar.btnActionBarLeft.performClick()
                    }
                }
            }
        }
    }

    private fun handleDownload(list: ArrayList<String>) {
        viewModel.downloadFiles(this, list)
    }

    private fun ensureTabsCreated() {
        if (myAvatarFragment?.isAdded == true && myDesignFragment?.isAdded == true) return
        myAvatarFragment = myAvatarFragment
            ?: supportFragmentManager.findFragmentByTag("MyAvatarFragment") as? MyAvatarFragment
            ?: MyAvatarFragment()
        myDesignFragment = myDesignFragment
            ?: supportFragmentManager.findFragmentByTag("MyDesignFragment") as? MyDesignFragment
            ?: MyDesignFragment()

        // Both tabs start loading independently and keep their views while hidden.
        supportFragmentManager.beginTransaction().setReorderingAllowed(true).apply {
            listOf(myAvatarFragment!! to "MyAvatarFragment", myDesignFragment!! to "MyDesignFragment")
                .forEach { (fragment, tag) ->
                    if (!fragment.isAdded) add(R.id.frmList, fragment, tag)
                    hide(fragment)
                    setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }
        }.commitNow()
    }

    private suspend fun showFragment(type: Int) {
        if (displayedTab == type || supportFragmentManager.isStateSaved) return
        ensureTabsCreated()
        val target: Fragment = when (type) {
            ValueKey.AVATAR_TYPE -> myAvatarFragment ?: return
            ValueKey.MY_DESIGN_TYPE -> myDesignFragment ?: return
            else -> return
        }

        // Only wait when the destination is still loading; cached tabs switch immediately.
        when (target) {
            is MyAvatarFragment -> target.prepareForDisplay()
            is MyDesignFragment -> target.prepareForDisplay()
        }
        if (supportFragmentManager.isStateSaved) return

        if (isInSelectionMode) {
            when (displayedTab) {
                ValueKey.AVATAR_TYPE -> myAvatarFragment?.resetSelectionMode()
                ValueKey.MY_DESIGN_TYPE -> myDesignFragment?.resetSelectionMode()
            }
        }
        displayedTab = type
        supportFragmentManager.beginTransaction().setReorderingAllowed(true).apply {
            listOfNotNull(myAvatarFragment, myDesignFragment).filter { it != target }.forEach {
                hide(it)
                setMaxLifecycle(it, Lifecycle.State.STARTED)
            }
            show(target)
            setMaxLifecycle(target, Lifecycle.State.RESUMED)
        }.commitNow()
        exitSelectionMode()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        startIntentWithClearTop(HomeActivity::class.java)
    }

    fun initNativeCollab() {
//        Admob.getInstance().loadNativeCollapNotBanner(this,getString(R.string.native_cl_creation), binding.flNativeCollab)
    }

    override fun initAds() {
//        initNativeCollab()
//        Admob.getInstance().loadNativeAd(
//            this,
//            getString(R.string.native_creation),
//            binding.nativeAds,
//            R.layout.ads_native_banner
//        )
    }

    override fun onRestart() {
        super.onRestart()
        // Returning from an editor/viewer may have changed either album.
        myAvatarFragment?.refreshData()
        myDesignFragment?.refreshData()
        android.util.Log.w(
            "MyCreationActivity",
            "🔄 onRestart() called - Activity restarting after being stopped"
        )
        android.util.Log.w(
            "MyCreationActivity",
            "Current tab: ${
                when (viewModel.typeStatus.value) {
                    ValueKey.AVATAR_TYPE -> "MyAvatar"; else -> "MyDesign"
                }
            }"
        )
        android.util.Log.w("MyCreationActivity", "Selection mode: $isInSelectionMode")

        // Check permission status
        val hasPermission =
            checkPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        android.util.Log.w("MyCreationActivity", "📱 Storage permission: $hasPermission")

        // Exit selection mode when returning from another activity
        if (isInSelectionMode) {
            val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
            val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

            when {
                avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.resetSelectionMode()
                designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.resetSelectionMode()
            }
            exitSelectionMode()
        }

//        initNativeCollab()
        android.util.Log.w("MyCreationActivity", "🔄 onRestart() END")
    }

    override fun onStart() {
        super.onStart()
        android.util.Log.w("MyCreationActivity", "🔵 onStart() called - Activity becoming visible")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.w("MyCreationActivity", "🟢 onResume() called - Activity in foreground")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.w("MyCreationActivity", "🟡 onPause() called - Activity losing focus")
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.w("MyCreationActivity", "🔴 onStop() called - Activity no longer visible")
    }

    fun enterSelectionMode() {
        isInSelectionMode = true
        isAllSelected = false
        binding.actionBar.apply {
            btnActionBarNextRight.visible()
            btnActionBarNextRight.setImageResource(R.drawable.ic_delete_creation)
            btnActionBarRight.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
            btnActionBarNextRight1.gone()
        }
        refreshBottomVisibility()
    }

    fun exitSelectionMode() {
        isInSelectionMode = false
        isAllSelected = false
        binding.actionBar.apply {
            btnActionBarNextRight.gone()
            btnActionBarRight.gone()
        }
        refreshBottomVisibility()
    }

    fun refreshBottomVisibility() {
        val hasDisplayItems = when (displayedTab) {
            ValueKey.AVATAR_TYPE -> myAvatarFragment
                ?.takeIf { it.isAdded }
                ?.hasDisplayItems() ?: false
            ValueKey.MY_DESIGN_TYPE -> myDesignFragment
                ?.takeIf { it.isAdded }
                ?.hasDisplayItems() ?: false
            else -> false
        }

        if (isInSelectionMode && hasDisplayItems) {
            binding.flBottomView.visible()
        } else {
            binding.flBottomView.gone()
        }

        when {
            !hasDisplayItems -> binding.lnlBottom.gone()
            displayedTab == ValueKey.AVATAR_TYPE -> {
                binding.lnlBottom.visible()
                binding.lnlBottom.translationY = if (isInSelectionMode) {
                    resources.getDimension(R.dimen.dp_15)
                } else {
                    0f
                }
            }
            isInSelectionMode -> binding.lnlBottom.gone()
            else -> {
                binding.lnlBottom.invisible()
                binding.lnlBottom.translationY = 0f
            }
        }
        binding.root.doOnPreDraw { updateListBottomInsets() }
    }

    private fun updateListBottomInsets() {
        val location = IntArray(2)
        val bottomTop = listOf(binding.lnlBottom, binding.flBottomView)
            .filter { it.visibility == View.VISIBLE && it.height > 0 }
            .minOfOrNull { bottom ->
                bottom.getLocationInWindow(location)
                location[1]
            }
        val gap = resources.getDimensionPixelSize(R.dimen.dp_12)
        val lists = listOfNotNull(
            myAvatarFragment?.takeIf { it.isVisible }?.view?.findViewById<RecyclerView>(R.id.rcvMyAvatar),
            myDesignFragment?.takeIf { it.isVisible }?.view?.findViewById<RecyclerView>(R.id.rcvMyDesign)
        )
        lists.forEach { recyclerView ->
            if (recyclerView.height == 0) return@forEach
            val parent = recyclerView.parent as? ViewGroup ?: return@forEach
            val params = recyclerView.layoutParams as? ViewGroup.MarginLayoutParams
                ?: return@forEach
            // Measure the available bottom before applying a margin, so repeated
            // layout passes cannot count the margin twice.
            parent.getLocationInWindow(location)
            val availableBottom = location[1] + parent.height - parent.paddingBottom
            val overlap = bottomTop?.let {
                (availableBottom - it).coerceAtLeast(0)
            } ?: 0
            val hasBottomOverlap = overlap > 0
            val marginBottom = if (!isInSelectionMode && hasBottomOverlap) overlap + gap else 0
            val paddingBottom = when {
                isInSelectionMode -> overlap + gap
                hasBottomOverlap -> 0
                else -> gap
            }
            if (params.bottomMargin != marginBottom) {
                recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = marginBottom
                }
            }
            if (recyclerView.paddingBottom != paddingBottom) {
                recyclerView.setPadding(
                    recyclerView.paddingLeft,
                    recyclerView.paddingTop,
                    recyclerView.paddingRight,
                    paddingBottom
                )
            }
        }
    }

    private fun updateBottomButtonsVisibility() {
        val layoutBottom = binding.lnlBottom.getChildAt(0)
        val btnRight = layoutBottom.findViewById<View>(R.id.btnRight)
        val btnLeft = layoutBottom.findViewById<View>(R.id.btnLeft)
        val imgBgBtnLeft = layoutBottom.findViewById<android.widget.ImageView>(R.id.imgBgBtnLeft)
        val imgBgBtnRight = layoutBottom.findViewById<android.widget.ImageView>(R.id.imgBgBtnRight)
        val tvLeft = layoutBottom.findViewById<android.widget.TextView>(R.id.tvTelegram)
        val tvRight = layoutBottom.findViewById<android.widget.TextView>(R.id.tvWhatsapp)

        // Bottom buttons always visible, content depends on current tab
        btnLeft?.visible()
        btnRight?.visible()

        if (viewModel.typeStatus.value == ValueKey.AVATAR_TYPE) {
            imgBgBtnLeft?.setImageResource(R.drawable.bg_btn_telegram_shape)
            imgBgBtnRight?.setImageResource(R.drawable.bg_btn_whatsapp_shape)
            tvLeft?.setText(R.string.add_to_telegram)
            tvRight?.setText(R.string.add_to_whatsapp)

        } else {
            imgBgBtnLeft?.setImageResource(R.drawable.bg_btn_layout_bottom)
            imgBgBtnRight?.setImageResource(R.drawable.bg_btn_layout_bottom)
            tvLeft?.setText(R.string.share)
            tvRight?.setText(R.string.download)

        }

        // ActionBar download/share buttons: only in selection mode (avatar tab only)
        binding.actionBar.apply {
            if (isInSelectionMode && viewModel.typeStatus.value == ValueKey.AVATAR_TYPE) {
                btnActionBarNextRight.setImageResource(R.drawable.ic_download_actionbar)
                btnActionBarNextRight1.setImageResource(R.drawable.ic_share_actionbar)
                btnActionBarNextRight.invisible()
                btnActionBarNextRight1.invisible()
            } else {
                btnActionBarNextRight.setImageResource(R.drawable.ic_download_actionbar)
                btnActionBarNextRight1.setImageResource(R.drawable.ic_share_actionbar)
                btnActionBarNextRight.invisible()
                btnActionBarNextRight1.invisible()
            }
        }
    }

    private fun setupSelectedTab(textView: com.character.ocmaker.creator.core.custom.text.OuterStrokeTextView) {
        textView.setTextColor(Color.parseColor("#FFFFFF"))
        //textView.setShadowLayer(2f, 0f, 2f, Color.WHITE)
        //textView.setupSelectedTab()
    }

    private fun setupUnselectedTab(textView: com.character.ocmaker.creator.core.custom.text.OuterStrokeTextView) {
        textView.setTextColor(Color.parseColor("#FFFFFF"))
        //textView.setupUnselectedTab()
    }

    // Public method to update select all icon based on selection state
    fun updateSelectAllIcon(allSelected: Boolean) {
        isAllSelected = allSelected
        if (allSelected) {
            binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_select_all)
        } else {
            binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
        }
    }
}
