package id.co.self.mystock.fetchApi.url

import okhttp3.MediaType

object LinkApi {
    /*base url*/
    val url = "http://192.168.43.223:3000/api/v1/"

    /*authentication url*/
    val registerURL = url + "register"
    val loginUrl = url + "login"

    /* category url */
    val categoryMenu = url + "category/index/menu"
    val categorySpinner = url + "category/index/spinner"
    val categoryBaru = url + "category/baru"

    val JSON = MediaType.parse("application/json; charset=utf-8")
    val image = MediaType.parse("image/*")

}
