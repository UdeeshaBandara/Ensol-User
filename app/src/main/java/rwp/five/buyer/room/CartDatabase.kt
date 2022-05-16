package rwp.five.buyer.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import rwp.five.buyer.room.model.CartProduct

@Database(entities = [CartProduct::class], exportSchema = false, version = 1)
abstract class CartDatabase : RoomDatabase() {


    abstract fun cartDao(): CartDao


    companion object {
        private var instance: CartDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): CartDatabase? {
            if (instance == null) {

                synchronized(CartDatabase::class) {
                    instance = Room.databaseBuilder(
                        ctx.applicationContext, CartDatabase::class.java,
                        "ensol_cart_database"
                    ).fallbackToDestructiveMigration()

                        .build()
                }
            }
            return instance

        }


    }

}