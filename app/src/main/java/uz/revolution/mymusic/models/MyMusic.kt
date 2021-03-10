package uz.revolution.mymusic.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.material.button.MaterialButton
import java.io.Serializable

@Entity(tableName = "music")
class MyMusic : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int? = null

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "artist")
    var artist:String?=null

    @ColumnInfo(name = "duration")
    var duration:String?=null

    @ColumnInfo(name = "path")
    var path: String? = null

    constructor()

    @Ignore
    constructor(name: String?, artist: String?, duration: String?, path: String?) {
        this.name = name
        this.artist = artist
        this.duration = duration
        this.path = path
    }


}