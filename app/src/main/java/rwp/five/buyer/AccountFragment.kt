package rwp.five.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment() {

    var accountLabelArray = arrayOf(

        "Service Requests",
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_account.adapter = CategoryAdapter()

        recycler_account.layoutManager = GridLayoutManager(activity?.applicationContext, 2)

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
//        if (hidden)
//            requireActivity().window?.statusBarColor =
//                ContextCompat.getColor(requireActivity(), R.color.white)
//        else
//            requireActivity().window?.statusBarColor =
//                ContextCompat.getColor(requireActivity(), R.color.red)
//

    }

    inner class AccountItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.findViewById(R.id.title)
        var icon: ImageView = itemView.findViewById(R.id.icon)


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

        }

    }
}