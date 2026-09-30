package com.therian.oc.aaabb.ui.customize

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.therian.oc.aaabb.R
import com.therian.oc.aaabb.core.extensions.loadWithXmlShimmer
import com.therian.oc.aaabb.core.extensions.setMargins
import com.therian.oc.aaabb.core.extensions.tap
import com.therian.oc.aaabb.data.model.custom.NavigationModel
import com.therian.oc.aaabb.databinding.ItemBottomNavigationBinding

class BottomNavigationCustomizeAdapter(
    private val context: Context,
    private val useSouthParkStyle: Boolean = false
) :
    ListAdapter<NavigationModel, BottomNavigationCustomizeAdapter.BottomNavViewHolder>(DiffCallback) {
    var onItemClick: (Int) -> Unit = {}

    inner class BottomNavViewHolder(
        private val binding: ItemBottomNavigationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NavigationModel, position: Int) = with(binding) {

            // Apply circular clipping to cvContent (so shimmer/image fills circle and doesn't overflow)
//            cvContent.clipToOutline = true
//            cvContent.outlineProvider = object : ViewOutlineProvider() {
//                override fun getOutline(view: View, outline: Outline) {
//                    outline.setOval(0, 0, view.width, view.height)
//                }
//            }

            // Apply circular clipping to imvImage (shimmer layer - fills full circle)
           // imvImage.clipToOutline = true
//            imvImage.outlineProvider = object : ViewOutlineProvider() {
//                override fun getOutline(view: View, outline: Outline) {
//                    outline.setOval(0, 0, view.width, view.height)
//                }
//            }
//
//            // Apply circular clipping to imvImageBG (actual image layer - with margin)
//            imvImageBG.clipToOutline = true
//            imvImageBG.outlineProvider = object : ViewOutlineProvider() {
//                override fun getOutline(view: View, outline: Outline) {
//                    outline.setOval(0, 0, view.width, view.height)
//                }
//            }
//
//            val offset = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                15f,
//                cvContent.resources.displayMetrics
//            )

            // Cancel any running animations to prevent jumps when recycling views
            cvContent.animate().cancel()

            val normalSize = context.resources.getDimensionPixelSize(R.dimen.dp_48)
            val selectedSize = context.resources.getDimensionPixelSize(R.dimen.dp_56)

            if (useSouthParkStyle) {
                val itemSize = if (item.isSelected) selectedSize else normalSize
                binding.main.layoutParams = binding.main.layoutParams.apply {
                    width = itemSize
                    height = selectedSize
                }
                binding.cvContent.layoutParams = binding.cvContent.layoutParams.apply {
                    width = itemSize
                    height = itemSize
                }
                binding.main.setMargins(
                    left = 0,
                    top = 0,
                    right = context.resources.getDimensionPixelSize(R.dimen.dp_1),
                    bottom = 0
                )
            } else {
                binding.main.layoutParams = binding.main.layoutParams.apply {
                    width = normalSize
                    height = normalSize
                }
                binding.cvContent.layoutParams = binding.cvContent.layoutParams.apply {
                    width = normalSize
                    height = normalSize
                }
                val horizontalMargin = context.resources.getDimensionPixelSize(R.dimen.dp_4)
                binding.main.setMargins(horizontalMargin, 0, horizontalMargin, 0)
            }

            if (item.isSelected) {
                imvImage.setBackgroundColor(Color.TRANSPARENT)
                cvContent.setBackgroundResource(
                    if (useSouthParkStyle) {
                        R.drawable.bg_bottom_nav_selected
                    } else {
                        R.drawable.bg_bottom_nav_selected
                    }
                )
            } else {
                imvImage.setBackgroundColor(Color.TRANSPARENT)
                cvContent.setBackgroundResource(
                    if (useSouthParkStyle) {
                        R.drawable.bg_bottom_nav_unselected
                    } else {
                        R.drawable.bg_bottom_nav_unselected
                    }
                )
            }

            imvImageBG.loadWithXmlShimmer(item.imageNavigation, sflShimmer)

            root.tap { onItemClick.invoke(position) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomNavViewHolder {
        val binding = ItemBottomNavigationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BottomNavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomNavViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<NavigationModel>() {
            override fun areItemsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                // Nếu NavigationModel có id riêng thì nên so sánh id, ở đây tạm so sánh hình
                return oldItem.imageNavigation == newItem.imageNavigation
            }

            override fun areContentsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
