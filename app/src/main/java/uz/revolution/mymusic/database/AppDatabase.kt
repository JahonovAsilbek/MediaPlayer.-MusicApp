package uz.revolution.mymusic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.revolution.mymusic.daos.MusicDao
import uz.revolution.mymusic.models.MyMusic

@Database(entities = [MyMusic::class],version = 1,exportSchema = false)
abstract class AppDatabase:RoomDatabase() {
    abstract fun getMusicDao():MusicDao

    companion object{

        @Volatile
        private var database:AppDatabase?=null

        fun init(context: Context) {
            synchronized(this) {
                if (database == null) {
                    database= Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"music.db")
                        .allowMainThreadQueries()
                        .build()
                }
            }
        }
    }

    object get{
        fun getDatabase(): AppDatabase {
            return database!!
        }
    }
}