package rwp.five.buyer

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
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
import rwp.five.buyer.utilities.TinyDB
import java.util.*


class HomeFragment : Fragment() {

    lateinit var tinyDB: TinyDB

    var machines = JsonArray()
    var selectedMachine = JsonObject()
    var hud: KProgressHUD? = null
    lateinit var selectedDate: TextView
    lateinit var productBottomSheet: ProductBottomSheet

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


        search.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                performSearch()
                Toast.makeText(requireActivity(), "Searched", Toast.LENGTH_LONG).show()
                return@OnEditorActionListener true
            }
            false
        })


        recycler_top_selling.adapter = TopSellingAdapter()
        recycler_top_selling.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recycler_home.adapter = HomeAdapter()
        recycler_home.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
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

            return machines.size()
        }


        override fun onBindViewHolder(holder: TopSellingItemHolder, position: Int) {
            holder.machine_title.text =
                machines.get(position).asJsonObject.get("machineType").asString
            holder.machine_price.text =
                "LKR " + machines.get(position).asJsonObject.get("rentPrice").asString
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

    inner class HomeAdapter : RecyclerView.Adapter<HomeItemHolder>() {

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
                "LKR " + machines.get(position).asJsonObject.get("rentPrice").asString
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

    inner class ProductBottomSheet(context: Context?) :
        BottomSheetDialog(context!!) {

        var quantity = 1
        var noOfDays = 1
        var totalPrice = 0.0
        var price = 0.0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            name.text = selectedMachine.get("machineType").asString
            description.text = selectedMachine.get("description").asString
            price = selectedMachine.get("rentPrice").asDouble
            selectedDate = findViewById(R.id.selected_date)!!
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
                totalPrice = quantity * noOfDays * price

            }
            plus.setOnClickListener {
                if (quantity < selectedMachine.get("availableQty").asInt)
                    txt_qty.text = (++quantity).toString()

                totalPrice = quantity * noOfDays * price

            }
        }
    }

    fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            requireActivity(),
            { _, year, monthOfYear, dayOfMonth ->
                selectedDate.visibility = View.VISIBLE

                val month = if ((monthOfYear + 1) < 10)
                    String.format("%02d", monthOfYear + 1)
                else
                    (monthOfYear + 1).toString()

                selectedDate.text =
                    year.toString() + "-" + month + "-" + dayOfMonth.toString()

            },
            year,
            month,
            day
        )
        dpd.datePicker.minDate = System.currentTimeMillis()

        dpd.show()
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
                        productContract = selectedDate.text.toString(),
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
                    selectedDate.text.toString()
                )

//            lifecycleScope.launch(Dispatchers.IO) {
//                var subTotal = 0.0
//                model.cartItems.postValue(cartDao?.getAll()!!)
//
//                cartDao?.getAll()!!.forEach { item ->
//                    subTotal += item.productPrice?.times(item.quantity!!)!!
//                    model.subTotal.postValue(subTotal)
//                    model.total.postValue(subTotal)
//
//
//                }
//            }

            activity?.runOnUiThread {

                Toast.makeText(
                    requireActivity(),
                    "Your product has been added to cart",
                    Toast.LENGTH_LONG
                ).show()


            }
        }

    }

    private fun getMachines() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getAllMachines(
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

                        machines = it.getAsJsonArray("data")
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