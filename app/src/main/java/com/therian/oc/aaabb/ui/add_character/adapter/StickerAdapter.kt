package com.therian.oc.aaabb.ui.add_character.adapter

import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.loadImage
import com.therian.oc.aaabb.core.extensions.loadImageSticker
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.SelectedModel
import com.therian.oc.aaabb.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}