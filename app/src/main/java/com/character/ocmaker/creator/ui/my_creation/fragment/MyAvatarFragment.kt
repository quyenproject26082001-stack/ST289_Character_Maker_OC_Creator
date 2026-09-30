package com.character.ocmaker.creator.ui.my_creation.fragment

import android.app.ActivityOptions
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.first
import androidx.recyclerview.widget.RecyclerView
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseFragment
import com.character.ocmaker.creator.core.extensions.hideNavigation
import com.character.ocmaker.creator.core.extensions.select
import com.character.ocmaker.creator.core.extensions.showInterAll
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.core.helper.LanguageHelper
import com.character.ocmaker.creator.core.helper.MediaHelper
import com.character.ocmaker.creator.core.utils.key.IntentKey
import com.character.ocmaker.creator.core.utils.key.ValueKey
import com.character.ocmaker.creator.core.utils.state.HandleState
import com.character.ocmaker.creator.databinding.FragmentMyAvatarBinding
import com.character.ocmaker.creator.dialog.YesNoDialog
import com.character.ocmaker.creator.ui.customize.CustomizeCharacterActivity
import com.character.ocmaker.creator.ui.customize.CustomizeCharacterViewModel
import com.character.ocmaker.creator.ui.home.DataViewModel
import com.character.ocmaker.creator.ui.my_creation.MyCreationActivity
import com.character.ocmaker.creator.ui.my_creation.adapter.MyAvatarAdapter
import com.character.ocmaker.creator.ui.my_creation.view_model.MyAvatarViewModel
import com.character.ocmaker.creator.ui.view.ViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import com.character.ocmaker.creator.ui.my_creation.view_model.AlbumLoadState
import com.character.ocmaker.creator.data.model.MyAlbumModel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class MyAvatarFragment : BaseFragment<FragmentMyAvatarBinding>() {
    private val viewModel: MyAvatarViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val myAvatarAdapter by lazy { MyAvatarAdapter(requireActivity()) }

    private val myAlbumActivity: MyCreationActivity
        get() = requireActivity() as MyCreationActivity

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyAvatarBinding {
        return FragmentMyAvatarBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.layoutNoItem.isVisible = false
        binding.tvNoitem.select()
        initRcv()
        viewModel.ensureLoaded(myAlbumActivity)
        dataViewModel.ensureData(myAlbumActivity)
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.myAvatarList, viewModel.loadState) { list, state ->
                        list to state
                    }.collect { (list, state) ->
                        renderContent(list, state)
                        if (!isHidden) {
                            myAlbumActivity.refreshBottomVisibility()
                        }
                    }
                }
                // Removed - action bar buttons are disabled
                // launch {
                //     viewModel.isLastItem.collect { selectStatus ->
                //         myAlbumActivity.changeImageActionBarRight(selectStatus)
                //     }
                // }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            layoutNoItem.tap {
                if (viewModel.loadState.value == AlbumLoadState.Error) refreshData()
            }
            rcvMyAvatar.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView, motionEvent: MotionEvent
                ): Boolean {
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                        val touchPos = if (child != null) recyclerView.getChildAdapterPosition(child) else RecyclerView.NO_ID.toInt()
                        android.util.Log.d("TouchDebug", "--- ACTION_DOWN ---")
                        android.util.Log.d("TouchDebug", "  touch x=${motionEvent.x.toInt()} y=${motionEvent.y.toInt()}")
                        android.util.Log.d("TouchDebug", "  child view under touch: ${if (child != null) "found" else "null"}")
                        android.util.Log.d("TouchDebug", "  adapterPosition of touched child=$touchPos")
                        android.util.Log.d("TouchDebug", "  item at touchPos: ${myAvatarAdapter.items.getOrNull(touchPos)?.path?.substringAfterLast("/")}")
                    }
                    return when {
                        motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                            motionEvent.x, motionEvent.y
                        ) != null -> false

                        else -> {
                            resetSelectionMode()
                            true
                        }
                    }
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
            })
            // Action bar buttons disabled - using btnDeleteSelect instead
            // myAlbumActivity.binding.actionBar.btnActionBarRight.tap { handleSelectAll() }
            // myAlbumActivity.binding.actionBar.btnActionBarNextToRight.tap { handleDelete(viewModel.getPathSelected()) }

            // Share and Download buttons are handled in MyCreationActivity

            myAvatarAdapter.onItemClick = { pathInternal -> handleItemClick(pathInternal) }
            myAvatarAdapter.onItemTick = { position ->
                viewModel.toggleSelect(position)
                // Check if all items are now selected and update the icon
                val allSelected = viewModel.myAvatarList.value.all { it.isSelected }
                myAlbumActivity.updateSelectAllIcon(allSelected)
            }
            myAvatarAdapter.onEditClick = { pathInternal -> handleEditClick(pathInternal) }
            myAvatarAdapter.onDeleteClick = { pathInternal -> handleDelete(arrayListOf(pathInternal)) }
            myAvatarAdapter.onLongClick = { position -> handleLongClick(position) }
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvMyAvatar.apply {
                adapter = myAvatarAdapter
                itemAnimator = null
                setHasFixedSize(true)
                setItemViewCacheSize(4)
                isNestedScrollingEnabled = true
            }
        }
    }

    private fun handleDelete(pathInternalList: ArrayList<String>) {
        if (pathInternalList.isEmpty()) {
            myAlbumActivity.showToast(R.string.please_select_an_image)
            return
        }
        val dialog = YesNoDialog(myAlbumActivity, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        LanguageHelper.setLocale(myAlbumActivity)
        dialog.show()
        dialog.onDismissClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
            resetSelectionMode()
        }
        dialog.onNoClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
            // Exit selection mode when user cancels
            //resetData()
        }
        dialog.onYesClick = {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteItem(myAlbumActivity, pathInternalList)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    myAlbumActivity.hideNavigation()
                    resetSelectionMode()
                }
            }
        }
    }

    private fun handleEditClick(pathInternal: String) {
        lifecycleScope.launch {
            myAlbumActivity.showLoading()
            val availableCharacters = withTimeoutOrNull(12_000) {
                dataViewModel.allData.first { it.isNotEmpty() }
            } ?: arrayListOf()
            val canEdit = withContext(Dispatchers.IO) {
                viewModel.editItem(myAlbumActivity, pathInternal, availableCharacters)
            }
            myAlbumActivity.dismissLoading()
            if (!canEdit) {
                showCharacterUnavailableDialog()
                return@launch
            }
            viewModel.checkDataInternet(myAlbumActivity) {
                val intent = Intent(
                    myAlbumActivity,
                    CustomizeCharacterActivity::class.java
                ).apply {
                  //  addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(IntentKey.INTENT_KEY, viewModel.positionCharacter)
                    putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
                }
                val option = ActivityOptions.makeCustomAnimation(
                    myAlbumActivity, R.anim.slide_out_left, R.anim.slide_in_right
                )
                myAlbumActivity.showInterAll {
                    startActivity(intent, option.toBundle())
                }
            }
        }
    }

    private fun showCharacterUnavailableDialog() {
        val dialog = YesNoDialog(
            myAlbumActivity,
            R.string.error,
            R.string.character_no_longer_available,
            isError = true
        )
        LanguageHelper.setLocale(myAlbumActivity)
        dialog.show()
        dialog.onYesClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
        }
    }

    private fun handleItemClick(pathInternal: String) {
        val intent = Intent(myAlbumActivity, ViewActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(IntentKey.INTENT_KEY, pathInternal)
            putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
            putExtra(IntentKey.STATUS_KEY, ValueKey.AVATAR_TYPE)
        }
        val options = ActivityOptionsCompat.makeCustomAnimation(myAlbumActivity, R.anim.slide_in_right, R.anim.slide_out_left)
        myAlbumActivity.showInterAll { myAlbumActivity.launchViewActivity(intent, options) }
    }

    private fun handleLongClick(position: Int) {
        if (position >= viewModel.myAvatarList.value.size) return
        viewModel.showLongClick(position)
        binding.rcvMyAvatar.clipToPadding = true
        myAlbumActivity.enterSelectionMode()
        // Enable select mode margins in adapter
        myAvatarAdapter.isSelectMode = true

        // Check if all items are now selected (e.g., if there's only 1 item)
        val allSelected = viewModel.myAvatarList.value.all { it.isSelected }
        myAlbumActivity.updateSelectAllIcon(allSelected)

        val list = viewModel.myAvatarList.value
        android.util.Log.d("SelectState", "=== rcvAlbum (MyAvatar) longClick pos=$position | total=${list.size} ===")
        list.forEachIndexed { i, item ->
            android.util.Log.d("SelectState", "  [$i] isSelected=${item.isSelected} | isShowSelection=${item.isShowSelection} | path=${item.path}")
        }
    }

    fun refreshData() {
        viewModel.loadMyAvatar(myAlbumActivity)
    }

    fun deleteSelectedItems() {
        handleDelete(viewModel.getPathSelected())
    }

    fun getSelectedPaths(): ArrayList<String> {
        return viewModel.getPathSelected()
    }

    fun selectAllItems() {
        viewModel.selectAll(true)
        // Use notifyItemRangeChanged instead of notifyDataSetChanged for better performance
        myAvatarAdapter.notifyItemRangeChanged(0, myAvatarAdapter.itemCount)
    }

    fun deselectAllItems() {
        viewModel.selectAll(false)
        // Use notifyItemRangeChanged instead of notifyDataSetChanged for better performance
        myAvatarAdapter.notifyItemRangeChanged(0, myAvatarAdapter.itemCount)
    }

    fun exitSelectMode() {
        myAvatarAdapter.isSelectMode = false
    }

    fun resetSelectionMode() {
        viewModel.clearSelection()
        binding.rcvMyAvatar.clipToPadding = false
        myAlbumActivity.exitSelectionMode()
        myAvatarAdapter.isSelectMode = false
        // No need to notify here - isSelectMode setter already handles it
    }

    fun hasDisplayItems(): Boolean = viewModel.myAvatarList.value.isNotEmpty()

    suspend fun prepareForDisplay() {
        viewModel.prepareMyAvatar(myAlbumActivity)
        renderContent(viewModel.myAvatarList.value, viewModel.loadState.value)
    }

    private fun renderContent(list: List<MyAlbumModel>, state: AlbumLoadState) {
        if (myAvatarAdapter.items != list) myAvatarAdapter.submitList(list)
        binding.layoutNoItem.isVisible = list.isEmpty() &&
            (state == AlbumLoadState.Empty || state == AlbumLoadState.Error)
        binding.tvNoitem.text = if (state == AlbumLoadState.Error) {
            getString(R.string.an_error_occurred) + " " + getString(R.string.try_again)
        } else {
            getString(R.string.no_item_here)
        }
    }

    fun getAllPaths(): ArrayList<String> {
        return viewModel.myAvatarList.value
            .filterNot { it.isFake }
            .map { it.path }
            .toCollection(ArrayList())
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            myAlbumActivity.refreshBottomVisibility()
        }
    }

}
