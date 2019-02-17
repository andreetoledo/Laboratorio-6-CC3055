package music_player.laboratorio6.andree_toledo.reporductor_laboratorio6


import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.Collections
import android.widget.ListView
import kotlin.collections.ArrayList
import android.widget.MediaController.MediaPlayerControl
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager
import android.database.Cursor
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View;
import android.widget.MediaController
import music_player.laboratorio6.andree_toledo.reporductor_laboratorio6.servicios.MusicBinder


class PrincipalReproductor : AppCompatActivity(), MediaPlayerControl {

    private var cancionaccionList: ArrayList<cancion_accion> = ArrayList()
    private var songView: ListView? = null
    private var musicSrv: servicios? = servicios()
    private var playIntent: Intent? = Intent()
    private var musicBound = false
    private var controller: MediaController? = null
    private var paused = false
    private var playbackPaused = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.PrincipalReproductor)

        songView = findViewById(R.id.song_list)
        getSongList()

        controller = MediaController(this@PrincipalReproductor)

        Collections.sort(cancionaccionList) { a, b -> a.title.compareTo(b.title) }

        val songAdt = adaptador(this@PrincipalReproductor, cancionaccionList)
        songView!!.adapter = songAdt

        setController();

    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, servicios::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null
        super.onDestroy()
    }

    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            musicSrv = binder.service
            musicSrv!!.setList(cancionaccionList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    fun getSongList() {
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        if (ContextCompat.checkSelfPermission(this@PrincipalReproductor,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this@PrincipalReproductor,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this@PrincipalReproductor,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    112
                    )

            }
        } else {
            val musicCursor: Cursor? = musicResolver.query(musicUri, null, null, null, null)

            if (musicCursor != null && musicCursor.moveToFirst()) {
                val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
                do {
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    cancionaccionList.add(cancion_accion(thisId, thisTitle, thisArtist))
                } while (musicCursor.moveToNext())
            }

            musicCursor!!.close()
        }
    }

    private fun setController() {
        controller = Controlador(this);

        controller!!.setPrevNextListeners(
            View.OnClickListener { playNext() },
            View.OnClickListener { playPrev() })

        controller!!.setMediaPlayer(this);
        controller!!.setAnchorView(findViewById(R.id.song_list));
        controller!!.setEnabled(true);

    }


    override fun isPlaying(): Boolean {
        if(musicSrv!=null && musicBound)
        return musicSrv!!.isPng();
        return false
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

     override fun getDuration(): Int {
        if(musicSrv!=null && musicBound && musicSrv!!.isPng())
        return musicSrv!!.getDur();
        else return 0;
    }

    override fun getBufferPercentage(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    override fun getCurrentPosition(): Int {
        return if (musicSrv != null && musicBound && musicSrv!!.isPng())
            musicSrv!!.getPosn()
        else
            0
    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun start() {
        musicSrv!!.go()
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller!!.hide()
        super.onStop()
    }

    private fun playNext() {
        musicSrv!!.playNext()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show(0)
    }

    private fun playPrev() {
        musicSrv!!.playPrev()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show(0)
    }

    fun songPicked(view: View) {
        musicSrv!!.setSong(Integer.parseInt(view.tag.toString()))
        musicSrv!!.playSong()
        if (playbackPaused) {
            setController()
            playbackPaused = false
        }
        controller!!.show(0)
    }

    override fun pause() {
        playbackPaused = true
        musicSrv!!.pausePlayer()
    }

}
