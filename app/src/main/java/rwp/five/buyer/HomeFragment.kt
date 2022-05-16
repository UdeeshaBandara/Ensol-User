package rwp.five.buyer

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kaopiz.kprogresshud.KProgressHUD
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB


class HomeFragment : Fragment() {

    lateinit var tinyDB: TinyDB

    var machines = JsonArray()
    var hud: KProgressHUD? = null

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