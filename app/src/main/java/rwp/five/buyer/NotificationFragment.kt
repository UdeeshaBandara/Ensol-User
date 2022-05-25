package rwp.five.buyer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kaopiz.kprogresshud.KProgressHUD
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.fragment_notification.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rwp.five.buyer.utilities.ApiInterface
import rwp.five.buyer.utilities.TinyDB

class NotificationFragment : Fragment() {

    var hud: KProgressHUD? = null
    lateinit var tinyDB: TinyDB

    var notifications = JsonArray()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tinyDB = TinyDB(requireActivity())
        hud = KProgressHUD.create(requireActivity())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        recycler_notification.adapter = NotificationAdapter()
        recycler_notification.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        getNotifications()

    }

    inner class NotificationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var card_root: CardView = itemView.findViewById(R.id.card_root)
        var title: TextView = itemView.findViewById(R.id.title)
        var description: TextView = itemView.findViewById(R.id.description)
        var time: TextView = itemView.findViewById(R.id.time)
        var notification_image: RoundedImageView = itemView.findViewById(R.id.notification_image)


    }

    inner class NotificationAdapter : RecyclerView.Adapter<NotificationHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): NotificationHolder {

            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)

            return NotificationHolder(view)
        }

        override fun getItemCount(): Int {

            return notifications.size()
        }


        override fun onBindViewHolder(holder: NotificationHolder, position: Int) {

            val jsonObject =
                JsonParser().parse(notifications.get(position).asJsonObject.get("content").asString) as JsonObject
            holder.title.text = jsonObject.get("title").asString
            holder.description.text = jsonObject.get("description").asString
            holder.time.text =
                notifications.get(position).asJsonObject.get("createdAt").asString.substring(
                    0,
                    10
                ) + " at " +
                        notifications.get(position).asJsonObject.get("createdAt").asString.substring(
                            11,
                            19
                        )
        }

    }

    private fun getNotifications() {

        showHUD()

        val apiInterface: Call<JsonObject> = ApiInterface.create().getNotifications(
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
                        empty_msg.visibility = View.GONE
                        notifications = it.getAsJsonArray("data")
                        recycler_notification.adapter?.notifyDataSetChanged()

                    } else {
                        empty_msg.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            it.get("data").asString,
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                hideHUD()
                Log.e("fail", t.message.toString())
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            getNotifications()
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