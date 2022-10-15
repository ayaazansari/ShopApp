package com.myshoppal.ui.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.myshoppal.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        @Suppress("DEPRECATION")
        Handler().postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        },1000)

        val typeFace : Typeface = Typeface.createFromAsset(assets,"Montserrat-Bold.ttf")
        tv_app_name.typeface = typeFace
    }
}