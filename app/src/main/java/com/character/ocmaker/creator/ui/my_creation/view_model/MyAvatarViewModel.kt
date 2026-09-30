package com.character.ocmaker.creator.ui.my_creation.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.character.ocmaker.creator.core.base.BaseActivity
import com.character.ocmaker.creator.core.helper.InternetHelper
import com.character.ocmaker.creator.core.helper.MediaHelper
import com.character.ocmaker.creator.core.utils.key.ValueKey
import com.character.ocmaker.creator.core.utils.state.HandleState
import com.character.ocmaker.creator.data.local.PersistenceRepository
import com.character.ocmaker.creator.data.model.MyAlbumModel
import com.character.ocmaker.creator.data.model.custom.CustomizeModel
import com.character.ocmaker.creator.data.model.custom.SuggestionModel
import com.character.ocmaker.creator.ui.my_creation.MyCreationPreviewConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyAvatarViewModel : ViewModel() {
    private val _myAvatarList = MutableStateFlow<ArrayList<MyAlbumModel>>(arrayListOf())
    val myAvatarList = _myAvatarList.asStateFlow()
    private val _isLastItem = MutableStateFlow<Boolean>(false)
    val isLastItem: StateFlow<Boolean> = _isLastItem


    var isApi: Boolean = false
    var positionCharacter = -1
    var editModel = SuggestionModel()
    private var loadJob: Job? = null
    private val _loadState = MutableStateFlow(AlbumLoadState.Loading)
    val loadState = _loadState.asStateFlow()

    fun ensureLoaded(context: Context) {
        if (loadJob == null && _loadState.value == AlbumLoadState.Loading) {
            loadMyAvatar(context)
        }
    }

    suspend fun prepareMyAvatar(context: Context) {
        ensureLoaded(context)
        loadState.first { it != AlbumLoadState.Loading }
    }

    fun loadMyAvatar(context: Context) {
        // A background refresh keeps the last successful UI available for tab switches.
        if (_loadState.value == AlbumLoadState.Error) _loadState.value = AlbumLoadState.Loading
        if (MyCreationPreviewConfig.SHOW_FAKE_ITEMS) {
            loadJob?.cancel()
            _myAvatarList.value = MyCreationPreviewConfig.createFakeItems("avatar")
            updateLoadState()
            checkLastItem()
            return
        }
        val appContext = context.applicationContext
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val paths = PersistenceRepository.getSavedAvatarPaths(appContext)
                _myAvatarList.value =
                    paths.map { MyAlbumModel(it) }.toCollection(ArrayList())
                updateLoadState()
                checkLastItem()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("MyAvatarViewModel", "Failed to load saved avatars", error)
                // Keep existing content if a refresh fails.
                _loadState.value = AlbumLoadState.Error
            }
        }
    }

    private fun updateLoadState() {
        _loadState.value = if (_myAvatarList.value.isEmpty()) AlbumLoadState.Empty else AlbumLoadState.Content
    }

    private fun checkLastItem() {
        _isLastItem.value = _myAvatarList.value.any { !it.isSelected }
    }

    suspend fun deleteItem(context: Context, pathList: ArrayList<String>) {
        loadJob?.cancel()
        PersistenceRepository.deleteSavedAvatarRows(context.applicationContext, pathList)
        _myAvatarList.value = _myAvatarList.value
            .filterNot { it.path in pathList }
            .toCollection(ArrayList())
        updateLoadState()
        checkLastItem()
    }

    suspend fun editItem(
        context: Context,
        pathInternal: String,
        allData: ArrayList<CustomizeModel>
    ): Boolean {
        val savedEditModel = PersistenceRepository.getSavedAvatar(
            context.applicationContext,
            pathInternal
        ) ?: return false
        val savedCharacterPosition =
            allData.indexOfFirst { it.avatar == savedEditModel.avatarPath }
        if (savedCharacterPosition < 0) {
            positionCharacter = -1
            isApi = false
            return false
        }

        editModel = savedEditModel
        positionCharacter = savedCharacterPosition
        isApi = allData[positionCharacter].isFromAPI
        MediaHelper.writeModelToFile(context, ValueKey.SUGGESTION_FILE_INTERNAL, editModel)
        return true
    }

    fun checkDataInternet(context: BaseActivity<*>, action: (() -> Unit)) {
        if (!isApi) {
            action.invoke()
            return
        }
        InternetHelper.checkInternet(context) { result ->
            if (result == HandleState.SUCCESS) {
                action.invoke()
            } else {
                // Show No Internet dialog
                val dialog = com.character.ocmaker.creator.dialog.YesNoDialog(
                    context,
                    com.character.ocmaker.creator.R.string.no_internet,
                    com.character.ocmaker.creator.R.string.please_check_your_internet,
                    isError = true
                )
                dialog.show()
                dialog.onYesClick = {
                    dialog.dismiss()
                }
            }
        }
    }

    fun showLongClick(positionSelect: Int) {
        _myAvatarList.value = _myAvatarList.value.mapIndexed { position, item ->
            item.copy(isSelected = position == positionSelect, isShowSelection = true)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    fun selectAll(shouldSelect: Boolean) {
        _myAvatarList.value = _myAvatarList.value.map {
            it.copy(isSelected = shouldSelect, isShowSelection = true)
        }.toCollection(ArrayList())
        checkLastItem()
    }

    fun toggleSelect(position: Int) {
        val list = _myAvatarList.value.toMutableList()
        list[position] = list[position].copy(isSelected = !list[position].isSelected, isShowSelection = true)
        _myAvatarList.value = list.toCollection(ArrayList())
        checkLastItem()
    }

    fun getPathSelected() : ArrayList<String>{
        return _myAvatarList.value
            .filter { it.isSelected && !it.isFake }
            .map { it.path }
            .toCollection(ArrayList())
    }

    fun clearSelection() {
        _myAvatarList.value = _myAvatarList.value.map {
            it.copy(isSelected = false, isShowSelection = false)
        }.toCollection(ArrayList())
        checkLastItem()
    }
}
