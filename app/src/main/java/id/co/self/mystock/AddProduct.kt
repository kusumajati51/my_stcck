package id.co.self.mystock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import id.co.self.mystock.databinding.ActivityAddProductBinding
import id.co.self.mystock.session.PreferenceHelper
import id.co.self.mystock.fetchApi.url.LinkApi
import org.json.JSONArray
import org.json.JSONObject
import java.security.AccessController.getContext
import java.util.ArrayList


class AddProduct : AppCompatActivity() {

    lateinit var mBinding:ActivityAddProductBinding
    lateinit var pref: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        mBinding = DataBindingUtil.setContentView(this@AddProduct,
            R.layout.activity_add_product)
        pref = PreferenceHelper(this)
        getData()
    }

    private fun getData(){

        Fuel.Companion.get(LinkApi.categorySpinner)
            .header(mapOf("Authorization" to pref.getToken().toString()))
            .responseJson{request, response, result ->
                when(result){
                    is Result.Failure ->{
                        val ex = result.getException()
                        val e = ex.errorData.toString(Charsets.UTF_8)
                        Toast.makeText(this,e, Toast.LENGTH_LONG).show()
                    }
                    is Result.Success ->{
                        val jObject = result.get().obj()
                        val status = jObject.optInt("status")
                        if(status == 1){
                            val jArray:JSONArray = jObject.getJSONArray("data")
                            val list = ArrayList<String>()
                            for (i in 0 until jArray.length()){
                                val vObject = jArray.optJSONObject(i)
                                list.add(vObject.optString("name"))
                            }

                            var adapterVal = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,list)
                            mBinding.productSpinner.adapter = adapterVal

                        }

                    }
                }
            }

    }


}
