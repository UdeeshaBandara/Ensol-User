package rwp.five.buyer

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.CoreApp.Companion.cartDao
import rwp.five.buyer.utilities.TinyDB

class AccountFragment : Fragment() {

    var accountLabelArray = arrayOf(

        "Repair Requests",
        "Account",
        "Contact Us",
        "Log out"
    )
    var accountImageArray = arrayOf(

        "information",
        "user",

        "call",
        "logout"
    )

    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tinyDB = TinyDB(requireActivity())
        hud = KProgressHUD.create(requireActivity())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        recycler_account.adapter = CategoryAdapter()

        recycler_account.layoutManager = GridLayoutManager(activity?.applicationContext, 2)

        getUserDetails()
    }


    inner class AccountItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.findViewById(R.id.title)
        var icon: ImageView = itemView.findViewById(R.id.icon)
        var card_root: CardView = itemView.findViewById(R.id.card_root)


    }

    inner class CategoryAdapter : RecyclerView.Adapter<AccountItemHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AccountItemHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_account, parent, false)

            return AccountItemHolder(view)
        }

        override fun getItemCount(): Int {

            return accountLabelArray.size
        }


        override fun onBindViewHolder(holder: AccountItemHolder, position: Int) {


            holder.title.text = accountLabelArray[position]
            holder.icon.setImageDrawable(
                getDrawable(
                    activity?.applicationContext!!,
                    resources.getIdentifier(
                        "@drawable/" + accountImageArray[position],
                        null,
                        activity?.applicationContext?.packageName
                    )
                )
            )
            holder.card_root.setOnClickListener {

                when {
                    accountLabelArray[position] == "Repair Requests" -> startActivity(
                        Intent(
                            requireActivity(),
                            ViewRepairActivity::class.java
                        )
                    )
                    accountLabelArray[position] == "Account" -> (activity as Main).userDetailUpdateLauncher.launch(
                        Intent(
                            requireActivity(),
                            UserActivity::class.java
                        )
                    )
                    accountLabelArray[position] == "Contact Us" -> {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:0333330873")
                        startActivity(intent)

                    }
                    accountLabelArray[position] == "Log out" -> createLogoutPopup()
                }

            }

        }

    }


    private fun createLogoutPopup() {


        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_log_out)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = dialog.findViewById(R.id.cancel)
        val btnConfirm: TextView = dialog.findViewById(R.id.btn_confirm)


        btnConfirm.setOnClickListener {
            dialog.dismiss()
            revokeFCMToken()


        }
        cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    private fun revokeFCMToken() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().revokeFCMToken(
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

                        tinyDB.clear()
                        lifecycleScope.launch(Dispatchers.IO) {
                            cartDao?.clearCart()
                        }
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finishAffinity()

                    } else
                        Toast.makeText(
                            requireContext(),
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

    fun getUserDetails() {

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

                        company_name.text = it.getAsJsonObject("data").get("name").asString
                        address.text = it.getAsJsonObject("data").get("address").asString

                    } else
                        Toast.makeText(
                            requireActivity(),
                            it.get("data").asString,
                            Toast.LENGTH_SHORT
                        ).show()


                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e("fail", t.message.toString())
                hideHUD()
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden)
            requireActivity().window?.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.white)
        else
            requireActivity().window?.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.red)


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