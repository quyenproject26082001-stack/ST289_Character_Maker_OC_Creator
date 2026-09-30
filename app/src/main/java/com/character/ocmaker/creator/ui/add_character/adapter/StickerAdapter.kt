package com.character.ocmaker.creator.ui.add_character.adapter

import com.character.ocmaker.creator.core.base.BaseAdapter
import com.character.ocmaker.creator.core.extensions.loadImage
import com.character.ocmaker.creator.core.extensions.loadWithXmlShimmer
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.character.ocmaker.creator.core.extensions.tap
import com.character.ocmaker.creator.data.model.SelectedModel
import com.character.ocmaker.creator.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            imvSticker.loadWithXmlShimmer(
                item.path,
                sflShimmer,
                RequestOptions()
                    .encodeQuality(70)
                    .override(256, 256)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .transform(RoundedCorners(24))
            )
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}
