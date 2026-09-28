package com.therian.oc.aaabb.ui.add_character.adapter

import android.annotation.SuppressLint
import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.AddCharacterCategoryModel
import com.therian.oc.aaabb.databinding.ItemCategoryBinding

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
