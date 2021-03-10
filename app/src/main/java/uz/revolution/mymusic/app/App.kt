package uz.revolution.mymusic.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import uz.revolution.mymusic.database.AppDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppDatabase.init(this)
    }
}