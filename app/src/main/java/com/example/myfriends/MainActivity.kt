package com.example.myfriends

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import com.example.myfriends.Data.FriendDao_Impl
import com.example.myfriends.Data.IFriendDao
import com.example.myfriends.model.BEFriend
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 1
    private val RESULT_CREATE = 2
    private val RESULT_UPDATE = 3
    private val RESULT_DELETE = 4

    private lateinit var myRepo: IFriendDao
    private lateinit var updatedList: List<BEFriend>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRepo = FriendDao_Impl(this)
        insertTestData()
        updatedList = myRepo.getAll().toMutableList()
        lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray())
        lst_friends.setOnItemClickListener { parent, view, position, id -> onListItemClick(parent as ListView, view, position) }
    }

    private fun insertTestData() {
        myRepo.insert(BEFriend(0, "Spiderman", "New York",  40.6643,  -73.9385, "123456", "spider@man", "spiderman.com", "28/12/1965", true, null))
        myRepo.insert(BEFriend(0, "Ironman", "Kansas",  39.1225, -94.7418, "456789", "iron@man", "ironman.dk", "29/05/1970", false, null))
        myRepo.insert(BEFriend(0, "Antman", "California", 37.25022, -119.75126, "123798", "ant@man", "antman.net", "15/08/2001", false, null))
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

    fun onListItemClick(parent: ListView?, v: View?, position: Int) {
        // position is in the list!
        // first get the name of the person clicked
        val friendForDetails = updatedList[position]
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("friendForDetails", friendForDetails)
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
            var friendId: Int?
            var updatedFriend: BEFriend
            var newFriend: BEFriend
            if (resultCode == RESULT_UPDATE) {
                updatedFriend = data?.extras?.getSerializable("editedFriend") as BEFriend
                if (updatedFriend != null) {
                    myRepo.update(updatedFriend)
                    updatedList = myRepo.getAll().toMutableList()
                    lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray())
                }
            }
            if (resultCode == RESULT_CREATE) {
                newFriend= data?.extras?.getSerializable("newFriend") as BEFriend
                myRepo.insert(newFriend)
                updatedList = myRepo.getAll().toMutableList()
                lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray())
            }
            if (resultCode == RESULT_DELETE) {
                friendId = data?.extras?.getInt("idOfFriend")
                if (friendId!! >= 0) {
                    myRepo.delete(friendId)
                    updatedList = myRepo.getAll().toMutableList()
                    lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray())
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelling...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}