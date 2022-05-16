package rwp.five.buyer.utilities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import rwp.five.buyer.LoginActivity
import rwp.five.buyer.Main

class SplashScreen : AppCompatActivity() {

    private lateinit var tinyDB: TinyDB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tinyDB = TinyDB(applicationContext)
        if (tinyDB.getBoolean("isLogged"))
            startActivity(Intent(this, Main::class.java))
        else
            startActivity(Intent(this, LoginActivity::class.java))
    }
}