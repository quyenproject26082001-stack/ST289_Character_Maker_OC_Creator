package com.character.ocmaker.creator.ui.customize

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.character.ocmaker.creator.R
import com.character.ocmaker.creator.core.base.BaseAdapter
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.data.model.custom.ItemColorModel
import com.character.ocmaker.creator.databinding.ItemColorBinding

class ColorLayerCustomizeAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            val colorSize = root.resources.getDimensionPixelSize(
                if (item.isSelected) R.dimen.dp_24 else R.dimen.dp_24
            )
            imvImage.layoutParams = imvImage.layoutParams.apply {
                width = colorSize
                height = colorSize
            }
            imvImage.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(item.color.toColorInt())
            }
            imvFocus.isVisible = item.isSelected
            root.tap {
                val rv = root.parent as? RecyclerView ?: return@tap
                val currentPosition = rv.getChildAdapterPosition(root)
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick.invoke(currentPosition)
                }
            }
        }
    }
}
