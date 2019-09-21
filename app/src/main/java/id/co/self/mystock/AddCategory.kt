@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package id.co.self.mystock

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.co.self.mystock.fetchApi.ApiInterface
import id.co.self.mystock.fetchApi.ApiMain
import id.co.self.mystock.session.PreferenceHelper
import kotlinx.android.synthetic.main.add_type_layout.*
import retrofit2.Callback
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception

class AddCategory : AppCompatActivity() {

    lateinit var pref:PreferenceHelper
    lateinit var apiInterface: ApiInterface
    var mut:HashMap<String, String> = HashMap()
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_type_layout)
        pref = PreferenceHelper(applicationContext)
        apiInterface = ApiMain().services
        mut.set("Authorization", pref.getToken().toString())

    }

    override fun onStart() {
        super.onStart()
        poto_category.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED
                    ||checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED)
                {
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, PERMISSION_CODE)
                }
                else{
                    //permission already granted
                    openCamera()
                }            }else{
                openCamera()
            }
        }
    }

    private fun openCamera(){
//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, "New Picture")
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
//        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
//        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE)
            }
        }
    }

    private fun putData(token: String,multipartBody: MultipartBody.Part, hashMap: HashMap<String, RequestBody>){
        apiInterface.createNewCategory(token,multipartBody,hashMap).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                   if(response.isSuccessful){
                      val rData: String = response.body()!!.string()
                      Toast.makeText(this@AddCategory,rData,Toast.LENGTH_LONG).show()
                   }
                   else{
                       val rData: String = response.errorBody()!!.string()
                       Toast.makeText(this@AddCategory,rData,Toast.LENGTH_LONG).show()
                   }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            //set image captured to image view
            val imageBitmap = data!!.extras.get("data") as Bitmap
            poto_category.setImageBitmap(imageBitmap)
    }
}
