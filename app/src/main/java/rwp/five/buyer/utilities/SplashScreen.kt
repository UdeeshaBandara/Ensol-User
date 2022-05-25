package rwp.five.buyer.utilities


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import rwp.five.buyer.LoginActivity
import rwp.five.buyer.Main
import rwp.five.buyer.R

class SplashScreen : AppCompatActivity() {
    lateinit var videoView: VideoView
    private lateinit var tinyDB: TinyDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        tinyDB = TinyDB(applicationContext)
        videoView = findViewById(R.id.video_view)

        try {

            videoView.setBackgroundColor(
                resources.getColor(android.R.color.white)
            )
            videoView.setZOrderOnTop(true)


            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER
            videoView.layoutParams = params
            val video: Uri =
                Uri.parse("android.resource://" + packageName + "/" + R.raw.splash_video)
            videoView.setVideoURI(video)
            videoView.setOnCompletionListener { jump() }
            videoView.start()
        } catch (ex: Exception) {
            jump()
        }

//        tinyDB.putString("token","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoibWFyY29zLmhlbnJpLmNvbWEiLCJmY20iOiJhcyIsIm5hbWUiOiJ1bmRlZmluZWQgdW5kZWZpbmVkIiwicmVmcmVzaEtleSI6ImhMR2wwT3dhRFliQ01tYjcrYXhlZWc9PSIsImlhdCI6MTY1MTk5MDA5MX0.NqoSfgsfelHHfNQm0s7uSS6tUzMjaOWcnGCGJp9Fza4")

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        jump()
        return true
    }

    private fun jump() {
        if (isFinishing) return
        if (tinyDB.getBoolean("isLogged"))
            startActivity(Intent(this, Main::class.java))
        else
            startActivity(Intent(this, LoginActivity::class.java))

        finishAffinity()
    }
}