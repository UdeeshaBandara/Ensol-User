package rwp.five.buyer.utilities

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {

    @POST("auth")
    fun login(
        @Body postDetails: HashMap<String, String>
    ): Call<JsonObject>

    @POST("user")
    fun register(
        @Body postDetails: HashMap<String, String>
    ): Call<JsonObject>

    @GET("machine")
    fun getAllMachines(
        @Header("Authorization") token: String
    ): Call<JsonObject>

    @GET("order/current")
    fun getCurrentOrders(
        @Header("Authorization") token: String
    ): Call<JsonObject>

    @GET("order/past")
    fun getPastOrders(
        @Header("Authorization") token: String
    ): Call<JsonObject>

    @GET("user/notification")
    fun getNotifications(
        @Header("Authorization") token: String
    ): Call<JsonObject>


    @POST("order")
    fun placeAnOrder(
        @Header("Authorization") token: String,
        @Body postDetails: HashMap<String, Any>
    ): Call<JsonObject>


    companion object {

        private val BASE_URL = "https://ensolapi.herokuapp.com/"
//        private val BASE_URL = "http://192.168.1.3:3000/"

        fun create(): ApiInterface {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiInterface::class.java)

        }

        fun JsonObject.getNullable(key: String): JsonElement? {
            val value: JsonElement = this.get(key) ?: return null

            if (value.isJsonNull) {

                return null
            }

            return value
        }
    }
}