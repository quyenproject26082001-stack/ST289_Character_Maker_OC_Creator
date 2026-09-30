package com.therian.oc.aaabb.ui.add_character.adapter

import com.therian.oc.aaabb.core.base.BaseAdapter
import com.therian.oc.aaabb.core.extensions.loadWithXmlShimmer
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.SelectedModel
import com.therian.oc.aaabb.databinding.ItemSpeechBinding

class SpeechAdapter : BaseAdapter<SelectedModel, ItemSpeechBinding>(ItemSpeechBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemSpeechBinding, item: SelectedModel, position: Int) {
        binding.apply {
            imvSpeech.loadWithXmlShimmer(
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
