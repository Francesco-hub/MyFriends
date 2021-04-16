package com.example.myfriends.Data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myfriends.model.BEFriend
import java.io.File
import java.io.FileOutputStream

class FriendDao_Impl(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), IFriendDao {
    private val TAG: String = "xyz"

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "Friend"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $DATABASE_NAME (id INTEGER PRIMARY KEY, name TEXT, address TEXT, locationLat DOUBLE, locationLon DOUBLE, phone TEXT, mail TEXT, website TEXT, birthday TEXT, isFavourite BIT, picture String)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + "friend")
        onCreate(db)
    }

    override fun getAll(): List<BEFriend> {
        val friendList: ArrayList<BEFriend> = ArrayList()
        val selectQuery = "SELECT  * FROM $DATABASE_NAME ORDER BY id"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: Int
        var name: String
        var address: String
        var mail: String
        var locationLat: Double
        var locationLon: Double
        var web: String
        var picture: String
        var birthday: String
        var phone: String
        var isFavourite: Boolean
        var isFavouriteInt: Int
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                name = cursor.getString(cursor.getColumnIndex("name"))
                mail = cursor.getString(cursor.getColumnIndex("mail"))
                address = cursor.getString(cursor.getColumnIndex("address"))
                locationLat = cursor.getDouble(cursor.getColumnIndex("locationLat"))
                locationLon = cursor.getDouble(cursor.getColumnIndex("locationLon"))
                web = cursor.getString(cursor.getColumnIndex("website"))
                phone = cursor.getString(cursor.getColumnIndex("phone"))
                birthday = cursor.getString(cursor.getColumnIndex("birthday"))
                picture = cursor.getString(cursor.getColumnIndex("picture"))
                isFavouriteInt = cursor.getInt(cursor.getColumnIndex("isFavourite"))
                isFavourite = isFavouriteInt != 0

                val friend = BEFriend(
                    id = id,
                    name = name,
                    address = address,
                    locationLat = locationLat,
                    locationLon = locationLon,
                    phone = phone,
                    mail = mail,
                    website = web,
                    birthday = birthday,
                    isFavorite = isFavourite,
                    picture = File(picture)
                )
                friendList.add(friend)
            } while (cursor.moveToNext())
        }
        return friendList
    }


    /*private fun getByCursor(cursor: Cursor): List<BEFriend> {
        val myDatabase = this.writableDatabase
        val result = ArrayList<BEFriend>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex("id"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
            } while (cursor.moveToNext())
        }
        return result
    } */

    override fun insert(f: BEFriend) {
        val myDatabase = this.writableDatabase
        val cv = ContentValues()
        cv.put("name", f.name)
        cv.put("address", f.address)
        cv.put("locationLat", f.locationLat)
        cv.put("locationLon", f.locationLon)
        cv.put("phone", f.phone)
        cv.put("mail", f.mail)
        cv.put("website", f.website)
        cv.put("birthday", f.birthday)
        cv.put("isFavourite", f.isFavorite)
        cv.put("picture", f.picture.toString())
        val result = myDatabase.insert("$DATABASE_NAME", null, cv)
        if (result > 0.toLong()) {
            f.id = result.toInt()
        } else Log.d(TAG, "Create friend: FAILED")
    }

    override fun update(f: BEFriend) {
        val myDatabase = this.writableDatabase
        val cv = ContentValues()
        cv.put("name", f.name)
        cv.put("address", f.address)
        cv.put("locationLat", f.locationLat)
        cv.put("locationLon", f.locationLon)
        cv.put("phone", f.phone)
        cv.put("mail", f.mail)
        cv.put("website", f.website)
        cv.put("birthday", f.birthday)
        cv.put("isFavourite", f.isFavorite)
        cv.put("picture", f.picture.toString())
        val whereClause = "id=?"
        val whereArgs = arrayOf((f.id).toString())
        myDatabase.update("$DATABASE_NAME", cv, whereClause, whereArgs)
    }

    override fun delete(id: Int) {
        val myDatabase = this.writableDatabase
        val whereClause = "id=?"
        val whereArgs = arrayOf((id).toString())
        myDatabase.delete("$DATABASE_NAME", whereClause, whereArgs)
    }
}
