package com.myshoppal.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.myshoppal.R
import com.myshoppal.firestore.FirestoreClass
import com.myshoppal.models.User
import com.myshoppal.utils.Constants
import com.myshoppal.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var userDetails :User
    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            userDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        et_first_name.setText(userDetails.firstName)
        et_last_name.setText(userDetails.lastName)
        et_email.isEnabled = false
        et_email.setText(userDetails.email)

        if(userDetails.profileCompleted ==0){
            tv_title.text = getString(R.string.title_complete_profile)
            et_first_name.isEnabled = false
            et_last_name.isEnabled = false
        }
        else{
            setupActionBar()
            tv_title.text = getString(R.string.title_edit_profile)
            GlideLoader(this).loadUserPicture(userDetails.image, iv_user_photo)

            if(userDetails.mobile!=0L)
                et_mobile_number.setText(userDetails.mobile.toString())
            if(userDetails.gender == Constants.MALE)
                rb_male.isChecked = true
            else
                rb_female.isChecked = true
        }

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        btn_submit.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if(v!=null){
            when(v.id){
                R.id.iv_user_photo -> {
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        ==PackageManager.PERMISSION_GRANTED)
                    {
                        Constants.showImageChooser(this@UserProfileActivity)
                    }
                    else{
                         ActivityCompat.requestPermissions(
                             this,
                             arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                             Constants.READ_STORAGE_PERMISSION_CODE
                         )
                    }
                }

                R.id.btn_submit ->{
                    if(validateUserProfileDetails()){

                        showProgress(getString(R.string.please_wait))

                        if(mSelectedImageFileUri!=null)
                            FirestoreClass().uploadImageToCloudStorage(this,mSelectedImageFileUri,Constants.USER_PROFILE_IMAGE)
                        else
                            updateUserProfileDetails()
                    }
                }
            }
        }
    }

    private fun updateUserProfileDetails(){
        val userHashMap = HashMap<String,Any>()

        val firstName = et_first_name.text.toString().trim(){it<=' '}
        if(firstName!=userDetails.firstName){
            userHashMap[Constants.FIRST_NAME] = firstName
        }

        val lastName = et_last_name.text.toString().trim(){it<=' '}
        if(lastName!=userDetails.lastName){
            userHashMap[Constants.LAST_NAME] = lastName
        }

        val mobileNumber = et_mobile_number.text.toString().trim(){it<=' '}
        val gender = if(rb_male.isChecked) {
            Constants.MALE
        }
        else{
            Constants.FEMALE
        }

        if(gender.isNotEmpty() && gender!=userDetails.gender) {
            userHashMap[Constants.GENDER] = gender
        }

        userHashMap[Constants.COMPLETE_PROFILE] = 1

        if(mUserProfileImageUrl.isNotEmpty())
            userHashMap[Constants.IMAGE] = mUserProfileImageUrl

        if(mobileNumber.isNotEmpty() && mobileNumber!=userDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

        FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    fun userProfileUpdateSuccess(){
        hideProgressBar()

        Toast.makeText(this@UserProfileActivity,getString(R.string.msg_profile_update_success),Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this@UserProfileActivity)
            }
            else{
                Toast.makeText(
                    this,
                    getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == Constants.PICK_IMAGE_REQUEST_CODE){
                if(data!=null){
                    try{
                        mSelectedImageFileUri = data.data!!
                        GlideLoader(this).loadUserPicture(mSelectedImageFileUri!!,iv_user_photo)
                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this,getString(R.string.image_selection_failed),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateUserProfileDetails():Boolean {
        return when{
            (et_mobile_number.text.toString().trim(){it<=' '}.isNullOrEmpty()) ->{
                showErrorSnackBar(getString(R.string.err_msg_enter_mobile_number),true)
                false
            }
            else ->{
                true
            }
        }
    }

    fun imageUploadSuccess(imageURL:String){
        mUserProfileImageUrl = imageURL
        updateUserProfileDetails()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_user_profile_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        toolbar_user_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }
}