package com.example.myfriends.Data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myfriends.MainActivity
import com.example.myfriends.model.BEFriend

class FriendDao_Impl : IFriendDao {

    val TAG: String = "xyz"
    var myDatabase: SQLiteDatabase

    constructor(context: Context) {
        val openHelper = MyOpenHelper(context)
        myDatabase = openHelper.writableDatabase
    }

    override fun getAll(): List<BEFriend> {
        val query = "SELECT * FROM friend ORDER BY id"
        val cursor = myDatabase.rawQuery(query, null)
        val result = getByCursor(cursor)
        Log.d(TAG, "DAO getAll() returned: ${result.size} friends")
        return result
    }

    override fun getById(id: Int): List<BEFriend> {
        val query = "SELECT * FROM friend WHERE id LIKE id"
        val cursor = myDatabase.rawQuery(query, null)
        val result = getByCursor(cursor)
        Log.d(TAG, "DAO getById() returned: ${result.size} friends")
        return result
    }

    private fun getByCursor(cursor: Cursor): List<BEFriend> {
        val result = ArrayList<BEFriend>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex("id"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
            } while (cursor.moveToNext())
        }
        return result
    }

    override fun insert(f: BEFriend) {
        val cv = ContentValues()
        cv.put("name", f.name)
        cv.put("birthday", f.birthday)
        val result = myDatabase.insert("friend", null, cv)
        if (result > 0.toLong()) {
            f.id = result.toInt()
            Log.d(TAG, "Create friend - id given: $result")
        } else Log.d(TAG, "Create friend: FAILED")
    }

    override fun update(f: BEFriend) {
        TODO("Not yet implemented")
    }

    override fun delete(f: BEFriend) {
        myDatabase.delete("friend", null, null)
    }

    inner class MyOpenHelper(context: Context) :
            SQLiteOpenHelper(context, "friendDB", null, 1) {
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL("CREATE TABLE Friend(id INTEGER PRIMARY KEY, name TEXT, address TEXT, locationLat DOUBLE, locationLon DOUBLE, phone TEXT, mail TEXT, website TEXT, birthday TEXT, isFavourite BIT, picture FILE)")
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            onCreate(db)
        }
    }
}