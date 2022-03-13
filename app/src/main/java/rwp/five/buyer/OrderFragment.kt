package rwp.five.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_order.*

class OrderFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        current.setOnClickListener {
            current.setBackgroundColor(resources.getColor(R.color.blue))
            past.setBackgroundColor(resources.getColor(R.color.white))
            past.alpha = 0.3f
            current.alpha = 1f
            past_tick.visibility = View.INVISIBLE
            current_tick.visibility = View.VISIBLE
        }
        past.setOnClickListener {
            current_tick.visibility = View.INVISIBLE
            past_tick.visibility = View.VISIBLE
            past.setBackgroundColor(resources.getColor(R.color.blue))
            current.setBackgroundColor(resources.getColor(R.color.white))
            current.alpha = 0.3f
            past.alpha = 1f
        }

        recycler_orders.adapter = CurrentOrderAdapter()
        recycler_orders.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )

    }
    inner class CurrentOrderItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {




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

            return 10
        }


        override fun onBindViewHolder(holder: CurrentOrderItemHolder, position: Int) {


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

}