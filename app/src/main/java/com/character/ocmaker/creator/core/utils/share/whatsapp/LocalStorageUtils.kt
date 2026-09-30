package com.character.ocmaker.creator.core.utils.share.whatsapp

import android.content.Context
import com.character.ocmaker.creator.data.local.PersistenceRepository

object LocalStorageUtils {

    fun readData(context: Context, key: String): Any? {
        return PersistenceRepository.getStringSetting(context, key, "")
            .takeIf { it.isNotEmpty() }
    }
}
