package com.example.myfriends

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfriends.Data.FriendDao_Impl
import com.example.myfriends.model.BEFriend
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_details.*
import java.io.File


class DetailsActivity : AppCompatActivity() {

    private var TAG = "xyz"
    private val REQUEST_CODE = 1
    private val RESULT_CREATE = 2
    private val RESULT_UPDATE = 3
    private val RESULT_DELETE = 4

    private lateinit var friendToEdit: BEFriend
    private var isCreation: Boolean = false
    private var locationChanged: Boolean = false
    private var currentLocationLat: Double = 0.0
    private var currentLocationLon: Double = 0.0
    private var myLocationListener: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        btn_camera.setOnClickListener { v -> openCamera() }
        btn_call.setOnClickListener { v -> onClickCall() }
        btn_sms.setOnClickListener { v -> onClickSms() }
        btn_mail.setOnClickListener { v -> onClickEmail() }
        btn_browser.setOnClickListener { v -> onClickBrowse() }
        btn_home.setOnClickListener { v -> onClickHome() }
        btn_map.setOnClickListener { v -> onClickMap() }

        var extras: Bundle = intent.extras!!
        isCreation = extras.getBoolean("isCreation")
        startListening()
        startListening()
        if (!isCreation) {
            friendToEdit = extras.getSerializable("friendForDetails") as BEFriend
            if (friendToEdit.picture != null) {
                friendPicture.setImageDrawable(Drawable.createFromPath(friendToEdit.picture?.absolutePath))
            }
            sw_Favourite.isChecked = friendToEdit.isFavorite
            field_name.setText(friendToEdit.name)
            field_phone.setText(friendToEdit.phone)
            field_address.setText(friendToEdit.address)
            field_birthday.setText(friendToEdit.birthday)
            field_mail.setText(friendToEdit.mail)
            field_web.setText(friendToEdit.website)
            btn_delete.setOnClickListener { v -> onClickDelete() }
            btn_save.setOnClickListener { v -> onClickSave() }
        } else {
            friendToEdit = BEFriend(1, "", "", 0.0, 0.0, "", "", "", "", false, null)
            textView.text = "Add Friend"
            btn_delete.text = "Cancel"
            btn_save.text = "Create"
            btn_delete.setOnClickListener { v -> onClickCancel() }
            btn_save.setOnClickListener { v -> onClickCreate() }
        }
        Toast.makeText(this, "id = ${friendToEdit.id}", Toast.LENGTH_LONG).show()
    }

    private fun onClickMap() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("friendName", friendToEdit.name)
        intent.putExtra("homeLocationLat", friendToEdit.locationLat)
        intent.putExtra("homeLocationLon", friendToEdit.locationLon)
        intent.putExtra("currentLocationLat", currentLocationLat)
        intent.putExtra("currentLocationLon", currentLocationLon)
        startActivity(intent)
    }

    private fun onClickBrowse() {
        var url = friendToEdit.website
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun onClickEmail() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        val receivers = arrayOf(friendToEdit.mail)
        intent.putExtra(Intent.EXTRA_EMAIL, receivers)
        intent.putExtra(Intent.EXTRA_TEXT, "Hello, this is the signature of the email")
        startActivity(intent)
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun onClickCreate() {
        val intent = Intent()
        if (validateInput()) {
            friendToEdit.name = field_name.text.toString()
            friendToEdit.address = field_address.text.toString()
            friendToEdit.phone = field_phone.text.toString()
            friendToEdit.mail = field_mail.text.toString()
            friendToEdit.website = field_web.text.toString()
            friendToEdit.birthday = field_birthday.text.toString()
            friendToEdit.isFavorite = sw_Favourite.isChecked
            intent.putExtra("newFriend", friendToEdit)
            setResult(RESULT_CREATE, intent)
            finish()
        }
    }

    private fun onClickCancel() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun onClickSave() {
        val intent = Intent()
        if (validateInput()) {
            friendToEdit.name = field_name.text.toString()
            friendToEdit.address = field_address.text.toString()
            friendToEdit.phone = field_phone.text.toString()
            friendToEdit.mail = field_mail.text.toString()
            friendToEdit.website = field_web.text.toString()
            friendToEdit.birthday = field_birthday.text.toString()
            friendToEdit.isFavorite = sw_Favourite.isChecked
            intent.putExtra("editedFriend", friendToEdit)
            setResult(RESULT_UPDATE, intent)
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (!field_name.text.isNullOrBlank() && !field_phone.text.isNullOrBlank()) return true
        showMissingInfo()
        return false
    }

    private fun showMissingInfo() {
        if (field_name.text.isNullOrBlank()) {
            field_name.error = "Please enter a valid Name"
        } else field_name.error = null
        if (field_phone.text.isNullOrBlank()) {
            field_phone.error = "Please enter a valid Number"
        } else field_phone.error = null
    }

    private fun onClickDelete() {
        val intent = Intent()
        intent.putExtra("idOfFriend", friendToEdit.id)
        setResult(RESULT_DELETE, intent)
        finish()
    }

    fun onClickCall() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel: ${friendToEdit.phone}")
        startActivity(intent)
    }

    private fun onClickSms() {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("sms: ${friendToEdit.phone}")
        sendIntent.putExtra("sms_body", "Hi, this is an sms")
        startActivity(sendIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                var newPicture = data?.extras?.getSerializable("newPicture") as File
                if (newPicture != null) {
                    friendPicture.setImageDrawable(Drawable.createFromPath(newPicture?.absolutePath))
                    friendToEdit.picture = newPicture
                }
            }
        }
    }

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun requestPermissions() {
        if (!isPermissionGiven()) {
            Log.d(TAG, "Permission to use GPS denied")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(permissions, 1)
        } else Log.d(TAG, "Permission to use GPS granted")
    }

    private fun isPermissionGiven(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return permissions.all { p -> checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED }
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun onClickGetLocation(view: View) {
        if (!isPermissionGiven()) {
            Toast.makeText(this, "No permission given", Toast.LENGTH_SHORT).show()
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null)
            Toast.makeText(this, "Location = ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        else Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show()
    }

    fun onClickHome() {
        requestPermissions()
        locationChanged = true
        startListening()
    }

    @SuppressLint("MissingPermission")
    private fun startListening() {
        if (!isPermissionGiven()) return
        if (myLocationListener == null)
            myLocationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {
                    if (locationChanged) {
                        friendToEdit.locationLat = location.latitude
                        friendToEdit.locationLon = location.longitude
                        Log.d(TAG, "hola")
                    } else {
                        currentLocationLat = location.latitude
                        currentLocationLon = location.longitude
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }
            }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0,
                0.0F,
                myLocationListener!!)
        stopListening()
    }

    @SuppressLint("MissingPermission")
    private fun stopListening() {
        if (myLocationListener == null) return

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(myLocationListener!!)
    }

    override fun onStop() {
        stopListening()
        super.onStop()
    }
}