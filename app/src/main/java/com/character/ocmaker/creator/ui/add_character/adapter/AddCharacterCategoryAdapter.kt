package com.character.ocmaker.creator.ui.add_character.adapter

import android.annotation.SuppressLint
import com.character.ocmaker.creator.core.base.BaseAdapter
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.data.model.AddCharacterCategoryModel
import com.character.ocmaker.creator.databinding.ItemCategoryBinding

class AddCharacterCategoryAdapter :
    BaseAdapter<AddCharacterCategoryModel, ItemCategoryBinding>(ItemCategoryBinding::inflate) {

    var onCategoryClick: ((Int) -> Unit) = {}

    @SuppressLint("NotifyDataSetChanged")
    override fun submitList(list: List<AddCharacterCategoryModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBind(
        binding: ItemCategoryBinding,
        item: AddCharacterCategoryModel,
        position: Int
    ) {
        binding.apply {
            tvCategory.text = item.name
            tvCategory.isSelected = true
            tvCategory.isActivated = item.isSelected
            root.tap { onCategoryClick(position) }
        }
    }
}
