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

    private lateinit var friendToEdit: BEFriend //The friend we are editing, received as an extra from MainActivity
    private var isCreation: Boolean = false //Used for checking if we are creating or updating a friend as the behaviour of the DetailsActivity changes depending on the functionality
    private var locationChanged: Boolean = false //Used for determining if the location has been changed
    private var currentLocationLat: Double = 0.0 //New variables for Current Location
    private var currentLocationLon: Double = 0.0
    private var myLocationListener: LocationListener? = null //Initialize the Location Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        btn_camera.setOnClickListener { v -> openCamera() } //Assign the method each of the buttons will call on click
        btn_call.setOnClickListener { v -> onClickCall() }
        btn_sms.setOnClickListener { v -> onClickSms() }
        btn_mail.setOnClickListener { v -> onClickEmail() }
        btn_browser.setOnClickListener { v -> onClickBrowse() }
        btn_home.setOnClickListener { v -> onClickHome() }
        btn_map.setOnClickListener { v -> onClickMap() }

        var extras: Bundle = intent.extras!! // We get the extras sent from the previous activity
        isCreation = extras.getBoolean("isCreation")
        startListening()
        startListening()
        if (!isCreation) { //Depending if we are creating a friend or not, we adapt the GUI.
            friendToEdit = extras.getSerializable("friendForDetails") as BEFriend
            if (friendToEdit.picture != null) {
                friendPicture.setImageDrawable(Drawable.createFromPath(friendToEdit.picture?.absolutePath))
            }
            else friendPicture.setImageResource(R.drawable.user_image)
            //Set the information in the Fields to the information of the previous friend
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
            //Set the fields and button functionality to be ready for friend creation
            friendToEdit = BEFriend(1, "", "", 0.0, 0.0, "", "", "", "", false, null)
            textView.text = "Add Friend"
            btn_delete.text = "Cancel"
            btn_save.text = "Create"
            btn_delete.setOnClickListener { v -> onClickCancel() }
            btn_save.setOnClickListener { v -> onClickCreate() }
        }
        Toast.makeText(this, "id = ${friendToEdit.id}", Toast.LENGTH_LONG).show()
    }

    private fun onClickMap() { //Method that will start the MapsActivity showing the address of our friend
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("friendName", friendToEdit.name)
        intent.putExtra("homeLocationLat", friendToEdit.locationLat)
        intent.putExtra("homeLocationLon", friendToEdit.locationLon)
        intent.putExtra("currentLocationLat", currentLocationLat)
        intent.putExtra("currentLocationLon", currentLocationLon)
        startActivity(intent)
    }

    private fun onClickBrowse() { //Method that will start the default browser and visit the webpage of our friend
        var url = "http://" +friendToEdit.website
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun onClickEmail() { //Method that will star the default Mail activity and create a new e-mail with the address of our friend
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        val receivers = arrayOf(friendToEdit.mail)
        intent.putExtra(Intent.EXTRA_EMAIL, receivers)
        intent.putExtra(Intent.EXTRA_TEXT, "Hello, this is the signature of the email")
        startActivity(intent)
    }

    private fun openCamera() { //Method that will open the camera activity and await for a picture file to be returned by it
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun onClickCreate() { //Method that will create a new friend and send it back to main activity
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

    private fun onClickCancel() { //Method called when clicking cancel, will set the result to cancel so that MainActivity receives it
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun onClickSave() { //Method called when updating a friend. It will send to MainActivity the updated Friend for it to be updated in Db
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

    private fun validateInput(): Boolean { //Method that will verify that all information is correct before allowing the user to Create/Update friends
        if (!field_name.text.isNullOrBlank() && !field_phone.text.isNullOrBlank()&& !field_address.text.isNullOrBlank()&& !field_birthday.text.isNullOrBlank()&& !field_mail.text.isNullOrBlank()&& !field_web.text.isNullOrBlank()) return true
        showMissingInfo()
        return false
    }

    private fun showMissingInfo() { //Method that will display the missing info if needed
        if (field_name.text.isNullOrBlank()) {
            field_name.error = "Please enter a valid Name"
        } else field_name.error = null
        if (field_phone.text.isNullOrBlank()) {
            field_phone.error = "Please enter a valid Number"
        } else field_phone.error = null
        if (field_address.text.isNullOrBlank()) {
            field_address.error = "Please enter a valid Address"
        } else field_address.error = null
        if (field_birthday.text.isNullOrBlank()) {
            field_birthday.error = "Please enter a valid Birthday"
        } else field_birthday.error = null
        if (field_mail.text.isNullOrBlank()) {
            field_mail.error = "Please enter a valid E-mail"
        } else field_mail.error = null
        if (field_web.text.isNullOrBlank()) {
            field_web.error = "Please enter a valid Website"
        } else field_web.error = null

    }

    private fun onClickDelete() { //Method that will send to MainActivity the RESULT_DELETE, Which will perform the deletion in Db
        val intent = Intent()
        intent.putExtra("idOfFriend", friendToEdit.id)
        setResult(RESULT_DELETE, intent)
        finish()
    }

    fun onClickCall() { //Method that will open the default app for calling with the number of our friend
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel: ${friendToEdit.phone}")
        startActivity(intent)
    }

    private fun onClickSms() { //Method that will open the default app for sending a SMS with the number of our friend
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("sms: ${friendToEdit.phone}")
        sendIntent.putExtra("sms_body", "Hi, this is an sms")
        startActivity(sendIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Method that will check that the CameraActivity will return a picture and assign it to our friend as well as display it in our DetailsActivity
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

    private fun requestPermissions() { //Method that will request persmission for using the GPS when needed
        if (!isPermissionGiven()) {
            Log.d(TAG, "Permission to use GPS denied")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(permissions, 1)
        } else Log.d(TAG, "Permission to use GPS granted")
    }

    private fun isPermissionGiven(): Boolean { //Method that will ensure that the permission has been given to use the GPS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return permissions.all { p -> checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED }
        }
        return true
    }

    @SuppressLint("MissingPermission")
    //When the application was finished we realized that this method was not used but we did not manage to get the set current location functionality to work correctly so we decided to let it here commented for further development
   /* fun onClickGetLocation(view: View) {
        if (!isPermissionGiven()) {
            Toast.makeText(this, "No permission given", Toast.LENGTH_SHORT).show()
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null)
            Toast.makeText(this, "Location = ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        else Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show()
    }*/

    fun onClickHome() { //Method that will set the Friend address to the current location
        requestPermissions()
        locationChanged = true
        startListening()
    }

    @SuppressLint("MissingPermission")
    private fun startListening() { //Method that will listen and set the location
        if (!isPermissionGiven()) return
        if (myLocationListener == null)
            myLocationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {
                    if (locationChanged) {
                        friendToEdit.locationLat = location.latitude
                        friendToEdit.locationLon = location.longitude
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
    private fun stopListening() { //Method that will ensure that our listener stops listening
        if (myLocationListener == null) return

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(myLocationListener!!)
    }

    override fun onStop() {
        stopListening()
        super.onStop()
    }
}