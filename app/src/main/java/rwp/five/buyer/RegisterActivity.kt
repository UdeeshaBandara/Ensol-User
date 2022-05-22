package rwp.five.buyer

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB

class RegisterActivity : AppCompatActivity() {


    lateinit var tinyDB: TinyDB
    var hud: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        tinyDB = TinyDB(this)
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        window?.statusBarColor =
            ContextCompat.getColor(this, R.color.red)


        back.setOnClickListener {
            onBackPressed()
        }

        btn_login.setOnClickListener {

            when {
                TextUtils.isEmpty(company_name.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter your company",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(email.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter your email",
                    Toast.LENGTH_SHORT
                ).show()
                !Patterns.EMAIL_ADDRESS.matcher(email.text).matches() -> Toast.makeText(
                    this,
                    "Please enter valid email address",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(mobile.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter your mobile",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(address.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter your address",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(password.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter password",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(repeat_password.text.toString()) -> Toast.makeText(
                    this,
                    "Please repeat the password",
                    Toast.LENGTH_SHORT
                ).show()
                password.text.toString() != repeat_password.text.toString() -> Toast.makeText(
                    this,
                    "Password didn't matched",
                    Toast.LENGTH_SHORT
                ).show()
                else -> getFcmToken()
            }

        }

    }

    private fun getFcmToken() {


        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isComplete) {

                    registerUser(it.result.toString())

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

    private fun registerUser(fcmToken: String) {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().register(
            postDetails = mutableMapOf(
                "name" to company_name.text.toString(),
                "email" to email.text.toString(),
                "telephone" to mobile.text.toString(),
                "address" to address.text.toString(),
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