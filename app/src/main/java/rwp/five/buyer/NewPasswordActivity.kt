package rwp.five.buyer

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_new_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB

class NewPasswordActivity : AppCompatActivity() {


    lateinit var tinyDB: TinyDB
    var hud: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)


        tinyDB = TinyDB(this)
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)


        btn_reset.setOnClickListener {

            when {

                TextUtils.isEmpty(password.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter new password",
                    Toast.LENGTH_SHORT
                ).show()
                TextUtils.isEmpty(repeat_password.text.toString()) -> Toast.makeText(
                    this,
                    "Please repeat the new password",
                    Toast.LENGTH_SHORT
                ).show()
                password.text.toString() != repeat_password.text.toString() -> Toast.makeText(
                    this,
                    "Password didn't matched",
                    Toast.LENGTH_SHORT
                ).show()
                else -> resetPassword()
            }

        }
    }

    private fun resetPassword() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().updateUser(

            postDetails = mutableMapOf(

                "userId" to tinyDB.getString("userId")!!,
                "password" to password.text.toString()

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
                            it.get("data").asString,
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
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