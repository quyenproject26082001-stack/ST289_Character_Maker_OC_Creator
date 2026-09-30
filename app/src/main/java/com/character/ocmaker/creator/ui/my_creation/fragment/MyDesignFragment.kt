package com.character.ocmaker.creator.ui.my_creation.fragment

import android.app.ActivityOptions
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseFragment
import com.character.ocmaker.creator.core.extensions.hideNavigation
import com.character.ocmaker.creator.core.extensions.select
import com.character.ocmaker.creator.core.extensions.showInterAll
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.core.extensions.visible
import com.character.ocmaker.creator.core.helper.LanguageHelper
import com.character.ocmaker.creator.core.utils.key.IntentKey
import com.character.ocmaker.creator.core.utils.key.ValueKey
import com.character.ocmaker.creator.core.utils.state.HandleState
import com.character.ocmaker.creator.databinding.FragmentMyDesignBinding
import com.character.ocmaker.creator.dialog.YesNoDialog
import com.character.ocmaker.creator.ui.customize.CustomizeCharacterActivity
import com.character.ocmaker.creator.ui.home.DataViewModel
import com.character.ocmaker.creator.ui.my_creation.MyCreationActivity
import com.character.ocmaker.creator.ui.my_creation.adapter.MyAvatarAdapter
import com.character.ocmaker.creator.ui.my_creation.adapter.MyDesignAdapter
import com.character.ocmaker.creator.ui.my_creation.view_model.MyAvatarViewModel
import com.character.ocmaker.creator.ui.my_creation.view_model.MyDesignViewModel
import com.character.ocmaker.creator.ui.view.ViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import com.character.ocmaker.creator.ui.my_creation.view_model.AlbumLoadState
import com.character.ocmaker.creator.data.model.MyAlbumModel
import kotlinx.coroutines.withContext
import kotlin.getValue

class MyDesignFragment : BaseFragment<FragmentMyDesignBinding>() {
    private val viewModel: MyDesignViewModel by viewModels()
    private val myDesignAdapter by lazy { MyDesignAdapter() }

    private val myAlbumActivity: MyCreationActivity
        get() = requireActivity() as MyCreationActivity

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyDesignBinding {
        return FragmentMyDesignBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.layoutNoItem.isVisible = false
        binding.tvNoitem.select()
        initRcv()
        viewModel.ensureLoaded(myAlbumActivity)
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.myDesignList, viewModel.loadState) { list, state ->
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
            rcvMyDesign.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView, motionEvent: MotionEvent
                ): Boolean {
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
            // Action bar buttons disabled - no delete button for MyDesign tab yet
            // myAlbumActivity.binding.actionBar.btnActionBarRight.tap { handleSelectAll() }
            // myAlbumActivity.binding.actionBar.btnActionBarNextToRight.tap { handleDelete(viewModel.getPathSelected()) }

            // Share and Download buttons are handled in MyCreationActivity

            myDesignAdapter.onItemClick = { pathInternal -> handleItemClick(pathInternal) }
            myDesignAdapter.onItemTick = { position ->
                viewModel.toggleSelect(position)
                // Check if all items are now selected and update the icon
                val allSelected = viewModel.myDesignList.value.all { it.isSelected }
                myAlbumActivity.updateSelectAllIcon(allSelected)
            }
            myDesignAdapter.onDeleteClick = { pathInternal -> handleDelete(arrayListOf(pathInternal)) }
            myDesignAdapter.onLongClick = { position -> handleLongClick(position) }
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvMyDesign.apply {
                adapter = myDesignAdapter
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
        }
        dialog.onNoClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
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

    private fun handleItemClick(pathInternal: String) {
        if (myDesignAdapter.items.any { it.isShowSelection }) {
            // In selection mode - reset before navigating
            resetSelectionMode()
        }
        val intent = Intent(myAlbumActivity, ViewActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(IntentKey.INTENT_KEY, pathInternal)
            putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
            putExtra(IntentKey.STATUS_KEY, ValueKey.MY_DESIGN_TYPE)
        }
        val options = ActivityOptionsCompat.makeCustomAnimation(myAlbumActivity, R.anim.slide_in_right, R.anim.slide_out_left)
        myAlbumActivity.showInterAll { myAlbumActivity.launchViewActivity(intent, options) }
    }

    private fun handleLongClick(position: Int) {
        if (position >= viewModel.myDesignList.value.size) return
        viewModel.showLongClick(position)
        binding.rcvMyDesign.clipToPadding = true
        myAlbumActivity.enterSelectionMode()

        // Check if all items are now selected (e.g., if there's only 1 item)
        val allSelected = viewModel.myDesignList.value.all { it.isSelected }
        myAlbumActivity.updateSelectAllIcon(allSelected)

        val list = viewModel.myDesignList.value
        android.util.Log.d("SelectState", "=== rcvDesign (MyDesign) longClick pos=$position | total=${list.size} ===")
        list.forEachIndexed { i, item ->
            android.util.Log.d("SelectState", "  [$i] isSelected=${item.isSelected} | isShowSelection=${item.isShowSelection} | path=${item.path}")
        }
    }

    fun refreshData() {
        viewModel.loadMyDesign(myAlbumActivity)
    }

    fun hasDisplayItems(): Boolean = viewModel.myDesignList.value.isNotEmpty()

    suspend fun prepareForDisplay() {
        viewModel.prepareMyDesign(myAlbumActivity)
        renderContent(viewModel.myDesignList.value, viewModel.loadState.value)
    }

    private fun renderContent(list: List<MyAlbumModel>, state: AlbumLoadState) {
        if (myDesignAdapter.items != list) myDesignAdapter.submitList(list)
        binding.layoutNoItem.isVisible = list.isEmpty() &&
            (state == AlbumLoadState.Empty || state == AlbumLoadState.Error)
        binding.tvNoitem.text = if (state == AlbumLoadState.Error) {
            getString(R.string.an_error_occurred) + " " + getString(R.string.try_again)
        } else {
            getString(R.string.no_item_here)
        }
    }

    fun getAllPaths(): ArrayList<String> {
        return viewModel.myDesignList.value
            .filterNot { it.isFake }
            .map { it.path }
            .toCollection(ArrayList())
    }

    fun getSelectedPaths(): ArrayList<String> {
        return viewModel.getPathSelected()
    }

    fun deleteSelectedItems() {
        handleDelete(viewModel.getPathSelected())
    }

    fun selectAllItems() {
        viewModel.selectAll(true)
        // Use notifyItemRangeChanged instead of notifyDataSetChanged for better performance
        myDesignAdapter.notifyItemRangeChanged(0, myDesignAdapter.itemCount)
    }

    fun deselectAllItems() {
        viewModel.selectAll(false)
        // Use notifyItemRangeChanged instead of notifyDataSetChanged for better performance
        myDesignAdapter.notifyItemRangeChanged(0, myDesignAdapter.itemCount)
    }

    fun resetSelectionMode() {
        viewModel.clearSelection()
        binding.rcvMyDesign.clipToPadding = false
        myAlbumActivity.exitSelectionMode()
        // notifyItemRangeChanged not needed - data will refresh automatically
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            myAlbumActivity.refreshBottomVisibility()
        }
    }

}
