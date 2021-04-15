package com.example.myfriends

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import com.example.myfriends.model.BEFriend
import com.example.myfriends.model.Friends
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 1

    var friendsLst: Array<BEFriend> = Friends().getAll()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lst_friends.adapter = FriendAdapter(this, friendsLst)
        lst_friends.setOnItemClickListener { parent, view, position, id -> onListItemClick(parent as ListView, view, position, id) }
        //floatingActionButton.setOnClickListener{v -> onClickAdd()}
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Inflate the menu. Add items to the action bar if it is present
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                val intent = Intent(this, DetailsActivity::class.java)
                intent.putExtra("isCreation", true)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }
/*
    private fun onClickAdd() {
        val intent = Intent(this,DetailsActivity::class.java)
        intent.putExtra("isCreation", true)
        startActivityForResult(intent, REQUEST_CODE)
    }
    */

    fun onListItemClick(parent: ListView?, v: View?, position: Int, id: Long) {
        // position is in the list!
        // first get the name of the person clicked
        val friendForDetails = friendsLst[position]
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("friendForDetails", friendForDetails)
        intent.putExtra("positionOfFriend", position)
        intent.putExtra("isCreation", false)
        startActivityForResult(intent, REQUEST_CODE)
    }

    internal class FriendAdapter(context: Context, private val friends: Array<BEFriend>) : ArrayAdapter<BEFriend>(context, 0, friends) {
        private val colours = intArrayOf(
                Color.parseColor("#AAAAAA"),
                Color.parseColor("#CCCCCC")
        )

        override fun getView(position: Int, v: View?, parent: ViewGroup): View {
            var v1: View? = v
            if (v1 == null) {
                val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                        as LayoutInflater
                v1 = li.inflate(R.layout.cell_extended, null)
            }
            val resView: View = v1!!
            resView.setBackgroundColor(colours[position % colours.size])
            val f = friends[position]
            val nameView = resView.findViewById<TextView>(R.id.tvNameExt)
            val phoneView = resView.findViewById<TextView>(R.id.tvPhoneExt)
            val favoriteView = resView.findViewById<ImageView>(R.id.imgFavoriteExt)
            val friendImage = resView.findViewById<ImageView>(R.id.imgFriendPicture)
            nameView.text = f.name
            phoneView.text = f.phone
            favoriteView.setImageResource(if (f.isFavorite) R.drawable.ok else R.drawable.notok)
            friendImage.setImageDrawable(Drawable.createFromPath(f.picture?.absolutePath))
            return resView
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            var friendPosition: Int?
            var updatedFriend: BEFriend?
            var newFriend: BEFriend?
            if (resultCode == RESULT_OK) {
                if (data?.extras?.getBoolean("isCreation") == false) {
                    friendPosition = data?.extras?.getInt("positionOfFriend")
                    updatedFriend = data?.extras?.getSerializable("editedFriend") as BEFriend
                    if (friendPosition != null) {
                        updateData(friendPosition, updatedFriend)
                        lst_friends.adapter = FriendAdapter(this, friendsLst)
                    }
                } else {
                    newFriend = data?.extras?.getSerializable("newFriend") as BEFriend
                    var updatedList = friendsLst.toMutableList()
                    updatedList.add(newFriend)
                    friendsLst = updatedList.toTypedArray()
                    lst_friends.adapter = FriendAdapter(this, friendsLst)
                }
            }
            if (resultCode == RESULT_FIRST_USER) {
                friendPosition = data?.extras?.getInt("positionOfFriend")
                if (friendPosition != null) {
                    var updatedList = friendsLst.toMutableList()
                    updatedList.removeAt(friendPosition)
                    friendsLst = updatedList.toTypedArray()
                    lst_friends.adapter = FriendAdapter(this, friendsLst)
                }
            }
            if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    private fun updateData(friendPosition: Int, updatedFriend: BEFriend) {
        friendsLst[friendPosition] = updatedFriend
    }
}