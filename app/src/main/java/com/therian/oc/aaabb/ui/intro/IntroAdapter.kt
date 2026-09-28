package com.therian.oc.aaabb.ui.intro

import android.content.Context
import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.loadImage
import com.therian.oc.aaabb.core.extensions.select
import com.therian.oc.aaabb.core.extensions.strings
import com.therian.oc.aaabb.data.model.IntroModel
import com.therian.oc.aaabb.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}