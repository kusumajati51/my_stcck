package id.co.self.mystock.session

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(private val context:Context) {
    private val INTRO = "intro"
    private val TOKEN = "token"
    private val NAME = "name"

    private val app_prefs: SharedPreferences

    init {
        app_prefs = context.getSharedPreferences(
            "shared",Context.MODE_PRIVATE
        )
    }

    fun putsLogin(loginOrOut: Boolean) {
        val edit = app_prefs.edit()
        edit.putBoolean(INTRO, loginOrOut)
        edit.apply()
    }

    fun getIsLogin():Boolean{
        return app_prefs.getBoolean(INTRO, false)
    }

    fun putToken(loginOrOut: String){
        val edit = app_prefs.edit()
        edit.putString(TOKEN, loginOrOut)
        edit.apply()
    }

    fun getToken(): String?{
        return app_prefs.getString(TOKEN,"")
    }

    fun putName(nama: String){
        val edit = app_prefs.edit()
        edit.putString(NAME, nama)
        edit.apply()
    }

    fun getName(): String?{
        return app_prefs.getString(NAME,"")
    }

    fun delete(){
        val delete = app_prefs.edit()
        delete.clear()
        delete.commit()
    }

}