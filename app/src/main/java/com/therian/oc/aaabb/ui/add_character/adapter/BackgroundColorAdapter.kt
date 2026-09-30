package com.therian.oc.aaabb.ui.add_character.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import com.bumptech.glide.Glide
import com.therian.oc.aaabb.R
import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.core.extensions.gone
import com.therian.oc.aaabb.core.extensions.visible
import com.therian.oc.aaabb.data.model.SelectedModel
import com.therian.oc.aaabb.databinding.ItemBackgroundColorBinding

class BackgroundColorAdapter :
    BaseAdapter<SelectedModel, ItemBackgroundColorBinding>(ItemBackgroundColorBinding::inflate) {
    var onChooseColorClick: (() -> Unit) = {}
    var onBackgroundColorClick: ((Int, Int) -> Unit) = {_,_ ->}

    var currentSelected = -1
    override fun onBind(binding: ItemBackgroundColorBinding, item: SelectedModel, position: Int) {
        Log.d("BackgroundColorAdapter", "onBind position=$position, color=${String.format("#%06X", 0xFFFFFF and item.color)}, isSelected=${item.isSelected}, path=${item.path}")

        binding.apply {
            sflShimmer.stopShimmer()
            sflShimmer.gone()
            if (item.isSelected) {
                cardCtn.setStrokeColor(Color.parseColor("#DC3C01"))
            } else {
                cardCtn.setStrokeColor(Color.TRANSPARENT)
            }
            // Set circular stroke for position 0, regular stroke for others


            if (position == 0) {
                Log.d("BackgroundColorAdapter", "Position 0: Setting local image")
                Glide.with(root.context).clear(imvColor)
                imvColor.background = null
                imvColor.setImageResource(R.drawable.img)
                if (imvColor.drawable == null) {
                    sflShimmer.visible()
                    sflShimmer.startShimmer()
                }
                root.tap { onChooseColorClick.invoke() }
            } else {
                Log.d("BackgroundColorAdapter", "Position $position: Setting color background")
                Glide.with(root.context).clear(imvColor)
                imvColor.setImageDrawable(null)
                imvColor.background = GradientDrawable().apply {
                   // shape = GradientDrawable.OVAL
                    setColor(item.color)
                }
                root.tap { onBackgroundColorClick.invoke(item.color, position) }
            }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>){
        Log.d("BackgroundColorAdapter", "submitItem called with position=$position")
        Log.d("BackgroundColorAdapter", "Item at position 0: color=${String.format("#%06X", 0xFFFFFF and list[0].color)}, isSelected=${list[0].isSelected}")
        if (position == 0) {
            Log.d("BackgroundColorAdapter", "WARNING: Position 0 was selected!")
        }

        items.clear()
        items.addAll(list)

        if (position != currentSelected){
            Log.d("BackgroundColorAdapter", "Notifying changes for positions $currentSelected and $position")
            notifyItemChanged(currentSelected)
            notifyItemChanged(position)
            currentSelected = position
        } else {
            Log.d("BackgroundColorAdapter", "Notifying change for position $position")
            notifyItemChanged(position)
        }
    }
}
