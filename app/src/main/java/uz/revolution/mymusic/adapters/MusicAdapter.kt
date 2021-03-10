package uz.revolution.mymusic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.revolution.mymusic.R
import uz.revolution.mymusic.databinding.ItemMusicBinding
import uz.revolution.mymusic.models.MyMusic

class MusicAdapter : RecyclerView.Adapter<MusicAdapter.VH>() {

    private var data: ArrayList<MyMusic>? = null
    var onItemCLick:OnItemCLick?=null

    fun setAdapter(data: ArrayList<MyMusic>) {
        this.data = data
    }

    inner class VH(var itemMusicBinding: ItemMusicBinding) : RecyclerView.ViewHolder(itemMusicBinding.root) {
        fun onBind(myMusic: MyMusic,position: Int){
            itemMusicBinding.image.setImageResource(R.drawable.hammali)
            itemMusicBinding.name.text = myMusic.name
            itemMusicBinding.artist.text=myMusic.artist
            itemMusicBinding.duration.text=myMusic.duration

            itemMusicBinding.root.setOnClickListener {
                if (onItemCLick != null) {
                    onItemCLick!!.onClick(myMusic,position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemMusicBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(data!![position],position)
    }

    override fun getItemCount(): Int = data!!.size

    interface OnItemCLick{
        fun onClick(myMusic: MyMusic,position: Int)
    }
}