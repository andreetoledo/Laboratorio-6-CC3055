package music_player.laboratorio6.andree_toledo.reporductor_laboratorio6

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.content.ContentUris
import android.os.Binder
import android.os.PowerManager
import android.util.Log
import java.util.Random
import android.app.Notification
import android.app.PendingIntent
import android.media.AudioAttributes






class servicios : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private val musicBind = MusicBinder()

    //media player
    private val player: MediaPlayer = MediaPlayer()
    //canciones list
    private var cancionaccions: ArrayList<cancion_accion> = ArrayList()
    //current position
    private var songPosn: Int = 0
    private var songTitle: String = ""
    private val NOTIFY_ID: Int = 1
    private var shuffle = false
    private var rand: Random? = null

    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()

        val notIntent = Intent(this@servicios, PrincipalReproductor::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder: Notification.Builder =  Notification.Builder(this@servicios)

        builder.setContentIntent(pendInt)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
        .setContentText(songTitle)
        val not:Notification = builder.build()

        startForeground(NOTIFY_ID, not)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    override fun onCreate() {
        super.onCreate()

        initMusicPlayer()
        rand = Random()
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    fun initMusicPlayer() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

        player.setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())

        player.setOnPreparedListener(this@servicios)
        player.setOnCompletionListener(this@servicios)
        player.setOnErrorListener(this@servicios)
    }

    fun setList(theCancionaccions: ArrayList<cancion_accion>) {
        cancionaccions = theCancionaccions
    }

    inner class MusicBinder : Binder() {
        internal val service: servicios
            get() = this@servicios
    }

    fun playSong() {

        player.reset()
        val playSong = cancionaccions[songPosn]
        songTitle = playSong.title
        val currSong = playSong.id
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currSong
        )

        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }

        player.prepareAsync()
    }

    fun setSong(songIndex: Int) {
        songPosn = songIndex
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    fun go() {
        player.start()
    }

    fun playPrev(){
        songPosn --
        if(songPosn < 0) songPosn = cancionaccions.size-1
        playSong()
    }

    fun playNext(){
            if(shuffle){
                var newSong = songPosn
                while(newSong==songPosn){
                    newSong=rand!!.nextInt(cancionaccions.size)
                }
                songPosn=newSong
            }
            else{
                songPosn++
                if(songPosn <= cancionaccions.size) songPosn=0
            }
            playSong()
    }

    fun setShuffle() {
        shuffle = !shuffle
    }


    override fun onCompletion(mp: MediaPlayer?) {
        if(player.currentPosition < 0){
            mp!!.reset()
            playNext()
        }
    }
}