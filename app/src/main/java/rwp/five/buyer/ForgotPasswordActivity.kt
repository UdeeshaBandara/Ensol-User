package rwp.five.buyer

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_forgot_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var tinyDB: TinyDB
    var hud: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)


        tinyDB = TinyDB(this)
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        btn_reset.setOnClickListener {

            if (TextUtils.isEmpty(email.text.toString()))
                Toast.makeText(
                    this,
                    "Please enter your email",
                    Toast.LENGTH_SHORT
                ).show()
            else if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches())
                Toast.makeText(
                    this,
                    "Please enter valid email address",
                    Toast.LENGTH_SHORT
                ).show()
            else
                requestOTP()

        }


    }

    private fun requestOTP() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().requestOTP(
            postDetails = mutableMapOf(

                "email" to email.text.toString()
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
                        Toast.makeText(
                            applicationContext,
                            it.get("message").asString,
                            Toast.LENGTH_SHORT
                        ).show()
                        tinyDB.putString("otp", it.getAsJsonObject("data").get("OTP").asString)
                        tinyDB.putString("userId", it.getAsJsonObject("data").get("userId").asString)
                        startActivity(Intent(applicationContext, OTPActivity::class.java))

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