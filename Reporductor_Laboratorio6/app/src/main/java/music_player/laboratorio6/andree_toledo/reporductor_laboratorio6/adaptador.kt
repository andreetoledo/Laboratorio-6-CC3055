package music_player.laboratorio6.andree_toledo.reporductor_laboratorio6
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.LinearLayout

class adaptador (c: Context, theCancionaccions: ArrayList<cancion_accion>) : BaseAdapter() {

    private var cancionaccions: ArrayList<cancion_accion> = theCancionaccions
    private var songInf: LayoutInflater? = LayoutInflater.from(c)

    override fun getCount(): Int {
        // TODO Auto-generated method stub
        return cancionaccions.size
    }

    override fun getItem(arg0: Int): Any? {
        // TODO Auto-generated method stub
        return null
    }

    override fun getItemId(arg0: Int): Long {
        // TODO Auto-generated method stub
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //map to canciones layout
        val songLay = songInf!!.inflate(R.layout.canciones, parent, false) as LinearLayout
        //get title and artist views
        val songView = songLay.findViewById<View>(R.id.song_title) as TextView
        val artistView = songLay.findViewById<View>(R.id.song_artist) as TextView
        //get canciones using position
        val currSong = cancionaccions!!.get(position)
        //get title and artist strings
        songView.text = currSong.title
        artistView.text = currSong.artist
        //set position as tag
        songLay.tag = position
        return songLay
    }

}