package com.therian.oc.aaabb.ui.my_creation

import com.therian.oc.aaabb.R
import com.therian.oc.aaabb.data.model.MyAlbumModel

object MyCreationPreviewConfig {
    const val SHOW_FAKE_ITEMS = false
    const val FAKE_ITEM_COUNT = 20

    fun createFakeItems(type: String): ArrayList<MyAlbumModel> {
        return List(FAKE_ITEM_COUNT.coerceAtLeast(0)) { index ->
            MyAlbumModel(
                path = "fake://$type/$index",
                previewResId = R.drawable.img_splash_avatar,
                isFake = true
            )
        }.toCollection(ArrayList())
    }
}
