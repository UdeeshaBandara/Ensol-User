package rwp.five.buyer

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kaopiz.kprogresshud.KProgressHUD
import com.makeramen.roundedimageview.RoundedImageView
import dev.joshhalvorson.calendar_date_range_picker.calendar.CalendarPicker
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.bottom_sheet_cart.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.room.model.CartProduct
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.CoreApp.Companion.cartDao
import rwp.five.buyer.utilities.CoreApp.Companion.getDateFromTimestamp
import rwp.five.buyer.utilities.CoreApp.Companion.getNoOfDays
import rwp.five.buyer.utilities.TinyDB
import java.lang.String
import java.time.Instant
import java.time.ZoneId
import java.util.*


class HomeFragment : Fragment() {

    lateinit var tinyDB: TinyDB

    var topMachines = JsonArray()
    var machines = JsonArray()
    var originalMachinesList = JsonArray()
    var selectedMachine = JsonObject()
    var hud: KProgressHUD? = null
    lateinit var selectedDate: TextView
    lateinit var cartItemPrice: TextView
    lateinit var productBottomSheet: ProductBottomSheet
    var contractStart = ""
    var contractEnd = ""
    var quantity = 1
    var noOfDays = 1
    var totalPrice = 0.0
    var unitPrice = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tinyDB = TinyDB(requireActivity())
        hud = KProgressHUD.create(requireActivity())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)






        recycler_top_selling.adapter = TopSellingAdapter()
        recycler_top_selling.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val homeAdapter = HomeAdapter()
        recycler_home.adapter = homeAdapter
        recycler_home.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        search.setOnEditorActionListener(OnEditorActionListener { searchTextView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                homeAdapter.filter.filter(searchTextView.text.toString())

                return@OnEditorActionListener true
            }
            false
        })
        search.addTextChangedListener(object : TextWatcher {
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
                homeAdapter.filter.filter(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        getMachines()
    }

    inner class TopSellingItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var card_root: CardView = itemView.findViewById(R.id.card_root)
        var machine_title: TextView = itemView.findViewById(R.id.machine_title)
        var machine_price: TextView = itemView.findViewById(R.id.machine_price)
        var machine_image: ImageView = itemView.findViewById(R.id.machine_image)


    }

    fun openBottomSheet() {
        productBottomSheet = ProductBottomSheet(activity)
        productBottomSheet.setContentView(R.layout.bottom_sheet_cart)
        productBottomSheet.show()
    }

    inner class TopSellingAdapter : RecyclerView.Adapter<TopSellingItemHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TopSellingItemHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_top_selling, parent, false)

            return TopSellingItemHolder(view)
        }

        override fun getItemCount(): Int {

            return topMachines.size()
        }


        override fun onBindViewHolder(holder: TopSellingItemHolder, position: Int) {
            holder.machine_title.text =
                machines.get(position).asJsonObject.get("machineType").asString
            holder.machine_price.text =
                "LKR " +
                        String.format(
                            "%.2f",
                            machines.get(position).asJsonObject.get("rentPrice").asDouble
                        )
            val jsonArray =
                JsonParser().parse(machines.get(position).asJsonObject.get("images").asString) as JsonArray
            if (jsonArray.size() > 0)
                Glide.with(activity!!)
                    .load(
                        jsonArray.get(0).asString

                    ).fitCenter()
                    .into(holder.machine_image)

            holder.card_root.setOnClickListener {
                selectedMachine = machines.get(position).asJsonObject
                openBottomSheet()
            }

        }

    }

    inner class HomeItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var card_root: CardView = itemView.findViewById(R.id.card_root)
        var machine_title: TextView = itemView.findViewById(R.id.machine_title)
        var machine_price: TextView = itemView.findViewById(R.id.machine_price)
        var machine_image: RoundedImageView = itemView.findViewById(R.id.machine_image)
    }

    inner class HomeAdapter : RecyclerView.Adapter<HomeItemHolder>(), Filterable {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): HomeItemHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home, parent, false)

            return HomeItemHolder(view)
        }

        override fun getItemCount(): Int {

            return machines.size()
        }


        override fun onBindViewHolder(holder: HomeItemHolder, position: Int) {

            holder.machine_title.text =
                machines.get(position).asJsonObject.get("machineType").asString
            holder.machine_price.text =

                "LKR " +
                        String.format(
                            "%.2f",
                            machines.get(position).asJsonObject.get("rentPrice").asDouble
                        )


            val jsonArray =
                JsonParser().parse(machines.get(position).asJsonObject.get("images").asString) as JsonArray
            if (jsonArray.size() > 0)
                Glide.with(activity!!)
                    .load(
                        jsonArray.get(0).asString

                    ).fitCenter()
                    .into(holder.machine_image)

            holder.card_root.setOnClickListener {
                selectedMachine = machines.get(position).asJsonObject
                openBottomSheet()
            }


        }

        override fun getFilter(): Filter {
            return searchedFilter
        }

        private val searchedFilter: Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = JsonArray()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(originalMachinesList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }

                    machines.forEach {

                        if (it.asJsonObject.get("machineType").asString.toLowerCase()
                                .contains(filterPattern) || it.asJsonObject.get("rentPrice").asString.toLowerCase()
                                .contains(filterPattern) || it.asJsonObject.get("description").asString.toLowerCase()
                                .contains(filterPattern)
                        )
                            filteredList.add(it)
                    }

                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                machines = JsonArray()
                machines.addAll(results.values as JsonArray)
                notifyDataSetChanged()
            }
        }

    }


    inner class ProductBottomSheet(context: Context?) :
        BottomSheetDialog(context!!) {


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            quantity = 1
            noOfDays = 1
            totalPrice = 0.0
            unitPrice = 0.0
            selectedDate = findViewById(R.id.selected_date)!!
            cartItemPrice = findViewById(R.id.cart_item_price)!!

            name.text = selectedMachine.get("machineType").asString
            description.text = selectedMachine.get("description").asString
            unitPrice = selectedMachine.get("rentPrice").asDouble

            totalPrice = quantity * noOfDays * unitPrice

            cart_qty.text = quantity.toString() + " Items to cart"

            price_per_day.text = "LKR " + String.format(
                "%.2f",
                selectedMachine.get("rentPrice").asDouble
            ) + " per day"
            cart_item_price.text = "LKR " + String.format(
                "%.2f",
                totalPrice
            )


            val jsonArray =
                JsonParser().parse(selectedMachine.get("images").asString) as JsonArray
            if (jsonArray.size() > 0)
                Glide.with(activity!!)
                    .load(
                        jsonArray.get(0).asString

                    ).fitCenter()
                    .into(product_image)

            date.setOnClickListener { showDatePicker() }

            add_to_cart.setOnClickListener {
                if (selectedDate.visibility == View.GONE)
                    Toast.makeText(
                        requireActivity(),
                        "Please select contract date",
                        Toast.LENGTH_LONG
                    ).show()
                else
                    addToCart(quantity)
            }
            minus.setOnClickListener {
                if (quantity > 1)
                    txt_qty.text = (--quantity).toString()
                totalPrice = quantity * noOfDays * unitPrice
                cart_item_price.text = "LKR " + String.format(
                    "%.2f",
                    totalPrice
                )
                cart_qty.text = quantity.toString() + " Items to cart"
            }
            plus.setOnClickListener {
                if (quantity < selectedMachine.get("availableQty").asInt)
                    txt_qty.text = (++quantity).toString()

                totalPrice = quantity * noOfDays * unitPrice
                cart_item_price.text = "LKR " + String.format(
                    "%.2f",
                    totalPrice
                )
                cart_qty.text = quantity.toString() + " Items to cart"
            }
        }
    }

    fun showDatePicker() {

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_date_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val calendarPicker: CalendarPicker = dialog.findViewById(R.id.calendarPicker)
        val btnConfirm: Button = dialog.findViewById(R.id.btn_confirm)

        if (selectedDate.visibility == View.VISIBLE) {
            val from = Instant.ofEpochSecond(contractStart.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            val to = Instant.ofEpochSecond(contractEnd.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            calendarPicker.setFirstSelectedDate(
                year = from.year,
                month = from.monthValue + 1,
                day = from.dayOfMonth
            )
            calendarPicker.setSecondSelectedDate(
                year = to.year,
                month = to.monthValue + 1,
                day = to.dayOfMonth
            )
        }
        calendarPicker.initCalendar()

        btnConfirm.setOnClickListener {
            try {
                val selectedDates = calendarPicker.getSelectedDates()


                if (selectedDates != null) {
                    val firstDate = selectedDates.first.toString()
                    val secondDate = selectedDates.second.toString()
                    totalPrice = 0.0
                    selectedDate.visibility = View.VISIBLE
                    contractStart = firstDate
                    contractEnd = secondDate
                    selectedDate.text =
                        "From ${firstDate.getDateFromTimestamp()} To ${secondDate.getDateFromTimestamp()}"
                    noOfDays = getNoOfDays(
                        secondDate.toLong(),
                        firstDate.toLong()
                    )
                    totalPrice = quantity * noOfDays * unitPrice
                    cartItemPrice.text = "LKR " + String.format(
                        "%.2f",
                        totalPrice
                    )

                }
            } catch (e: Exception) {

                e.printStackTrace()
            }
            dialog.dismiss()
        }

        dialog.show()

    }


    private fun addToCart(quantity: Int) {


        lifecycleScope.launch(Dispatchers.IO) {
            if (cartDao?.checkItemExist(
                    selectedMachine.get(
                        "id"
                    ).asInt
                ) == 0
            ) {

                cartDao?.insertAll(
                    CartProduct(
                        productId = selectedMachine.get(
                            "id"
                        ).asInt,
                        productTitle = selectedMachine.get(
                            "machineType"
                        ).asString,
                        productImage = selectedMachine.get(
                            "images"
                        ).asString,
                        productContractStart = contractStart,
                        productContractEnd = contractEnd,
                        productPrice = selectedMachine.get(
                            "rentPrice"
                        ).asDouble,
                        quantity = quantity

                    )
                )


            } else
                cartDao?.updateQuantityByGivenValue(
                    selectedMachine.get(
                        "id"
                    ).asInt,
                    quantity,
                    contractStart,
                    contractEnd
                )


            activity?.runOnUiThread {

                Toast.makeText(
                    requireActivity(),
                    "Your product has been added to cart",
                    Toast.LENGTH_LONG
                ).show()


            }
            tinyDB.putBoolean("isEmptyCart", false)
        }

    }

    private fun getMachines() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getMachineHome(
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

                        topMachines = it.getAsJsonObject("data").getAsJsonArray("top_machines")
                        machines = it.getAsJsonObject("data").getAsJsonArray("machines")
                        originalMachinesList = it.getAsJsonObject("data").getAsJsonArray("machines")
                        recycler_home.adapter?.notifyDataSetChanged()
                        recycler_top_selling.adapter?.notifyDataSetChanged()

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