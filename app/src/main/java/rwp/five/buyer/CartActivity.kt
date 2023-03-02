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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kaopiz.kprogresshud.KProgressHUD
import com.makeramen.roundedimageview.RoundedImageView
import dev.joshhalvorson.calendar_date_range_picker.calendar.CalendarPicker
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.room.model.CartProduct
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.CoreApp.Companion.cartDao
import rwp.five.buyer.utilities.CoreApp.Companion.getDateFromTimestamp
import rwp.five.buyer.utilities.CoreApp.Companion.getNoOfDays
import rwp.five.buyer.utilities.TinyDB
import java.text.SimpleDateFormat
import java.util.*


class CartActivity : AppCompatActivity() {
    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB
    var cartItems: MutableList<CartProduct> = mutableListOf()

    var total = 0.0
    var quantity = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


        window?.statusBarColor =
            ContextCompat.getColor(this, R.color.red)


        tinyDB = TinyDB(this)
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        recycler_cart.adapter = CartAdapter()
        recycler_cart.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                cartItems = cartDao?.getAll()!!
            }

            cartItems.forEach {
                total += it.productPrice?.times(it.quantity!!)!!.times(
                    getNoOfDays(
                        it.productContractEnd!!.toLong(),
                        it.productContractStart!!.toLong()
                    )
                )
            }
            count.text = cartItems.size.toString() + " items in the cart"
            txt_total.text = "LKR " + String.format(
                "%.2f",
                total
            )

            recycler_cart.adapter?.notifyDataSetChanged()
        }

        btn_order.setOnClickListener {
            placeAnOrder()
        }

    }

    inner class CartHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var name: TextView = itemView.findViewById(R.id.name)
        var contract: TextView = itemView.findViewById(R.id.contract)
        var price: TextView = itemView.findViewById(R.id.price)
        var txt_qty: TextView = itemView.findViewById(R.id.txt_qty)
        var minus: ImageView = itemView.findViewById(R.id.minus)
        var plus: ImageView = itemView.findViewById(R.id.plus)
        var delete_top: ImageView = itemView.findViewById(R.id.delete_top)
        var machine_image: RoundedImageView = itemView.findViewById(R.id.machine_image)


    }

    inner class CartAdapter : RecyclerView.Adapter<CartHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CartHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)

            return CartHolder(view)
        }

        override fun getItemCount(): Int {

            return cartItems.size
        }


        override fun onBindViewHolder(holder: CartHolder, position: Int) {


            holder.contract.text = "From ${
                cartItems[position].productContractStart!!.getDateFromTimestamp()

            } \nTo ${
                cartItems[position].productContractEnd!!.getDateFromTimestamp()

            }"
            holder.name.text = cartItems[position].productTitle
            holder.txt_qty.text = cartItems[position].quantity.toString()
            holder.price.text =
                "LKR " +
                        String.format(
                            "%.2f",
                            cartItems[position].productPrice
                        )

            val jsonArray =
                JsonParser().parse(cartItems[position].productImage) as JsonArray
            if (jsonArray.size() > 0)
                Glide.with(applicationContext)
                    .load(
                        jsonArray.get(0).asString

                    ).fitCenter()
                    .into(holder.machine_image)

            holder.minus.setOnClickListener {
                if ((holder.txt_qty.text).toString().toInt() > 1) {
                    cartItems[position].productId?.let { it1 ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            cartDao?.decreaseQuantityByOne(
                                it1
                            )
                        }
                    }
                    cartItems[position].quantity = cartItems[position].quantity?.minus(1)
                    total -= cartItems[position].productPrice!!.times(
                        getNoOfDays(
                            cartItems[position].productContractEnd!!.toLong(),
                            cartItems[position].productContractStart!!.toLong()
                        )
                    )
                    recycler_cart.adapter?.notifyItemChanged(position)
                    txt_total.text = "LKR " + String.format(
                        "%.2f",
                        total
                    )
                }

            }
            holder.plus.setOnClickListener {
                cartItems[position].productId?.let { it1 ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        cartDao?.increaseQuantityByOne(
                            it1
                        )
                    }
                }
                total += cartItems[position].productPrice!!.times(
                    getNoOfDays(
                        cartItems[position].productContractEnd!!.toLong(),
                        cartItems[position].productContractStart!!.toLong()
                    )
                )
                cartItems[position].quantity = cartItems[position].quantity?.plus(1)
                recycler_cart.adapter?.notifyItemChanged(position)
                txt_total.text = "LKR " + String.format(
                    "%.2f",
                    total
                )
            }
            holder.delete_top.setOnClickListener {

                lifecycleScope.launch(Dispatchers.Main) {
                    holder.delete_top.isEnabled = false
                    withContext(Dispatchers.IO) {
                        cartItems[position].productId?.let { it1 ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                cartDao?.deleteCartItemByProductId(
                                    it1
                                )
                            }
                        }
                    }

                    //adding sample PR
                    total -= ((cartItems[position].productPrice!!).times(cartItems[position].quantity!!)).times(
                        getNoOfDays(
                            cartItems[position].productContractEnd!!.toLong(),
                            cartItems[position].productContractStart!!.toLong()
                        )
                    )

                    cartItems.removeAt(position)
                    recycler_cart.adapter?.notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)
                    if (cartItems.size == 0) {
                        tinyDB.putBoolean("isEmptyCart", true)
                        finish()
                    }


                    count.text = cartItems.size.toString() + " items in the cart"

                    txt_total.text = "LKR " + String.format(
                        "%.2f",
                        total
                    )
                    holder.delete_top.isEnabled = true


                }


            }
            holder.contract.setOnClickListener {

                showDatePicker(position)

            }

        }

    }
    private fun createOrderQuantityPopup() {


        val dialog = Dialog(this@CartActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_quantity)


        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = dialog.findViewById(R.id.cancel)

        val quantityEditText: EditText = dialog.findViewById(R.id.quantity_edit_text)
        val btnConfirm: Button = dialog.findViewById(R.id.btn_confirm)



        btnConfirm.setOnClickListener {

            when {
                TextUtils.isEmpty(quantityEditText.text.toString()) -> {
                    Toast.makeText(
                        this@CartActivity,
                        "Please enter value for quantity",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
//                    quantity = quantityEditText.text.toString().toInt()
//                    when {
//                        quantity > selectedMachine.get("availableQty").asInt -> Toast.makeText(
//                            requireActivity(),
//                            "Cannot add more than available quantity",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        else -> {
//
//                            totalPrice = quantity * noOfDays * unitPrice
//                            cartItemPrice.text = "LKR " +  String.format(
//                                "%.2f",
//                                totalPrice
//                            )
//                            cartQuantityTotal.text = quantity.toString() + " Items to cart"
//                            dialog.dismiss()
//                        }
//                    }
                }
            }

        }
        cancel.setOnClickListener {


            dialog.dismiss()
        }


        dialog.show()

    }


    fun showDatePicker(position: Int) {


        val dialog = Dialog(this@CartActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_date_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val calendarPicker: CalendarPicker = dialog.findViewById(R.id.calendarPicker)
        val btnConfirm: Button = dialog.findViewById(R.id.btn_confirm)
        val cancel: TextView = dialog.findViewById(R.id.cancel)


        val from = cartItems[position].productContractStart!!.toString().getDateFromTimestamp()

        val to = cartItems[position].productContractEnd!!.toString().getDateFromTimestamp()

        calendarPicker.setFirstSelectedDate(
            year = from.substring(0, 4).toInt(),
            month = from.substring(5, 7).toInt() - 1,
            day = from.substring(8, 10).toInt()
        )
        calendarPicker.setSecondSelectedDate(
            year = to.substring(0, 4).toInt(),
            month = to.substring(5, 7).toInt() - 1,
            day = to.substring(8, 10).toInt()
        )

        calendarPicker.initCalendar()

        btnConfirm.setOnClickListener {
            try {
                val selectedDates = calendarPicker.getSelectedDates()
                if (System.currentTimeMillis() < selectedDates?.first!!.plus(86400000) && System.currentTimeMillis() < selectedDates.second + 86400000
                ) {

                    total -= ((cartItems[position].productPrice!!).times(cartItems[position].quantity!!)).times(
                        getNoOfDays(
                            cartItems[position].productContractEnd!!.toLong(),
                            cartItems[position].productContractStart!!.toLong()
                        )
                    )
                    cartItems[position].productContractStart = selectedDates.first.toString()
                    cartItems[position].productContractEnd = selectedDates.second.toString()
                    total += ((cartItems[position].productPrice!!).times(cartItems[position].quantity!!)).times(
                        getNoOfDays(
                            cartItems[position].productContractEnd!!.toLong(),
                            cartItems[position].productContractStart!!.toLong()
                        )
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        cartDao?.updateQuantityByGivenValue(
                            cartItems[position].productId!!,
                            quantity,
                            cartItems[position].productContractStart!!,
                            cartItems[position].productContractEnd!!
                        )
                        runOnUiThread {

                            txt_total.text = "LKR " + String.format(
                                "%.2f",
                                total
                            )
                            recycler_cart.adapter?.notifyItemChanged(position)
                        }
                    }
                    dialog.dismiss()
                }else
                    Toast.makeText(
                        this@CartActivity,
                        "Selected date range invalid",
                        Toast.LENGTH_LONG
                    ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        cancel.setOnClickListener { dialog.dismiss() }


        dialog.show()

    }

    private fun createOrderSuccessPopup() {


        val dialog = Dialog(this@CartActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_order_placed)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btn_confirm: Button = dialog.findViewById(R.id.btn_confirm)




        btn_confirm.setOnClickListener {
            finish()

            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            finish()
        }

        dialog.show()

    }

    private fun placeAnOrder() {

        showHUD()
        val items = JsonArray()

        lateinit var singleItem: JsonObject

        cartItems.forEach { item ->
            singleItem = JsonObject()


            singleItem.addProperty("machineId", item.productId)
            singleItem.addProperty("quantity", item.quantity)
            singleItem.addProperty(
                "contractStartDate",
                item.productContractStart!!.getDateFromTimestamp()
            )
            singleItem.addProperty(
                "contractEndDate",
                item.productContractEnd!!.getDateFromTimestamp()
            )
            items.add(singleItem)

        }


        val parameterNames: HashMap<String, Any> = hashMapOf(

            "price" to total,
            "orderDate" to SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date()),
            "machines" to items
        )


        val apiInterface: Call<JsonObject> = ApiInterface.create().placeAnOrder(
            "Bearer ${tinyDB.getString("token")}",
            postDetails = parameterNames
        )

        apiInterface.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                hideHUD()

                response.body()?.let {


                    if (it.get("status").asBoolean) {

                        lifecycleScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO) {
                                cartDao?.clearCart()
                                tinyDB.putBoolean("isEmptyCart", true)
                            }
                            runOnUiThread {
                                createOrderSuccessPopup()
                            }

                        }


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