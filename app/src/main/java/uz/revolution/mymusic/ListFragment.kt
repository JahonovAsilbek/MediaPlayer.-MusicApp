package uz.revolution.mymusic

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.florent37.runtimepermission.kotlin.askPermission
import uz.revolution.mymusic.adapters.MusicAdapter
import uz.revolution.mymusic.daos.MusicDao
import uz.revolution.mymusic.database.AppDatabase
import uz.revolution.mymusic.databinding.FragmentListBinding
import uz.revolution.mymusic.models.MyMusic
import java.util.concurrent.TimeUnit


class ListFragment : Fragment() {

    lateinit var binding: FragmentListBinding
    private var data: ArrayList<MyMusic>? = null
    private var adapter: MusicAdapter? = null
    private var database: AppDatabase? = null
    private var getMusicDao: MusicDao? = null
    lateinit var mmr: MediaMetadataRetriever

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)
        mmr = MediaMetadataRetriever()
        adapter = MusicAdapter()
        loadDatabase()
        checkPermission(binding.root.context)
        loadData()
        loadAdapter()
        itemClick()

        return binding.root
    }

    private fun itemClick() {
        adapter?.onItemCLick = object : MusicAdapter.OnItemCLick {
            override fun onClick(myMusic: MyMusic, position: Int) {
                val bundle = Bundle()
                bundle.putSerializable("param1", myMusic)
                bundle.putInt("param2", position)
                bundle.putInt("param3", data!!.size)
                findNavController().navigate(R.id.musicFragment, bundle)
            }
        }
    }


    private fun checkPermission(context: Context) {

        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
            //all permissions already granted or just granted
            getAllMusic(context)
        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(context)
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if (e.hasForeverDenied()) {
                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
    }

    private fun getAllMusic(context: Context) {

        if (ContextCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && getMusicDao!!.getAllMusic().isEmpty()
        ) {

            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DURATION
            )
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {

                    val path = cursor.getString(0)
                    val name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val artist =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val duration =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                    var format: String? = null
                    if (duration != null) {
                        format = String.format(
                            "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                                    TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            duration.toLong()
                                        )
                                    )
                        )
                    } else {
                        format = "--/--"
                    }

//                    mmr.setDataSource(path)
//                    val data = mmr.embeddedPicture

                    val myMusic =
                        MyMusic(
                            name.toString(),
                            artist.toString(),
                            format,
                            path.toString(),
                            null
                        )

                    Log.d("AAAA", "name: $name, artist: $artist, duration: $format")

                    getMusicDao?.insertMusic(myMusic)

                }
                cursor.close()
            }
        }
    }

    private fun loadAdapter() {
        adapter?.setAdapter(data!!)
        binding.rv.adapter = adapter
    }

    private fun loadDatabase() {
        database = AppDatabase.get.getDatabase()
        getMusicDao = database!!.getMusicDao()
        getMusicDao?.deleteAllData()
    }

    private fun loadData() {
        data = ArrayList()
        getMusicDao?.deleteEmptyMusic("--/--")
        data = getMusicDao?.getAllMusic() as ArrayList
    }
}