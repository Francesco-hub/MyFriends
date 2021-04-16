package com.example.myfriends.model

import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.Serializable

class BEFriend(
        var id: Int,
        var name: String,
        var address: String,
        var locationLat: Double,
        var locationLon: Double,
        var phone: String,
        var mail: String,
        var website: String,
        var birthday: String,
        var isFavorite: Boolean,
        var picture: File?) : Serializable

