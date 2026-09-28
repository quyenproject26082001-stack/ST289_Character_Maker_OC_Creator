package com.therian.oc.aaabb.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.therian.oc.aaabb.data.local.dao.AppSettingDao
import com.therian.oc.aaabb.data.local.dao.CharacterDao
import com.therian.oc.aaabb.data.local.dao.JsonCacheDao
import com.therian.oc.aaabb.data.local.dao.MediaItemDao
import com.therian.oc.aaabb.data.local.dao.SavedAvatarDao
import com.therian.oc.aaabb.data.local.entity.AppSettingEntity
import com.therian.oc.aaabb.data.local.entity.CharacterColorEntity
import com.therian.oc.aaabb.data.local.entity.CharacterEntity
import com.therian.oc.aaabb.data.local.entity.CharacterLayerEntity
import com.therian.oc.aaabb.data.local.entity.CharacterLayerGroupEntity
import com.therian.oc.aaabb.data.local.entity.JsonCacheEntity
import com.therian.oc.aaabb.data.local.entity.MediaItemEntity
import com.therian.oc.aaabb.data.local.entity.SavedAvatarEntity

@Database(
    entities = [
        AppSettingEntity::class,
        JsonCacheEntity::class,
        SavedAvatarEntity::class,
        MediaItemEntity::class,
        CharacterEntity::class,
        CharacterLayerGroupEntity::class,
        CharacterLayerEntity::class,
        CharacterColorEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appSettingDao(): AppSettingDao
    abstract fun jsonCacheDao(): JsonCacheDao
    abstract fun savedAvatarDao(): SavedAvatarDao
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun characterDao(): CharacterDao
}
