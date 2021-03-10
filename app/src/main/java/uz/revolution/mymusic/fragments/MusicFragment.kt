package uz.revolution.mymusic.fragments

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import uz.revolution.mymusic.R
import uz.revolution.mymusic.daos.MusicDao
import uz.revolution.mymusic.database.AppDatabase
import uz.revolution.mymusic.databinding.FragmentMusicBinding
import uz.revolution.mymusic.models.MyMusic
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class MusicFragment : Fragment() {

    private var myMusic: MyMusic? = null
    private var position: Int? = null
    private var size: Int? = null
    private var mediaPlayer: MediaPlayer? = null
    private var data: ArrayList<MyMusic>? = null
    private var database: AppDatabase? = null
    private var getMusicDao: MusicDao? = null
    private var music: MyMusic? = null
    private var currentPosition = 0
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            myMusic = it.getSerializable(ARG_PARAM1) as MyMusic?
            position = it.getInt(ARG_PARAM2)
            size = it.getInt(ARG_PARAM3)
        }
        database = AppDatabase.get.getDatabase()
        getMusicDao = database!!.getMusicDao()
    }

    lateinit var binding: FragmentMusicBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(layoutInflater, container, false)
        handler = Handler(Looper.getMainLooper())
        loadData()
        loadDataToView()
        playMusic(currentPosition)
        playClick()
        replay30Click()
        forward30Click()
        nextClick()
        previousClick()
        menuClick()
        seekBarChanging()
        mediaEnded()

        return binding.root
    }

    private fun mediaEnded() {
        mediaPlayer!!.setOnCompletionListener {
            binding.playPause.setImageResource(R.drawable.ic_play2)
        }
    }


    private fun seekBarChanging() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mediaPlayer?.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })
    }

    private var runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                binding.seekbar.progress = mediaPlayer!!.currentPosition
                handler.postDelayed(this, 100)
                setProgress(mediaPlayer!!.currentPosition)
            }
        }

    }

    private fun setProgress(duration: Int) {
        val format = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            duration.toLong()
                        )
                    )
        )
        binding.currentDuration.text = format
    }

    private fun menuClick() {
        binding.menu.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun previousClick() {
        binding.previous.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                mediaPlayer = null
                currentPosition--
                playMusic(currentPosition)
                loadDataToView()
            } else {
                mediaPlayer = null
                currentPosition--
                playMusic(currentPosition)
                loadDataToView()
            }
        }
    }

    private fun realiseMp() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer!!.stop()
                mediaPlayer == null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun nextClick() {
        binding.next.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                mediaPlayer = null
                currentPosition++
                playMusic(currentPosition)
                loadDataToView()
            } else {
                mediaPlayer = null
                currentPosition++
                playMusic(currentPosition)
                loadDataToView()
            }

        }
    }

    private fun forward30Click() {
        binding.forward30.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(30000)!!)
            } else {
                mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(30000)!!)
                mediaPlayer?.start()
                binding.playPause.setImageResource(R.drawable.ic_pause_circle_filled)
            }
        }
    }

    private fun replay30Click() {
        binding.replay30.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.seekTo(mediaPlayer?.currentPosition?.minus(30000)!!)
            } else {
                mediaPlayer!!.seekTo(mediaPlayer?.currentPosition?.minus(30000)!!)
                mediaPlayer!!.start()
                binding.playPause.setImageResource(R.drawable.ic_pause_circle_filled)
            }
        }
    }

    private fun loadData() {
        data = ArrayList()
        data = getMusicDao?.getAllMusic() as ArrayList
        music = data!![position!!]
        currentPosition = position as Int
    }

    private fun playClick() {
        binding.playPause.setOnClickListener {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.pause()
                    binding.playPause.setImageResource(R.drawable.ic_play2)
                } else {
                    mediaPlayer?.start()
                    binding.playPause.setImageResource(R.drawable.ic_pause_circle_filled)
                }
            } else {
                playMusic(currentPosition)
            }
        }
    }

    private fun playMusic(position: Int) {

        if (position < data!!.size && position >= 0) {
            if (mediaPlayer == null) {
                mediaPlayer =
                    MediaPlayer.create(binding.root.context, Uri.parse(data!![position].path))
                mediaPlayer?.start()
                binding.playPause.setImageResource(R.drawable.ic_pause_circle_filled)
                binding.seekbar.max = mediaPlayer?.duration!!
                handler.postDelayed(runnable, 100)
            }
        } else {
            currentPosition = 0
            playMusic(currentPosition)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun loadDataToView() {
        if (currentPosition < data!!.size && currentPosition>=0) {
            binding.position.text = "${currentPosition + 1}/$size"
            binding.name.text = data!![currentPosition].name
            binding.artist.text = data!![currentPosition].artist
            binding.duration.text = data!![currentPosition].duration
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realiseMp()
    }
}
