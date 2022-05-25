package rwp.five.buyer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getColor
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

class OrderFragment : Fragment() {

    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB

    var orders = JsonArray()
    var orderMachines = JsonArray()

    var selectedOrderId = ""
    var selectedMachineId = ""
    var isCurrentOrder = true

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
//            past_tick.visibility = View.INVISIBLE
//            current_tick.visibility = View.VISIBLE
            isCurrentOrder = true
            getCurrentOrders()
        }
        past.setOnClickListener {
//            current_tick.visibility = View.INVISIBLE
//            past_tick.visibility = View.VISIBLE
            past.setBackgroundColor(resources.getColor(R.color.black))
            current.setBackgroundColor(resources.getColor(R.color.white))
            past.setTextColor(resources.getColor(R.color.white))
            current.setTextColor(resources.getColor(R.color.black))
            current.alpha = 0.3f
            past.alpha = 1f
            isCurrentOrder = false
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
        var endDate: TextView = itemView.findViewById(R.id.end_date)
        var orderStatus: RelativeLayout = itemView.findViewById(R.id.order_status)
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

            holder.order_no.text =
                "Order #ZES" + orders.get(position).asJsonObject.get("id").asString
            holder.total.text = "Total : LKR " + String.format(
                "%.2f",
                orders.get(position).asJsonObject.get("price").asDouble
            )

            when (orders.get(position).asJsonObject.get("orderStatus").asString) {
                "0" -> {
                    holder.orderStatus.setBackgroundColor(
                        getColor(
                            requireActivity(),
                            R.color.cancel
                        )
                    )
                    holder.endDate.text = "Cancelled"
                }
                "1" -> {
                    holder.orderStatus.setBackgroundColor(
                        getColor(
                            requireActivity(),
                            R.color.complete
                        )
                    )
                    holder.endDate.text = "Completed"
                }
                "2" -> {
                    holder.orderStatus.setBackgroundColor(
                        getColor(
                            requireActivity(),
                            R.color.ongoing
                        )
                    )
                    holder.endDate.text = "Ongoing"
                }
                "3" -> {
                    holder.orderStatus.setBackgroundColor(
                        getColor(
                            requireActivity(),
                            R.color.pending
                        )
                    )
                    holder.endDate.text = "Pending"
                }
            }


            holder.view_machines.setOnClickListener {
                selectedOrderId = orders.get(position).asJsonObject.get("id").asString
                orderMachines = orders.get(position).asJsonObject.get("machines").asJsonArray
                createOrderMachineDetailsPopup(holder.order_no.text.toString())
            }
        }

    }

    inner class OrderMachineHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card_root: CardView = itemView.findViewById(R.id.card_root)
        var machine_title: TextView = itemView.findViewById(R.id.machine_title)
        var machine_price: TextView = itemView.findViewById(R.id.machine_price)
        var qty: TextView = itemView.findViewById(R.id.qty)
        var machine_image: ImageView = itemView.findViewById(R.id.machine_image)
        var repair: Button = itemView.findViewById(R.id.repair)
        var contractStartDate: TextView = itemView.findViewById(R.id.contract_start_date)
        var contractEndDate: TextView = itemView.findViewById(R.id.contract_end_date)


    }

    inner class OrderMachineAdapter : RecyclerView.Adapter<OrderMachineHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): OrderMachineHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_machine, parent, false)

            return OrderMachineHolder(view)
        }

        override fun getItemCount(): Int {

            return orderMachines.size()
        }


        override fun onBindViewHolder(holder: OrderMachineHolder, position: Int) {

            if(isCurrentOrder)
                holder.repair.visibility = View.VISIBLE
            else
                holder.repair.visibility = View.GONE


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
            holder.contractStartDate.text =
                "Contract Start Date : " + orderMachines.get(position).asJsonObject.get("OrderMachines").asJsonObject.get(
                    "contractStartDate"
                ).asString.substring(0, 10)
            holder.contractEndDate.text =
                "Contract End Date   : " + orderMachines.get(position).asJsonObject.get("OrderMachines").asJsonObject.get(
                    "contractEndDate"
                ).asString.substring(0, 10)

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


            holder.repair.setOnClickListener {
                selectedMachineId = orderMachines.get(position).asJsonObject.get("id").asString
                createRepairSubmitPopup()
            }

        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden)
            requireActivity().window?.statusBarColor =
                getColor(requireActivity(), R.color.white)
        else {
            requireActivity().window?.statusBarColor =
                getColor(requireActivity(), R.color.red)
            current.performClick()
        }


    }

    private fun createOrderMachineDetailsPopup(orderNo: String) {


        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_view_machines)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ((resources.displayMetrics.heightPixels * 0.60).toInt())
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btn_ok: TextView = dialog.findViewById(R.id.btn_ok)

        val order_no: TextView = dialog.findViewById(R.id.order_no)
        val recycler_order_machines: RecyclerView =
            dialog.findViewById(R.id.recycler_order_machines)
        order_no.text = orderNo

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

    private fun createRepairSubmitPopup() {


        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_repair)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val description: TextView = dialog.findViewById(R.id.description)
        val btnSubmit: TextView = dialog.findViewById(R.id.btn_submit)


        btnSubmit.setOnClickListener {

            if (TextUtils.isEmpty(description.text.toString()))
                Toast.makeText(
                    requireActivity(),
                    "Please enter issue",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                submitARepairRequest(description.text.toString())
                dialog.dismiss()
            }

        }

        dialog.show()

    }

    private fun submitARepairRequest(description: String) {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().submitARepairRequest(
            "Bearer ${tinyDB.getString("token")}",
            postDetails = mutableMapOf(
                "description" to description,
                "orderId" to selectedOrderId,
                "machineId" to selectedMachineId
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