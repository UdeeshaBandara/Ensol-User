package rwp.five.buyer.utilities

import android.app.Application
import android.content.Context
import rwp.five.buyer.room.CartDao
import rwp.five.buyer.room.CartDatabase

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
    }
}
