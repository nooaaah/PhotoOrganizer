package com.noah.photoorganizer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.noah.photoorganizer.data.dao.AlbumDao
import com.noah.photoorganizer.data.dao.AlbumPhotoDao
import com.noah.photoorganizer.data.dao.FavoriDao
import com.noah.photoorganizer.data.dao.GroupeDao
import com.noah.photoorganizer.data.model.Album
import com.noah.photoorganizer.data.model.AlbumPhoto
import com.noah.photoorganizer.data.model.Favori
import com.noah.photoorganizer.data.model.Groupe

@Database(
    entities = [Groupe::class, Album::class, AlbumPhoto::class, Favori::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun groupeDao(): GroupeDao
    abstract fun albumDao(): AlbumDao
    abstract fun albumPhotoDao(): AlbumPhotoDao
    abstract fun favoriDao(): FavoriDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_organizer_database"
                ).fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}