package com.example.myfriends.Data

import com.example.myfriends.model.BEFriend

interface IFriendDao {

    fun getAll(): List<BEFriend>

    fun insert(f: BEFriend)

    fun update(f: BEFriend)

    fun delete(id: Int)
}