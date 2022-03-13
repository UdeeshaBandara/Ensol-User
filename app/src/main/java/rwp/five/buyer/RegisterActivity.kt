package rwp.five.buyer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        window?.statusBarColor =
            ContextCompat.getColor(this, R.color.red)
        back.setOnClickListener {
            onBackPressed()
        }
    }
}