package rwp.five.buyer

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import rwp.five.buyer.utilities.TinyDB

class Main : AppCompatActivity() {

    //Animation duration for bottom navigation
    private var transitionDuration: Long = 300

    lateinit var tinyDB: TinyDB
    lateinit var mainFragmentManager: FragmentManager

    private val homeFragment: HomeFragment = HomeFragment()
    private val orderFragment: OrderFragment = OrderFragment()
    private val notificationFragment: NotificationFragment = NotificationFragment()
    private val accountFragment: AccountFragment = AccountFragment()
    private var currentFragments: Fragment = homeFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainFragmentManager = supportFragmentManager


        tinyDB = TinyDB(this)

        //hide bottom navigation on keyboard appear
        hideOverlay()

        //add initial fragments to fragment container
        if (savedInstanceState == null)
            mainFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                homeFragment,
                homeFragment.javaClass.name
            ).add(
                R.id.fragment_container,
                orderFragment,
                orderFragment.javaClass.name
            ).hide(orderFragment).add(
                R.id.fragment_container,
                notificationFragment,
                notificationFragment.javaClass.name
            ).hide(notificationFragment).add(
                R.id.fragment_container,
                accountFragment,
                accountFragment.javaClass.name
            ).hide(accountFragment).commit()


        lnr_home_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_home_selected,
                "Home",
                R.drawable.home_select,
                lnr_home_deselected
            )
            changeFragment(currentFragments, homeFragment)

        }
        lnr_orders_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_categories_selected,
                "Orders",
                R.drawable.order_select,
                lnr_orders_deselected
            )
            changeFragment(currentFragments, orderFragment)

        }
        lnr_notification_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_notification_selected,
                "Notifications",
                R.drawable.bell_select,
                lnr_notification_deselected
            )
            changeFragment(currentFragments, notificationFragment)
        }



        lnr_profile_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_profile_selected,
                "Account",
                R.drawable.user_select,
                lnr_profile_deselected
            )
            changeFragment(currentFragments, accountFragment)
        }

        cart.setOnClickListener {
            if (tinyDB.getBoolean("isEmptyCart"))
                Toast.makeText(
                    this,
                    "No products available in your cart!!",
                    Toast.LENGTH_LONG
                ).show()
            else
                startActivity(Intent(this, CartActivity::class.java))
        }


    }

    private fun bottomNavigationTabChanger(
        selectedLayout: LinearLayout,
        selectedTabText: String,
        selectedTabImage: Int,
        deselectIcon: ImageView
    ) {

        val tabIconViewAnimator =
            ObjectAnimator.ofFloat(
                lnr_base_selected,
                "translationX",
                lnr_base_selected.x,
                selectedLayout.x
            )

        tabIconViewAnimator.duration = transitionDuration
        tabIconViewAnimator.interpolator = AccelerateDecelerateInterpolator()
        tabIconViewAnimator.start()

        img_selected.setImageResource(selectedTabImage)
        text_selected.text = selectedTabText

        deselectIcon.visibility = View.INVISIBLE
        lnr_base_selected.visibility = View.VISIBLE


    }

    private fun showDeselectBottomNavigationItems() {

        lnr_home_deselected.visibility = View.VISIBLE
        lnr_orders_deselected.visibility = View.VISIBLE
        lnr_notification_deselected.visibility = View.VISIBLE
        lnr_profile_deselected.visibility = View.VISIBLE


    }

    private fun hideOverlay() {
        rlt_root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rlt_root.getWindowVisibleDisplayFrame(r)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val heightDiff = displayMetrics.heightPixels - (r.bottom - r.top)
            if (heightDiff > 100) {

                bottom_shadow.visibility =
                    View.INVISIBLE
                bottom_navigation.visibility =
                    View.INVISIBLE
                (mainFragmentManager.findFragmentByTag(homeFragment.javaClass.name) as HomeFragment).top_products.visibility =
                    View.GONE

            } else {

                bottom_shadow.visibility =
                    View.VISIBLE
                bottom_navigation.visibility =
                    View.VISIBLE
                (mainFragmentManager.findFragmentByTag(homeFragment.javaClass.name) as HomeFragment).top_products.visibility =
                    View.VISIBLE

            }

        }
    }

    private fun changeFragment(currentFragment: Fragment, nextFragment: Fragment) {

        mainFragmentManager.beginTransaction()
            .hide(mainFragmentManager.findFragmentByTag(currentFragment.javaClass.name)!!)
            .show(mainFragmentManager.findFragmentByTag(nextFragment.javaClass.name)!!).commit()
        currentFragments = nextFragment

    }

    var userDetailUpdateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK)
                (mainFragmentManager.findFragmentByTag(accountFragment.javaClass.name) as AccountFragment).getUserDetails()

        }

}