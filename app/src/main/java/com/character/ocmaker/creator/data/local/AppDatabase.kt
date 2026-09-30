package com.character.ocmaker.creator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.character.ocmaker.creator.data.local.dao.AppSettingDao
import com.character.ocmaker.creator.data.local.dao.CharacterDao
import com.character.ocmaker.creator.data.local.dao.JsonCacheDao
import com.character.ocmaker.creator.data.local.dao.MediaItemDao
import com.character.ocmaker.creator.data.local.dao.SavedAvatarDao
import com.character.ocmaker.creator.data.local.entity.AppSettingEntity
import com.character.ocmaker.creator.data.local.entity.CharacterColorEntity
import com.character.ocmaker.creator.data.local.entity.CharacterEntity
import com.character.ocmaker.creator.data.local.entity.CharacterLayerEntity
import com.character.ocmaker.creator.data.local.entity.CharacterLayerGroupEntity
import com.character.ocmaker.creator.data.local.entity.JsonCacheEntity
import com.character.ocmaker.creator.data.local.entity.MediaItemEntity
import com.character.ocmaker.creator.data.local.entity.SavedAvatarEntity

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
