package com.therian.oc.aaabb.ui.choose_character

import androidx.core.content.ContextCompat
import com.therian.oc.aaabb.R
import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.gone
import com.therian.oc.aaabb.core.extensions.loadImage
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.custom.CustomizeModel
import com.therian.oc.aaabb.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}


    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {

            loadImage(item.avatar, imvImage, onDismissLoading = {
                sflShimmer.stopShimmer()
                sflShimmer.gone()
            })
            root.tap { onItemClick.invoke(position) }
        }
    }
}