package id.co.self.mystock

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import id.co.self.mystock.session.PreferenceHelper
import id.co.self.mystock.url.LinkApi
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    lateinit var pref: PreferenceHelper
    lateinit var mProgreesDialog: ProgressDialog
    var jObject: JSONObject = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        pref = PreferenceHelper(applicationContext)
        mProgreesDialog = ProgressDialog(this@LoginActivity)
        mProgreesDialog.setMessage("Loading....")


    }

    override fun onStart() {
        super.onStart()

        btn.setOnClickListener {
            jObject.put("email", email.text.toString())
            jObject.put("password", password.text.toString())
            login()
        }

        tvreg.setOnClickListener{

        }
    }

    private fun login() {
        Fuel.post(LinkApi.loginUrl)
            .jsonBody(jObject.toString())
            .responseJson { request, response, result ->
                when(result){
                    is Result.Failure -> {
                        val ex = result.getException()
                        val e = ex.errorData.toString(Charsets.UTF_8)
                        Toast.makeText(this,e,Toast.LENGTH_LONG).show()
                    }
                    is Result.Success ->{
                        val jObject = result.get().obj()
                        Toast.makeText(this, jObject.toString(),Toast.LENGTH_LONG).show()
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        saveData(jObject)
                        println(jObject)
                    }
                }
            }
    }

    private fun saveData(jObject: JSONObject){
        pref!!.putsLogin(true)
        if(jObject.getInt("status")== 1){
            val data:JSONObject = jObject.getJSONObject("data")
            pref!!.putToken(data.optString("token"))
            pref!!.putName(data.optString("name"))
        }
    }


}
