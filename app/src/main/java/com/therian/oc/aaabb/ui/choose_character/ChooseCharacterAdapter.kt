package com.therian.oc.aaabb.ui.choose_character

import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.loadWithXmlShimmer
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.custom.CustomizeModel
import com.therian.oc.aaabb.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}


    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            imvImage.loadWithXmlShimmer(item.avatar, sflShimmer)
            root.tap { onItemClick.invoke(position) }
        }
    }
}
