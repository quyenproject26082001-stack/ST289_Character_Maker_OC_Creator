package com.character.ocmaker.creator.ui.choose_character

import com.character.ocmaker.creator.core.base.BaseAdapter
import com.character.ocmaker.creator.core.extensions.loadWithXmlShimmer
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.data.model.custom.CustomizeModel
import com.character.ocmaker.creator.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}


    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            imvImage.loadWithXmlShimmer(item.avatar, sflShimmer)
            root.tap { onItemClick.invoke(position) }
        }
    }
}
