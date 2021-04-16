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
    //Request and Result Codes for handling Intents
    private val REQUEST_CODE = 1
    private val RESULT_CREATE = 2
    private val RESULT_UPDATE = 3
    private val RESULT_DELETE = 4

    private lateinit var myRepo: IFriendDao //Declare IFriendDao
    private lateinit var updatedList: List<BEFriend> //Create a List of Friends that we will use in our ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRepo = FriendDao_Impl(this)
        insertTestData()
        updatedList = myRepo.getAll().toMutableList() //updatedList is filled with Friends from Db
        lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray()) //We set the content of FriendAdapter to be the UpdatedList of friends
        lst_friends.setOnItemClickListener { parent, view, position, id -> onListItemClick(parent as ListView, view, position) } //we define the method that will be called when clicking a friend in a list.
    }

    private fun insertTestData() { //We create some mock data inside our Db
        myRepo.insert(BEFriend(0, "Spiderman", "New York",  40.6643,  -73.9385, "123456", "spider@man", "spiderman.com", "28/12/1965", true, null))
        myRepo.insert(BEFriend(0, "Ironman", "Kansas",  39.1225, -94.7418, "456789", "iron@man", "ironman.dk", "29/05/1970", false, null))
        myRepo.insert(BEFriend(0, "Antman", "California", 37.25022, -119.75126, "123798", "ant@man", "antman.net", "15/08/2001", false, null))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Inflate the menu. Add items to the action bar if it is present
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Called when clicking Create new friend. Will star detailsActivity expecting a result and return the created friend
        when (item.itemId) {
            R.id.action_add -> {
                val intent = Intent(this, DetailsActivity::class.java)
                intent.putExtra("isCreation", true)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onListItemClick(parent: ListView?, v: View?, position: Int) { //Method that will be called when a Friend is clicked. Opens a new DetailsActivity for editing the friend. Receives the updated friend and then sends the update to Database
        // position is in the list!
        // first get the name of the person clicked
        val friendForDetails = updatedList[position]
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("friendForDetails", friendForDetails)
        intent.putExtra("isCreation", false)
        startActivityForResult(intent, REQUEST_CODE)
    }

    internal class FriendAdapter(context: Context, private val friends: Array<BEFriend>) : ArrayAdapter<BEFriend>(context, 0, friends) { //Friend Adapter that will create a new Extended Cell for each friend in the updatedLst
        private val colours = intArrayOf(
                Color.parseColor("#A6D9F7"),
                Color.parseColor("#B084CC")
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
            favoriteView.setImageResource(if (f.isFavorite) R.drawable.fav_icon else R.drawable.notok)
            friendImage.setImageDrawable(Drawable.createFromPath(f.picture?.absolutePath))
            return resView
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Method called when receiving results from
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            var friendId: Int?
            var updatedFriend: BEFriend
            var newFriend: BEFriend
            if (resultCode == RESULT_UPDATE) { //Result received when the friend is updated
                updatedFriend = data?.extras?.getSerializable("editedFriend") as BEFriend
                if (updatedFriend != null) {
                    myRepo.update(updatedFriend) //update the friend in Db
                    updatedList = myRepo.getAll().toMutableList() //Update our local Friend Lst with the updated friends
                    lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray()) //Update adapter with our new list
                }
            }
            if (resultCode == RESULT_CREATE) { //Result received when the friend is Created
                newFriend= data?.extras?.getSerializable("newFriend") as BEFriend
                myRepo.insert(newFriend) //create the friend in Db
                updatedList = myRepo.getAll().toMutableList() //Update our local Friend Lst with the new friends
                lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray()) //Update adapter with our new list
            }
            if (resultCode == RESULT_DELETE) { //Result received when the friend is Deleted
                friendId = data?.extras?.getInt("idOfFriend")
                if (friendId!! >= 0) {
                    myRepo.delete(friendId)//delete friend in Db
                    updatedList = myRepo.getAll().toMutableList() //Update our local Friend Lst with the friends we keep
                    lst_friends.adapter = FriendAdapter(this, updatedList.toTypedArray()) //Update adapter with our new list
                }
            }
            if (resultCode == RESULT_CANCELED) { //Result received when the DetailsActivity does not create, delete or update a new friend
                Toast.makeText(this, "Cancelling...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}