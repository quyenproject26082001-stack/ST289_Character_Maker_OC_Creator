package com.character.ocmaker.creator.ui.intro

import android.content.Context
import com.character.ocmaker.creator.core.base.BaseAdapter
import com.character.ocmaker.creator.core.extensions.loadImage
import com.character.ocmaker.creator.core.extensions.select
import com.character.ocmaker.creator.core.extensions.strings
import com.character.ocmaker.creator.data.model.IntroModel
import com.character.ocmaker.creator.databinding.ItemIntroBinding

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