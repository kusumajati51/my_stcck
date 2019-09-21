package id.co.self.mystock

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.co.self.mystock.session.PreferenceHelper
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class MainActivity : AppCompatActivity() {

    lateinit var pref: PreferenceHelper

    var id = "my_channel_01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pref = PreferenceHelper(this@MainActivity)

    }

    override fun onStart() {
        super.onStart()

        delete.setOnClickListener {
            pref.delete()
            val intent = Intent(this@MainActivity , RegistrationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        addType.setOnClickListener {
            val intent = Intent(this@MainActivity, AddCategory::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        addProduct.setOnClickListener {

        }



    }



}
