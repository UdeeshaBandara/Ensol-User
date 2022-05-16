package rwp.five.buyer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.fragment_order.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB
import java.lang.String

class OrderFragment : Fragment() {

    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB

    var orders = JsonArray()
    var orderMachines = JsonArray()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tinyDB = TinyDB(requireActivity())
        hud = KProgressHUD.create(requireActivity())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        current.setOnClickListener {
            current.setBackgroundColor(resources.getColor(R.color.black))
            past.setBackgroundColor(resources.getColor(R.color.white))
            current.setTextColor(resources.getColor(R.color.white))
            past.setTextColor(resources.getColor(R.color.black))
            past.alpha = 0.3f
            current.alpha = 1f
            past_tick.visibility = View.INVISIBLE
            current_tick.visibility = View.VISIBLE
            getCurrentOrders()
        }
        past.setOnClickListener {
            current_tick.visibility = View.INVISIBLE
            past_tick.visibility = View.VISIBLE
            past.setBackgroundColor(resources.getColor(R.color.black))
            current.setBackgroundColor(resources.getColor(R.color.white))
            past.setTextColor(resources.getColor(R.color.white))
            current.setTextColor(resources.getColor(R.color.black))
            current.alpha = 0.3f
            past.alpha = 1f
            getPastOrders()

        }

        recycler_orders.adapter = CurrentOrderAdapter()
        recycler_orders.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        getCurrentOrders()

    }

    inner class CurrentOrderItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var order_no: TextView = itemView.findViewById(R.id.order_no)
        var total: TextView = itemView.findViewById(R.id.total)
        var end_date: TextView = itemView.findViewById(R.id.end_date)
        var view_machines: TextView = itemView.findViewById(R.id.view_machines)


    }

    inner class CurrentOrderAdapter : RecyclerView.Adapter<CurrentOrderItemHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CurrentOrderItemHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_current_order, parent, false)

            return CurrentOrderItemHolder(view)
        }

        override fun getItemCount(): Int {

            return orders.size()
        }


        override fun onBindViewHolder(holder: CurrentOrderItemHolder, position: Int) {

            holder.order_no.text = "OR " + orders.get(position).asJsonObject.get("id").asString
            holder.total.text = "Total : LKR " + String.format(
                "%.2f",
                orders.get(position).asJsonObject.get("price").asDouble
            )
//            holder.end_date.setText( orders.get(position).asJsonObject.get("").asDouble)
            holder.view_machines.setOnClickListener {
                orderMachines = orders.get(position).asJsonObject.get("machines").asJsonArray
                createOrderMachineDetailsPopup()
            }
        }

    }

    inner class OrderMachineHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card_root: CardView = itemView.findViewById(R.id.card_root)
        var machine_title: TextView = itemView.findViewById(R.id.machine_title)
        var machine_price: TextView = itemView.findViewById(R.id.machine_price)
        var qty: TextView = itemView.findViewById(R.id.qty)
        var machine_image: ImageView = itemView.findViewById(R.id.machine_image)

        init {
            qty.visibility = View.VISIBLE
        }

    }

    inner class OrderMachineAdapter : RecyclerView.Adapter<OrderMachineHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): OrderMachineHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home, parent, false)

            return OrderMachineHolder(view)
        }

        override fun getItemCount(): Int {

            return orderMachines.size()
        }


        override fun onBindViewHolder(holder: OrderMachineHolder, position: Int) {

            holder.machine_title.text =
                orderMachines.get(position).asJsonObject.get("machineType").asString

            holder.machine_price.text = "Total : LKR " + String.format(
                "%.2f",
                orderMachines.get(position).asJsonObject.get("rentPrice").asDouble
            )

            holder.qty.text =
                "Quantity : " + orderMachines.get(position).asJsonObject.get("OrderMachines").asJsonObject.get(
                    "quantity"
                ).asString

            val jsonArray =
                JsonParser().parse(
                    orderMachines.get(position).asJsonObject.get("images").asString
                ) as JsonArray
            if (jsonArray.size() > 0)
                Glide.with(activity!!)
                    .load(
                        jsonArray.get(0).asString

                    ).fitCenter()
                    .into(holder.machine_image)

        }

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

    private fun createOrderMachineDetailsPopup(
    ) {


        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_view_machines)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btn_ok: TextView = dialog.findViewById(R.id.btn_ok)
        val date: TextView = dialog.findViewById(R.id.date)
        val order_no: TextView = dialog.findViewById(R.id.order_no)
        val recycler_order_machines: RecyclerView =
            dialog.findViewById(R.id.recycler_order_machines)


        recycler_order_machines.adapter = OrderMachineAdapter()
        recycler_order_machines.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )

        btn_ok.setOnClickListener {


            dialog.dismiss()
        }

        dialog.show()

    }

    private fun getPastOrders() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getPastOrders(
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

                        orders = it.getAsJsonArray("data")
                        recycler_orders.adapter?.notifyDataSetChanged()

                    } else
                        Toast.makeText(
                            requireContext(),
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

    private fun getCurrentOrders() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getCurrentOrders(
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

                        orders = it.getAsJsonArray("data")
                        recycler_orders.adapter?.notifyDataSetChanged()

                    } else
                        Toast.makeText(
                            requireContext(),
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