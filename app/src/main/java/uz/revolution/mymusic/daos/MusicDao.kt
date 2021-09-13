package uz.revolution.mymusic.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uz.revolution.mymusic.models.MyMusic

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMusic(myMusic: MyMusic)

    @Query("SELECT * FROM music order by name")
    fun getAllMusic(): List<MyMusic>

    @Query("DElETE FROM music WHERE duration=:duration")
    fun deleteEmptyMusic(duration: String)

    @Query("DELETE FROM music")
    fun deleteAllData()
}