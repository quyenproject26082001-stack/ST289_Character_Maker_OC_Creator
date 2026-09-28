package com.therian.oc.aaabb.ui.add_character.adapter

import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.loadImageSticker
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.SelectedModel
import com.therian.oc.aaabb.databinding.ItemSpeechBinding

class SpeechAdapter : BaseAdapter<SelectedModel, ItemSpeechBinding>(ItemSpeechBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemSpeechBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSpeech)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}
