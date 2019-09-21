package id.co.self.mystock.fetchApi

import okhttp3.*
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Headers

interface ApiInterface {

    @Headers("Content-Type: multipart/form-data")
    @Multipart
    @POST("category/baru")
    fun createNewCategory(
        @Header("Authorization") token: String,
        @Part multipart: MultipartBody.Part,
        @Part hashMap: Map<String, RequestBody>): Call<ResponseBody>


}