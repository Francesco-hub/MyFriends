package com.example.myfriends.Data

import com.example.myfriends.model.BEFriend

interface IFriendDao { //Interface for Db. All methods are implemented in FriendDao_Impl.kt

    fun getAll(): List<BEFriend>

    fun insert(f: BEFriend)

    fun update(f: BEFriend)

    fun delete(id: Int)
}