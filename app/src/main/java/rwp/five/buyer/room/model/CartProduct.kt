package rwp.five.buyer.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "CartItem")

data class CartProduct  (
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "product_id") val productId: Int?,
    @ColumnInfo(name = "product_title") val productTitle: String?,
    @ColumnInfo(name = "product_image") val productImage: String?,
    @ColumnInfo(name = "product_contract") val productContract: String?,
    @ColumnInfo(name = "product_price") val productPrice: Double?,
    @ColumnInfo(name = "quantity") var quantity: Int?
) : Serializable