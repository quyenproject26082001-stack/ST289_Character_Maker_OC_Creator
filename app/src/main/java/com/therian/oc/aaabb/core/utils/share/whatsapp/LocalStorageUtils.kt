package com.therian.oc.aaabb.core.utils.share.whatsapp

import android.content.Context
import com.therian.oc.aaabb.data.local.PersistenceRepository

object LocalStorageUtils {

    fun readData(context: Context, key: String): Any? {
        return PersistenceRepository.getStringSetting(context, key, "")
            .takeIf { it.isNotEmpty() }
    }
}
