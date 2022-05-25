package rwp.five.buyer

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB

class LoginActivity : AppCompatActivity() {

    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tinyDB = TinyDB(this)
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        register.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))
        }
        btn_login.setOnClickListener {

            when {
                TextUtils.isEmpty(email.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter your email",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(password.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter password",
                    Toast.LENGTH_SHORT
                ).show()
                else -> getFcmToken()
            }
        }
        forget_password.setOnClickListener {

            startActivity(Intent(this,ForgotPasswordActivity::class.java))

        }

//        getFcmToken()
    }

    private fun getFcmToken() {


        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isComplete) {

                    Log.e("fcm", it.result.toString())
                    authUser(it.result.toString())

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

    private fun authUser(fcmToken: String) {

        showHUD()
        val apiInterface: Call<JsonObject> = ApiInterface.create().login(
            postDetails = mutableMapOf(
                "email" to email.text.toString(),
                "password" to password.text.toString(),
                "fcm" to fcmToken
            ) as HashMap<String, String>
        )


        apiInterface.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                hideHUD()
                response.body()?.let {


                    if (it.get("status").asBoolean) {
                        tinyDB.putString("token", it.get("accessToken").asString)
                        tinyDB.putBoolean("isLogged", true)
                        tinyDB.putBoolean("isEmptyCart", true)
                        startActivity(Intent(applicationContext, Main::class.java))
                        finishAffinity()
                    } else
                        Toast.makeText(
                            applicationContext,
                            it.get("data").asString,
                            Toast.LENGTH_SHORT
                        ).show()


                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                hideHUD()
                Log.e("fail", t.message.toString())
            }
        })
    }

    private fun showHUD() {
        if (hud!!.isShowing) {
            hud!!.dismiss()
        }
        hud!!.show()
    }

    private fun hideHUD() {
        if (hud!!.isShowing) {
            hud!!.dismiss()
        }
    }
}