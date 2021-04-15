package com.example.myfriends.model

class Friends {

    var mFriends = arrayOf<BEFriend>(
            BEFriend(1, "Jonas", "casa", 0.0, 0.0, "1222", "aaa@aaa", "www.wow.com", "22/05/1997", true, null),
            BEFriend(1, "Carl", "casa", 0.0, 0.0, "1222", "aaa@aaa", "www.wow.com", "22/05/1997", true, null),
            BEFriend(1, "John", "casa", 0.0, 0.0, "1222", "aaa@aaa", "www.wow.com", "22/05/1997", true, null)

    )

    fun getAll(): Array<BEFriend> = mFriends

    //fun getAllNames(): Array<String> = mFriends.map { aFriend -> aFriend.name }.toTypedArray()
    fun updateData(friendPosition: Int, updatedFriend: BEFriend): Array<BEFriend> {
        mFriends[friendPosition] = updatedFriend
        return mFriends
    }

    fun deleteFriend(friendPosition: Int): Array<BEFriend> {
        var updatedList = mFriends.toMutableList()
        updatedList.removeAt(friendPosition)
        return updatedList.toTypedArray()
    }
}
