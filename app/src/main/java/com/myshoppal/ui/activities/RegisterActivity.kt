package com.myshoppal.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.myshoppal.R
import com.myshoppal.firestore.FirestoreClass
import com.myshoppal.models.User
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupActionBar()

        tv_login.setOnClickListener {
            onBackPressed()
        }

        btn_register.setOnClickListener{
            registerUser()
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_register_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back)
        }
        toolbar_register_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateRegisterDetails():Boolean{
        if(et_first_name.text.toString().trim{it<=' '}.isNullOrEmpty()){
            showErrorSnackBar(getString(R.string.err_msg_enter_first_name),true)
            return false
        }
        else if(et_last_name.text.toString().trim{it<=' '}.isNullOrEmpty()){
            showErrorSnackBar(getString(R.string.err_msg_enter_last_name),true)
            return false
        }
        else if(et_email.text.toString().trim{it<=' '}.isNullOrEmpty()){
            showErrorSnackBar(getString(R.string.err_msg_enter_email),true)
            return false
        }
        else if(et_password.text.toString().trim{it<=' '}.isNullOrEmpty()){
            showErrorSnackBar(getString(R.string.err_msg_enter_password),true)
            return false
        }
        else if(et_confirm_password.text.toString().trim{it<=' '}.isNullOrEmpty()){
            showErrorSnackBar(getString(R.string.err_msg_enter_confirm_password),true)
            return false
        }
        else if(!cb_terms_and_condition.isChecked){
            showErrorSnackBar(getString(R.string.err_msg_agree_terms_and_condition),true)
            return false
        }
        else if((et_password.text.toString().trim{it<=' '})!=(et_confirm_password.text.toString().trim{it<=' '})){
            showErrorSnackBar(getString(R.string.err_msg_password_and_confirm_password_mismatch),true)
            return false
        }
        else{
//            showErrorSnackBar(getString(R.string.register_success),false)
            return true
        }
    }

    private fun registerUser(){
        if(validateRegisterDetails()){
            showProgress("Please wait")
            val email = et_email.text.toString().trim(){it<=' '}
            val password = et_password.text.toString().trim(){it<=' '}

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task->
                        if(task.isSuccessful){
                            val firebaseUser:FirebaseUser = task.result!!.user!!

                            val user = User(
                                firebaseUser.uid,
                                et_first_name.text.toString().trim(){it<=' '},
                                et_last_name.text.toString().trim(){it<=' '},
                                et_email.text.toString().trim(){it<=' '}
                            )

                            FirestoreClass().registerUser(this@RegisterActivity,user)

//                            FirebaseAuth.getInstance().signOut()
//                            finish()
                        }
                        else{
                            hideProgressBar()
                            showErrorSnackBar(
                                task.exception!!.message.toString(),true
                            )
                        }
                    }
                )
        }
    }

    fun userRegistrationSuccess(){
        hideProgressBar()
        Toast.makeText(this,getString(R.string.register_success),Toast.LENGTH_SHORT).show()
    }
}