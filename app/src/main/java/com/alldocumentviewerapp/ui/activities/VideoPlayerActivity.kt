package com.alldocumentviewerapp.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityVideoPlayerBinding
import com.alldocumentviewerapp.models.Videos
import com.alldocumentviewerapp.utils.Utils


class VideoPlayerActivity : AppCompatActivity() {
    lateinit var binding:ActivityVideoPlayerBinding
    private lateinit var player: ExoPlayer
    lateinit var videoUri:Uri
    private var playbackPosition: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Utils.statusBarColor(this)
        val bundle=intent.getParcelableExtra<Videos>("data")
        videoUri = bundle!!.videoUri


        if (::videoUri.isInitialized){
            initializePlayer(videoUri)
        }else{
            Toast.makeText(this@VideoPlayerActivity, "Video is missing.", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onResume() {
        super.onResume()
        initializePlayer(videoUri)
        player.seekTo(playbackPosition)
        player.play()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        player.release()
    }

    override fun onPause() {
        super.onPause()
        if (player.isPlaying) {
            playbackPosition = player.currentPosition
            player.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }


    private fun initializePlayer(videoUri:Uri) {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

}