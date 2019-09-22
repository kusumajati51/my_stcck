@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package id.co.self.mystock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import id.co.self.mystock.fetchApi.ApiInterface
import id.co.self.mystock.fetchApi.ApiMain
import id.co.self.mystock.session.PreferenceHelper
import id.co.self.mystock.fetchApi.url.LinkApi
import kotlinx.android.synthetic.main.add_type_layout.*
import retrofit2.Callback
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

 class AddCategory : AppCompatActivity() {

    lateinit var pref:PreferenceHelper
    lateinit var apiInterface: ApiInterface
    lateinit var bitmap : Bitmap
    var mut:HashMap<String, RequestBody> = HashMap()
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null
    lateinit var path: String
    lateinit var name: String
    lateinit var partFile:MultipartBody.Part


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_type_layout)
        pref = PreferenceHelper(this@AddCategory)
        apiInterface = ApiMain().services
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
                    camera()
                }
            }else{
                camera()
            }
        }
        save.setOnClickListener {
            val name: String = name_category.text.toString()
            if(name.trim().isEmpty()){
                name_category.setError("Silahkan isi nama anda")
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(path)){
                Toast.makeText(this@AddCategory,"Anda Belum Memasukan gambar category",
                    Toast.LENGTH_LONG).show()
            }
            mut.put("name",RequestBody.create(null,name))
            putData(pref.getToken().toString(),partFile,mut)
        }


    }


    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray){
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    camera()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            //set image captured to image view
           /* val imageBitmap = data!!.extras.get("data") as Bitmap
            poto_category.setImageBitmap(imageBitmap)*/
            if(requestCode == IMAGE_CAPTURE_CODE){
                val options = RequestOptions().centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                bitmap = BitmapFactory.decodeFile(path)
                partFile = convertPart(bitmap,"attachment")
                Glide.with(this).setDefaultRequestOptions(options).load(path).into(poto_category)
            }
    }

     private fun convertPart(bitmap: Bitmap, param:String):MultipartBody.Part{
         val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            System.currentTimeMillis().toString() +"_image.png")
         var os: OutputStream
         try {
             os = FileOutputStream(imageFile)
             bitmap.compress(Bitmap.CompressFormat.JPEG, 75, os)
             os.flush()
             os.close()
         }catch (e:Exception){
            e.printStackTrace()
         }
         var body:RequestBody = RequestBody.create(LinkApi.image,imageFile)
         var partBody:MultipartBody.Part = MultipartBody.Part.createFormData(param,imageFile.name,body)
         return partBody
     }

     private fun camera(){
         val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
         if(intent.resolveActivity(packageManager)!= null){
             var photoFile: File? = null
             try {
                 photoFile = createPath()
             }catch (e:Exception){
                 Log.d("exepction_camera: ", e.message)
             }
             if(photoFile != null){
                 val photoUri =FileProvider.getUriForFile(this@AddCategory,
                     "id.co.self.mystock.fileprovider",
                     photoFile)
                 intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
                 startActivityForResult(intent,IMAGE_CAPTURE_CODE)
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


    @Throws(Exception::class)
    private fun createPath():File{
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmms").format(Date())
        val imageFileName =  "JPEG_" + timeStamp + "_"
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
}
