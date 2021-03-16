package uz.revolution.mymusic

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.revolution.mymusic.adapters.MusicAdapter
import uz.revolution.mymusic.daos.MusicDao
import uz.revolution.mymusic.database.AppDatabase
import uz.revolution.mymusic.databinding.FragmentListBinding
import uz.revolution.mymusic.models.MyMusic
import java.util.concurrent.TimeUnit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentListBinding
    private var data: ArrayList<MyMusic>? = null
    private var adapter: MusicAdapter? = null
    private var database: AppDatabase? = null
    private var getMusicDao: MusicDao? = null
    private var requestCode = 1

    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(layoutInflater, container, false)

        adapter = MusicAdapter()
        loadDatabase()
        checkPermission(binding.root.context)
        getAllMusic(binding.root.context)
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

    override fun onResume() {
        super.onResume()
        loadDatabase()
        getAllMusic(binding.root.context)
        loadData()
        loadAdapter()
    }

    private fun checkPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getAllMusic(context)
        } else {
            requestAudoiPermission()
        }
    }

    private fun requestAudoiPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val dialog = AlertDialog.Builder(binding.root.context)
            dialog.setTitle("Allow permission")
            dialog.setMessage("Needed permission to access device external storage")
            dialog.setPositiveButton(
                "OK"
            ) { p0, p1 ->



                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), requestCode
                )
                count+=1
                if (count == 2) {
                    goToSettings()
                }

                p0.cancel()
                checkPermission(binding.root.context)
            }
            dialog.show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), requestCode
            )
//            val dialog = AlertDialog.Builder(binding.root.context)
//            dialog.setTitle("Allow permission")
//            dialog.setMessage("Needed permission to access device external storage")
//            dialog.setPositiveButton(
//                "OK"
//            ) { p0, p1 ->
//
//                ActivityCompat.requestPermissions(
//                    requireActivity(), arrayOf(
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ), requestCode
//                )
//
//                p0.cancel()
//
//
//
//            }
//            dialog.show()
            checkPermission(binding.root.context)
        }
    }

    private fun getAllMusic(context: Context) {

        if (ContextCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                    val myMusic = MyMusic()

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

                    myMusic.name = name.toString()
                    myMusic.artist = artist.toString()
                    myMusic.path = path.toString()
                    myMusic.duration = format

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
    }

    private fun loadData() {
        data = ArrayList()
        getMusicDao?.deleteEmptyMusic("--/--")
        data = getMusicDao?.getAllMusic() as ArrayList
    }
    private fun goToSettings() {
        val myAppSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + requireActivity().getPackageName())
        )
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
        myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(myAppSettings)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}