package com.character.ocmaker.creator.ui.my_creation.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.character.ocmaker.creator.core.helper.MediaHelper
import com.character.ocmaker.creator.core.utils.key.ValueKey
import com.character.ocmaker.creator.data.local.PersistenceRepository
import com.character.ocmaker.creator.data.model.MyAlbumModel
import com.character.ocmaker.creator.ui.my_creation.MyCreationPreviewConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyDesignViewModel : ViewModel() {
    private val _myDesignList = MutableStateFlow<ArrayList<MyAlbumModel>>(arrayListOf())
    val myDesignList = _myDesignList.asStateFlow()
    private val _isLastItem = MutableStateFlow<Boolean>(false)
    val isLastItem: StateFlow<Boolean> = _isLastItem
    private var loadJob: Job? = null
    private val _loadState = MutableStateFlow(AlbumLoadState.Loading)
    val loadState = _loadState.asStateFlow()

    fun ensureLoaded(context: Context) {
        if (loadJob == null && _loadState.value == AlbumLoadState.Loading) {
            loadMyDesign(context)
        }
    }

    suspend fun prepareMyDesign(context: Context) {
        ensureLoaded(context)
        loadState.first { it != AlbumLoadState.Loading }
    }

    fun loadMyDesign(context: Context) {
        // A background refresh keeps the last successful UI available for tab switches.
        if (_loadState.value == AlbumLoadState.Error) _loadState.value = AlbumLoadState.Loading
        if (MyCreationPreviewConfig.SHOW_FAKE_ITEMS) {
            loadJob?.cancel()
            _myDesignList.value = MyCreationPreviewConfig.createFakeItems("design")
            updateLoadState()
            checkLastItem()
            return
        }
        val appContext = context.applicationContext
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val paths = PersistenceRepository.getAlbumPathsAsync(
                    appContext,
                    ValueKey.DOWNLOAD_ALBUM
                )
                _myDesignList.value =
                    paths.map { MyAlbumModel(it) }.toCollection(ArrayList())
                updateLoadState()
                checkLastItem()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("MyDesignViewModel", "Failed to load saved designs", error)
                // Keep existing content if a refresh fails.
                _loadState.value = AlbumLoadState.Error
            }
        }
    }

    fun showLongClick(positionSelect: Int) {
        _myDesignList.value = _myDesignList.value.mapIndexed { position, item ->
            item.copy(isSelected = position == positionSelect, isShowSelection = true)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    private fun updateLoadState() {
        _loadState.value = if (_myDesignList.value.isEmpty()) AlbumLoadState.Empty else AlbumLoadState.Content
    }

    private fun checkLastItem() {
        _isLastItem.value = _myDesignList.value.any { !it.isSelected }
    }

    suspend fun deleteItem(context: Context, pathList: ArrayList<String>) {
        loadJob?.cancel()
        MediaHelper.deleteFileByPathNotFlow(pathList, context)
        _myDesignList.value = _myDesignList.value
            .filterNot { it.path in pathList }
            .toCollection(ArrayList())
        updateLoadState()
        checkLastItem()
    }

    fun toggleSelect(position: Int) {
        val list = _myDesignList.value.toMutableList()
        list[position] = list[position].copy(isSelected = !list[position].isSelected, isShowSelection = true)
        _myDesignList.value = list.toCollection(ArrayList())
        checkLastItem()
    }

    fun selectAll(shouldSelect: Boolean) {
        _myDesignList.value = _myDesignList.value.map {
            it.copy(isSelected = shouldSelect, isShowSelection = true)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    fun getPathSelected() : ArrayList<String>{
        return _myDesignList.value.filter { it.isSelected && !it.isFake }.map { it.path }.toCollection(ArrayList())
    }

    fun clearSelection() {
        _myDesignList.value = _myDesignList.value.map {
            it.copy(isSelected = false, isShowSelection = false)
        }.toCollection(ArrayList())
        checkLastItem()
    }
}
