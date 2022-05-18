package rwp.five.buyer.utilities

import android.app.Application
import android.content.Context
import android.text.format.DateFormat
import rwp.five.buyer.room.CartDao
import rwp.five.buyer.room.CartDatabase
import java.util.*
import java.util.concurrent.TimeUnit

class CoreApp : Application() {


    override fun onCreate() {
        super.onCreate()
        instance = this
        appDatabase = CartDatabase.getInstance(this@CoreApp)
        cartDao = appDatabase?.cartDao()
    }

    companion object {
        var instance: CoreApp? = null
            private set

        //Database
        var appDatabase: CartDatabase? = null
        var cartDao: CartDao? = null
        fun getContext(): Context? {
            return instance
        }
        fun String.getDateFromTimestamp(): String {
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = this.toLong()
            return DateFormat.format("yyyy-MM-dd", calendar).toString()
        }

        fun getNoOfDays(to: Long, from: Long): Int {

            return (TimeUnit.MILLISECONDS.toDays(
                to.minus(from)
            )).toInt()

        }

    }
}
