package com.myshoppal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.myshoppal.R
import com.myshoppal.firestore.FirestoreClass
import com.myshoppal.models.User
import com.myshoppal.utils.Constants
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tv_register.setOnClickListener(this)
        btn_login.setOnClickListener(this)
        tv_forgot_password.setOnClickListener(this)
    }


    fun userLoggedInSuccess(user: User) {
        hideProgressBar()
        if(user.profileCompleted==1)
        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        else if(user.profileCompleted==0) {
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS,user)
            startActivity(intent)
        }
        finish()
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.tv_forgot_password -> {
                    startActivity(Intent(this, ForgotPassword::class.java))
                }
                R.id.btn_login -> {
                    loginRegisteredUser()
                }
                R.id.tv_register -> {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            (et_email.text.toString().trim { it <= ' ' }.isNullOrEmpty()) -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }
            et_password.text.toString().trim { it <= ' ' }.isNullOrEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun loginRegisteredUser() {
        if (validateLoginDetails()) {
            showProgress(getString(R.string.please_wait))
            val email = et_email.text.toString().trim { it <= ' ' }
            val pass = et_password.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful)
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    else {
                        hideProgressBar()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
}
