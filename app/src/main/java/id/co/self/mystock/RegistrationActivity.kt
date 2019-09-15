package id.co.self.mystock

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.DialogTitle
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import id.co.self.mystock.session.PreferenceHelper
import kotlinx.android.synthetic.main.activity_registration.*
import org.json.JSONObject
import java.lang.Exception
import java.nio.charset.Charset

class RegistrationActivity : AppCompatActivity() {
    internal var registerURL = "http://192.168.43.223:3000/api/v1/register"
    lateinit var pref: PreferenceHelper
    val regTask = 1
    lateinit var mProgressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        mProgressDialog = ProgressDialog(this@RegistrationActivity)
        mProgressDialog.setMessage("Halooo")
        pref = PreferenceHelper(this)
        if(pref!!.getIsLogin()){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK )
            startActivity(intent)
        }

        btn.setOnClickListener {

            register()

        }
    }

    @Throws(Exception::class)
    private fun register(){
        mProgressDialog.show()

            Fuel.post(registerURL, listOf("name" to name!!.text.toString(),
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
                        Toast.makeText(applicationContext, jObject.toString(),Toast.LENGTH_LONG).show()
                        println(data)
                    }
                }
                mProgressDialog.dismiss()

            }
    }

    fun saveInfo(jObject: JSONObject){
        val nama = jObject.getString("name")
    }


}
