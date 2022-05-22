package rwp.five.buyer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_otp.*
import rwp.five.buyer.utilities.TinyDB

class OTPActivity : AppCompatActivity() {

    lateinit var tinyDB: TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        tinyDB = TinyDB(this)
        setupOTPTextFieldsChangeListeners()

        btn_verify.setOnClickListener {
            if (!TextUtils.isEmpty(otp_1.text.toString()) && !TextUtils.isEmpty(
                    otp_2.text.toString()
                ) && !TextUtils.isEmpty(otp_3.text.toString()) && !TextUtils.isEmpty(
                    otp_4.text.toString()
                )
            ) {
                if (tinyDB.getString("otp") == (otp_1.text.toString() + otp_2.text.toString() + otp_3.text.toString() + otp_4.text.toString()))
                    startActivity(Intent(applicationContext, NewPasswordActivity::class.java))

            } else
                Toast.makeText(this, "Enter valid OTP code", Toast.LENGTH_LONG).show()


        }

    }

    private fun setupOTPTextFieldsChangeListeners() {

        otp_1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (otp_1.text.toString().length == 1) {
                    otp_2.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        otp_2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (otp_2.text.toString().length == 1) {
                    otp_3.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        otp_3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (otp_3.text.toString().length == 1) {
                    otp_4.requestFocus()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        otp_4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (!TextUtils.isEmpty(otp_1.text.toString()) and !TextUtils.isEmpty(
                        otp_2.text.toString()
                    ) and !TextUtils.isEmpty(
                        otp_3.text.toString()
                    ) and !TextUtils.isEmpty(otp_3.text.toString())
                ) {
                    val view: View? = this@OTPActivity.currentFocus
                    if (view != null) {
                        val imm =
                            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })


    }

}