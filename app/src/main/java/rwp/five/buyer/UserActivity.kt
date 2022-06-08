package rwp.five.buyer

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.activity_user.address
import kotlinx.android.synthetic.main.activity_user.company_name
import kotlinx.android.synthetic.main.activity_user.email
import kotlinx.android.synthetic.main.activity_user.mobile
import kotlinx.android.synthetic.main.activity_user.repeat_password
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB

class UserActivity : AppCompatActivity() {
    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        window?.statusBarColor =
            ContextCompat.getColor(this, R.color.red)

        tinyDB = TinyDB(this)
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        getUser()

        btn_update.setOnClickListener {


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
                TextUtils.isEmpty(current_password.text.toString()) -> Toast.makeText(
                    this,
                    "Please enter password",
                    Toast.LENGTH_SHORT
                ).show()
                (!TextUtils.isEmpty(new_password.text.toString()) || !TextUtils.isEmpty(
                    repeat_password.text.toString()
                )) -> {

                    when {
                        TextUtils.isEmpty(new_password.text.toString()) -> Toast.makeText(
                            this,
                            "Please enter new password",
                            Toast.LENGTH_SHORT
                        ).show()
                        TextUtils.isEmpty(repeat_password.text.toString()) -> Toast.makeText(
                            this,
                            "Please repeat the new password",
                            Toast.LENGTH_SHORT
                        ).show()
                        new_password.text.toString() != repeat_password.text.toString() -> Toast.makeText(
                            this,
                            "Password didn't matched",
                            Toast.LENGTH_SHORT
                        ).show()
                        else
                        -> updateUser()
                    }

                }

                else -> updateUser()
            }

        }

    }

    private fun getUser() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getUser(
            "Bearer ${tinyDB.getString("token")}"
        )

        apiInterface.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                hideHUD()

                response.body()?.let {


                    if (it.get("status").asBoolean) {

                        company_name.setText(it.getAsJsonObject("data").get("name").asString)
                        email.setText(it.getAsJsonObject("data").get("email").asString)
                        mobile.setText(it.getAsJsonObject("data").get("telephone").asString)
                        address.setText(it.getAsJsonObject("data").get("address").asString)

                    } else
                        Toast.makeText(
                            this@UserActivity,
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

    private fun updateUser() {

        showHUD()

        val postDetails: MutableMap<String, String>

        if (TextUtils.isEmpty(new_password.text.toString()) && TextUtils.isEmpty(repeat_password.text.toString())) {

            postDetails = mutableMapOf(
                "name" to company_name.text.toString(),
                "email" to email.text.toString(),
                "telephone" to mobile.text.toString(),
                "address" to address.text.toString(),
                "password" to current_password.text.toString()

            ) as HashMap<String, String>

        } else {
            postDetails = mutableMapOf(
                "name" to company_name.text.toString(),
                "email" to email.text.toString(),
                "telephone" to mobile.text.toString(),
                "address" to address.text.toString(),
                "password" to current_password.text.toString(),
                "newPassword" to new_password.text.toString()

            ) as HashMap<String, String>
        }

        val apiInterface: Call<JsonObject> = ApiInterface.create().updateUserDetails(
            "Bearer ${tinyDB.getString("token")}",
            postDetails = postDetails
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
                        tinyDB.putString("token", it.get("accessToken").asString)
                        setResult(Activity.RESULT_OK)

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