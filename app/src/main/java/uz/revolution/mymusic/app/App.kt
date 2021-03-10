package uz.revolution.mymusic.app

import android.app.Application
import uz.revolution.mymusic.database.AppDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.init(this)
    }
}