package rwp.five.buyer.room


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import rwp.five.buyer.room.model.CartProduct

@Dao
interface CartDao {

    @Insert
    fun insertAll(vararg cartProduct: CartProduct)

    @Query("DELETE FROM  CartItem WHERE product_id = :productId")
    fun deleteCartItemByProductId(productId: Int): Int

    @Query("DELETE FROM  CartItem")
    fun clearCart(): Int

    @Query("SELECT * FROM CartItem")
    fun getAll(): MutableList<CartProduct>

    @Query("UPDATE CartItem SET quantity = quantity - 1 WHERE product_id = :productId")
    fun decreaseQuantityByOne(productId: Int): Int

    @Query("UPDATE CartItem SET quantity = quantity + 1 WHERE product_id = :productId")
    fun increaseQuantityByOne(productId: Int): Int

    @Query("UPDATE CartItem SET quantity = quantity + :quantity , product_contract_start = :productContractStart,product_contract_end = :productContractEnd WHERE product_id = :productId")
    fun updateQuantityByGivenValue(
        productId: Int,
        quantity: Int,
        productContractStart: String,
        productContractEnd: String
    ): Int

//    @Query("UPDATE CartItem SET product_contract = :contractDate WHERE product_id = :productId")
//    fun updateContractByGivenValue(productId: Int, contractDate: String): Int

    @Query("SELECT COUNT(*) FROM CartItem WHERE product_id = :productId")
    fun checkItemExist(productId: Int): Int

    @Query("SELECT quantity FROM CartItem WHERE product_id = :productId")
    fun getQuantityById(productId: Int): Int

}