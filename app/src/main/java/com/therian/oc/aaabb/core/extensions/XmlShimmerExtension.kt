package com.therian.oc.aaabb.core.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.ShimmerFrameLayout

/** Keep the XML shimmer visible while loading and after failure, until an image succeeds. */
fun ImageView.loadWithXmlShimmer(
    model: Any?,
    shimmer: ShimmerFrameLayout,
    options: RequestOptions = RequestOptions()
) {
    Glide.with(this).clear(this)
    Glide.with(this)
        .load(model)
        .apply(options)
        .into(object : DrawableImageViewTarget(this) {
            override fun onLoadStarted(placeholder: Drawable?) {
                super.onLoadStarted(placeholder)
                shimmer.visible()
                shimmer.startShimmer()
            }

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                super.onResourceReady(resource, transition)
                hideShimmer()
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(null)
                shimmer.visible()
                shimmer.startShimmer()
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                super.onLoadCleared(placeholder)
                hideShimmer()
            }

            private fun hideShimmer() {
                shimmer.stopShimmer()
                shimmer.gone()
            }
        })
}
