package rwp.five.buyer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_view_repair.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_order.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.CoreApp.Companion.tinyDB

class ViewRepairActivity : AppCompatActivity() {

    var repairs = JsonArray()
    var hud: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_repair)
        window?.statusBarColor =
            ContextCompat.getColor(this, R.color.red)

        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        recycler_repairs.adapter = CategoryAdapter()
        recycler_repairs.layoutManager = LinearLayoutManager(
         this,
            LinearLayoutManager.VERTICAL,
            false
        )
        getAllRepairs()
    }

    inner class RepairItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var orderNo: TextView = itemView.findViewById(R.id.order_no)
        var repairId: TextView = itemView.findViewById(R.id.repair_id)
        var repairStatus: TextView = itemView.findViewById(R.id.repair_status)
        var rltStatus: RelativeLayout = itemView.findViewById(R.id.rlt_status)
        var repairDate: TextView = itemView.findViewById(R.id.repair_date)
        var machine: TextView = itemView.findViewById(R.id.machine)
        var description: TextView = itemView.findViewById(R.id.description)


    }

    inner class CategoryAdapter : RecyclerView.Adapter<RepairItemHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RepairItemHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_repair, parent, false)

            return RepairItemHolder(view)
        }

        override fun getItemCount(): Int {

            return repairs.size()
        }


        override fun onBindViewHolder(holder: RepairItemHolder, position: Int) {

            holder.repairId.text = "Repair #ENR" + repairs[position].asJsonObject.get("id").asString
            holder.orderNo.text =
                "Order #ZES" + repairs[position].asJsonObject.get("orderId").asString
            holder.machine.text =repairs[position].asJsonObject.get("machine").asJsonObject.get("machineType").asString

            when (repairs[position].asJsonObject.get("status").asString) {
                "0" -> {
                    holder.rltStatus.setBackgroundColor(getColor(R.color.cancel))
                    holder.repairStatus.text = "Cancelled"
                }
                "1" -> {
                    holder.rltStatus.setBackgroundColor(getColor(R.color.complete))
                    holder.repairStatus.text = "Completed"
                }
                "2" -> {
                    holder.rltStatus.setBackgroundColor(getColor(R.color.ongoing))
                    holder.repairStatus.text = "Ongoing"
                }
            }
            holder.repairDate.text =
                repairs[position].asJsonObject.get("createdAt").asString.substring(0, 10)
            holder.description.text = repairs[position].asJsonObject.get("description").asString



        }

    }

    private fun getAllRepairs() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getAllRepairs(
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

                        repairs = it.getAsJsonArray("data")
                        recycler_repairs.adapter?.notifyDataSetChanged()

                    } else
                        Toast.makeText(
                            this@ViewRepairActivity,
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