package rwp.five.buyer.utilities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import rwp.five.buyer.LoginActivity

class SplashScreen : AppCompatActivity() {

    private lateinit var tinyDB: TinyDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tinyDB = TinyDB(applicationContext)

        startActivity(Intent(this, LoginActivity::class.java))
    }
}