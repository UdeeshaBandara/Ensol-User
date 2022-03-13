package rwp.five.buyer

import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*

class Main : AppCompatActivity() {

    //Animation duration for bottom navigation
    private var transitionDuration: Long = 300


    lateinit var mainFragmentManager: FragmentManager

    private val homeFragment: HomeFragment = HomeFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainFragmentManager = supportFragmentManager

        //hide bottom navigation on keyboard appear
        hideOverlay()

        //add initial fragments to fragment container
        if (savedInstanceState == null)
            mainFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                homeFragment,
                homeFragment.javaClass.name
            ).commit()


        lnr_home_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_home_selected,
                "Home",
                R.drawable.home_select,
                lnr_home_deselected
            )

        }
        lnr_categories_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_categories_selected,
                "Orders",
                R.drawable.order_select,
                lnr_categories_deselected
            )
        }
        lnr_cart_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_cart_selected,
                "Notifications",
                R.drawable.bell_select,
                lnr_cart_deselected
            )

        }



        lnr_profile_deselected.setOnClickListener {

            showDeselectBottomNavigationItems()
            bottomNavigationTabChanger(
                lnr_profile_selected,
                "Account",
                R.drawable.user_select,
                lnr_profile_deselected
            )
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
        lnr_categories_deselected.visibility = View.VISIBLE
        lnr_cart_deselected.visibility = View.VISIBLE
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


            } else {

                bottom_shadow.visibility =
                    View.VISIBLE
                bottom_navigation.visibility =
                    View.VISIBLE


            }

        }
    }


}