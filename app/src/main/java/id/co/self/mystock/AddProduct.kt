package id.co.self.mystock

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import id.co.self.mystock.databinding.ActivityAddProductBinding
import id.co.self.mystock.fetchApi.ApiInterface
import id.co.self.mystock.fetchApi.ApiMain
import id.co.self.mystock.session.PreferenceHelper
import id.co.self.mystock.fetchApi.url.LinkApi
import kotlinx.android.synthetic.main.add_type_layout.*
import retrofit2.Callback
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class AddProduct : AppCompatActivity() {

    lateinit var mBinding:ActivityAddProductBinding
    lateinit var pref: PreferenceHelper
    lateinit var api:ApiInterface
    lateinit var hashMap: HashMap<String, RequestBody>
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    lateinit var name:String
    lateinit var path:String
    lateinit var partFile:MultipartBody.Part

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        mBinding = DataBindingUtil.setContentView(this@AddProduct,
            R.layout.activity_add_product)
        api = ApiMain().services
        pref = PreferenceHelper(this)
        hashMap = HashMap()
        getData()
    }

    override fun onStart() {
        super.onStart()
        mBinding.potoCategory.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED)
                {
                    val permission = arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, PERMISSION_CODE)
                }
                else{
                    camera()
                }
            }else{
                camera()
            }
        }

        mBinding.save.setOnClickListener {
            val name = mBinding.productName.text.toString()
            val price = mBinding.productPrice.text.toString()
            val quantity = mBinding.productQuantity.text.toString()
            if(name.trim().isEmpty()){
                mBinding.productName.error = "Silahkan isi nama product anda"
                return@setOnClickListener
            }
            if(price.trim().isEmpty()){
                mBinding.productPrice.error = "Silahkan masuksn harga product anda"
                return@setOnClickListener
            }
            if(quantity.trim().isEmpty()){
                mBinding.productQuantity.error = "Silahkan masukan jumlah barang anda"
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(path)){
                Toast.makeText(this@AddProduct,"Anda Belum Memasukan gambar category",
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //@TODO connect to api
            hashMap["name"] = RequestBody.create(null, name)
            hashMap["price"] = RequestBody.create(null, price)
            hashMap["check_in"] = RequestBody.create(null,quantity)

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        when(requestCode) {
            PERMISSION_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    camera()
                }else{
                    Toast.makeText(applicationContext,"Permission denied",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_CAPTURE_CODE){
            val options = RequestOptions().centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
            val bitmap = BitmapFactory.decodeFile(path)
            partFile = convertPart(bitmap,"attachment")
            Glide.with(this).setDefaultRequestOptions(options).load(path)
                .into(mBinding.potoCategory)
        }
    }



    private fun getData(){
        Fuel.get(LinkApi.categorySpinner)
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

                            var adapterVal = ArrayAdapter(this,
                                android.R.layout.simple_spinner_dropdown_item,list)
                            mBinding.productSpinner.adapter = adapterVal
                            mBinding.productSpinner.onItemSelectedListener = object :
                                AdapterView.OnItemSelectedListener{
                                override fun onItemSelected( parent: AdapterView<*>?, view: View?,
                                                             position: Int, id: Long) {
                                    val objectVal = jArray.getJSONObject(position)
                                    val idCategory = objectVal.optInt("id")
                                    hashMap["category_id"] = RequestBody.create(null
                                        ,idCategory.toString())
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager)!=null){
           lateinit var photoFile: File
            try {
                photoFile= createFilePath()
            }catch (e:Exception){
                e.printStackTrace()
            }
            val photoUri = FileProvider.getUriForFile(this@AddProduct,
                "id.co.self.mystock.fileprovider",
                photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
            startActivityForResult(intent,IMAGE_CAPTURE_CODE)
        }

    }

    @SuppressLint("SimpleDateFormat")
    @Throws(Exception::class)
    private fun createFilePath(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmms").format(Date())
        val imageFileName = "JPEG_" + timestamp + "_"
        name = imageFileName
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        path = image.absolutePath
        return image
    }

    private fun convertPart(bitmap: Bitmap,param: String):MultipartBody.Part {
        val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            System.currentTimeMillis().toString()+"_image.png")
        var os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG,75,os)
            os.flush()
            os.close()

        }catch (e:Exception){
            e.printStackTrace()
        }
        var body:RequestBody = RequestBody.create(LinkApi.image,imageFile)
        var part: MultipartBody.Part = MultipartBody.Part.createFormData(param,imageFile.name,body)
        return part
    }

    private fun createProduct(token:String, mut: HashMap<String,
            RequestBody>,part:MultipartBody.Part ){
        api.createNewProduct(token,part,mut).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try{
                    lateinit var body:String
                    if(response.isSuccessful){
                        body = response.body()!!.string()
                    }else{
                        body = response.errorBody()!!.string()
                    }
                    Toast.makeText(this@AddProduct,body,Toast.LENGTH_LONG).show()
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AddProduct,t.message,Toast.LENGTH_LONG).show()
                t.printStackTrace()
            }
        })
    }



}
