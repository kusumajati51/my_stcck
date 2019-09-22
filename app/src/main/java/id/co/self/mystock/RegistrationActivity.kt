package id.co.self.mystock

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import id.co.self.mystock.session.PreferenceHelper
import id.co.self.mystock.fetchApi.url.LinkApi
import kotlinx.android.synthetic.main.activity_registration.*
import org.json.JSONObject
import java.lang.Exception

class RegistrationActivity : AppCompatActivity() {
    lateinit var pref: PreferenceHelper

    lateinit var mProgressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        mProgressDialog = ProgressDialog(this@RegistrationActivity)
        mProgressDialog.setMessage("Loading...")
        pref = PreferenceHelper(this)
        if(pref!!.getIsLogin()){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK )
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        btn.setOnClickListener {
            register()
        }

        tvlogin.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    @Throws(Exception::class)
    private fun register(){
        mProgressDialog.show()

            Fuel.post(LinkApi.registerURL, listOf("name" to name!!.text.toString(),
                "email" to email!!.text.toString(),
                "no_hp" to phone_number!!.text.toString(),
                "password" to password!!.text.toString(),
                "password_confirmation" to password_conrimation!!.text.toString())
            ).responseJson { request, response, result ->
                when(result){
                    is Result.Failure ->{
                        val ex = result.getException()
                        val e = ex.errorData.toString(Charsets.UTF_8)
                        Toast.makeText(applicationContext, e ,Toast.LENGTH_LONG).show()
                        println(ex)
                    }
                    is Result.Success ->{
                        val data = result.get().content
                        val jObject = JSONObject(data)
                        Toast.makeText(applicationContext, jObject.optString("message"),Toast.LENGTH_LONG).show()
                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        saveInfo(jObject)
                        println(data)
                    }
                }
                mProgressDialog.dismiss()

            }
    }

    fun saveInfo(jObject: JSONObject){
        pref!!.putsLogin(true)
        if(jObject.getInt("status")== 1){
            val data:JSONObject = jObject.getJSONObject("data")
            pref!!.putToken(data.optString("token"))
            pref!!.putName(data.optString("name"))
        }
    }


}
