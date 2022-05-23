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
        tinyDB.putString("token","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoibWFyY29zLmhlbnJpLmNvbWEiLCJmY20iOiJhcyIsIm5hbWUiOiJ1bmRlZmluZWQgdW5kZWZpbmVkIiwicmVmcmVzaEtleSI6ImhMR2wwT3dhRFliQ01tYjcrYXhlZWc9PSIsImlhdCI6MTY1MTk5MDA5MX0.NqoSfgsfelHHfNQm0s7uSS6tUzMjaOWcnGCGJp9Fza4")
        if (!tinyDB.getBoolean("isLogged"))
            startActivity(Intent(this, Main::class.java))
        else
            startActivity(Intent(this, LoginActivity::class.java))

        finishAffinity()
    }
}