package com.therian.oc.aaabb.ui.add_character.adapter

import android.graphics.Color
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.loadWithXmlShimmer
import com.therian.oc.aaabb.core.extensions.gone
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.core.extensions.visible
import com.therian.oc.aaabb.data.model.SelectedModel
import com.therian.oc.aaabb.databinding.ItemBackgroundImageBinding

class BackgroundImageAdapter :
    BaseAdapter<SelectedModel, ItemBackgroundImageBinding>(ItemBackgroundImageBinding::inflate) {
    var onAddImageClick: (() -> Unit) = {}
    var onBackgroundImageClick: ((String, Int) -> Unit) = { _, _ -> }
    var currentSelected = -1

    override fun onBind(binding: ItemBackgroundImageBinding, item: SelectedModel, position: Int) {
        binding.apply {
            tvAddImg.isSelected=true
            if (item.isSelected) {
                containerCard.setStrokeColor(Color.parseColor("#DC3C01"))
            } else {
                containerCard.setStrokeColor(Color.TRANSPARENT)
            }

            lnlAddItem.gone()
            btnNone.gone()
            imvImage.gone()
            sflShimmer.stopShimmer()
            sflShimmer.gone()

            when (position) {
                ADD_IMAGE_POSITION -> {
                    Glide.with(root).clear(imvImage)
                    lnlAddItem.visible()
                    lnlAddItem.tap(800) { onAddImageClick.invoke() }
                }

                NONE_ITEM_POSITION -> {
                    Glide.with(root).clear(imvImage)
                    btnNone.visible()
                    btnNone.tap { onBackgroundImageClick.invoke(item.path, position) }
                }

                else -> {
                    imvImage.visible()
                    imvImage.loadWithXmlShimmer(
                        item.path,
                        sflShimmer,
                        RequestOptions()
                            .override(256, 256)
                            .encodeQuality(60)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerCrop()
                    )
                    imvImage.tap { onBackgroundImageClick.invoke(item.path, position) }
                }
            }
        }
    }

    override fun submitList(list: List<SelectedModel>) {
        currentSelected = list.indexOfFirst { it.isSelected }
        super.submitList(list)
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>) {
        if (position != currentSelected) {
            items.clear()
            items.addAll(list)

            if (currentSelected >= 0) notifyItemChanged(currentSelected)
            notifyItemChanged(position)

            currentSelected = position
        }
    }

    companion object {
        const val ADD_IMAGE_POSITION = 0
        const val NONE_ITEM_POSITION = 1
    }
}
