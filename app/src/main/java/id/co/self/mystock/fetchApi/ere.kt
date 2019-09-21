package id.co.self.mystock.fetchApi

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ere {

    @Headers("Content-Type: multipart/form-data")
    @Multipart
    @POST("s")
    fun upload(
        @Header("Authorization") token: String
    ): Call<ResponseBody>
}
