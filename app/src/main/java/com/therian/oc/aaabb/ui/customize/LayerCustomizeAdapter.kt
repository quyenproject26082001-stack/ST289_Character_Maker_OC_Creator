package com.therian.oc.aaabb.ui.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.therian.oc.aaabb.R
import com.therian.oc.aaabb.core.extensions.gone
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.core.extensions.visible
import com.therian.oc.aaabb.core.utils.key.AssetsKey
import com.therian.oc.aaabb.core.extensions.loadWithXmlShimmer
import com.therian.oc.aaabb.data.model.custom.ItemNavCustomModel
import com.therian.oc.aaabb.databinding.ItemCustomizeBinding
import com.bumptech.glide.Glide

class LayerCustomizeAdapter(val context: Context) : ListAdapter<ItemNavCustomModel, LayerCustomizeAdapter.CustomizeViewHolder>(DiffCallback) {

    var onItemClick: ((ItemNavCustomModel, Int) -> Unit) = { _, _ -> }
    var onNoneClick: ((Int) -> Unit) = {}
    var onRandomClick: (() -> Unit) = {}

    inner class CustomizeViewHolder(val binding: ItemCustomizeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: ItemNavCustomModel, position: Int) {
            binding.apply {
                val cornerRadiusPx = context.resources.getDimensionPixelSize(R.dimen.dp_4)

                val itemType = when (item.path) {
                    AssetsKey.NONE_LAYER -> "NONE"
                    AssetsKey.RANDOM_LAYER -> "RANDOM"
                    else -> "IMAGE"
                }
                android.util.Log.d(
                    "ItemCustomizeState",
                    "[$position] type=$itemType | isSelected=${item.isSelected} | path=${item.path} | colors=${item.listImageColor.size}"
                )

                if (item.isSelected) {
                    // Bring selected item to front with elevation
                    root.translationZ = 16f
                    root.scaleX = 1.0f
                    root.scaleY = 1.0f
                    vFocus.gone()

                    cardLayerItem.setBackgroundResource(R.drawable.layer_slt)
                    // vFocus.setBackgroundResource(R.drawable.bg_10_stroke_yellow)
                } else {
                    // Reset to normal state
                    root.translationZ = 0f
                    root.scaleX = 1f
                    root.scaleY = 1f
                    vFocus.gone()
                    cardLayerItem.setBackgroundResource(R.drawable.layer_uslt)


                }

                when (item.path) {
                    AssetsKey.NONE_LAYER -> {
                        Glide.with(root).clear(imvImage)
                        btnNone.visible()
                        btnRandom.gone()
                        imvImage.gone()
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    AssetsKey.RANDOM_LAYER -> {
                        Glide.with(root).clear(imvImage)
                        btnNone.gone()
                        btnRandom.visible()
                        imvImage.gone()
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    else -> {
                        btnNone.gone()
                        imvImage.visible()
                        btnRandom.gone()
                        imvImage.loadWithXmlShimmer(
                            item.thumb.ifEmpty { item.path },
                            sflShimmer,
                            RequestOptions().transform(RoundedCorners(cornerRadiusPx))
                        )
                    }
                }

                binding.imvImage.tap(100) { onItemClick.invoke(item, position) }

                binding.btnRandom.tap { onRandomClick.invoke() }

                binding.btnNone.tap { onNoneClick.invoke(position) }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizeViewHolder {
        val binding = ItemCustomizeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomizeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomizeViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<ItemNavCustomModel>(){
            override fun areItemsTheSame(oldItem: ItemNavCustomModel, newItem: ItemNavCustomModel): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: ItemNavCustomModel, newItem: ItemNavCustomModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
