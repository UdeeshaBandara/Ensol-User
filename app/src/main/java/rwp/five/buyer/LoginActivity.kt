package rwp.five.buyer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        register.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btn_login.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
        }

        getFcmToken()
    }

    private fun getFcmToken() {

        Log.e("hi", "fcm")

        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isComplete) {

                    Log.e("fcm", it.result.toString())


                } else {
                    Toast.makeText(
                        this,
                        "Something went wrong. Please try again",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}