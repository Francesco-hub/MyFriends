package com.example.myfriends

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import com.example.myfriends.Data.FriendDao_Impl
import com.example.myfriends.Data.IFriendDao
import com.example.myfriends.model.BEFriend
import com.example.myfriends.model.Friends
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 1
    private val TAG = "xyz"

    //var friendsLst: Array<BEFriend> = Friends().getAll()
    lateinit var myRepo: IFriendDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRepo = FriendDao_Impl(this)
        insertTestData()
        //setAdapterForListView(myRepo.getAll())

        lst_friends.adapter = FriendAdapter(this, myRepo.getAll().toTypedArray())
        lst_friends.setOnItemClickListener { parent, view, position, id -> onListItemClick(parent as ListView, view, position, id as Int) }
        //floatingActionButton.setOnClickListener{v -> onClickAdd()}
    }

    private fun insertTestData() {
        myRepo.insert(BEFriend(0, "a", "b", 1.1, 1.1, "c", "d", "e", "f", false, null))
        myRepo.insert(BEFriend(0, "h", "a", 1.1, 1.1, "w", "f", "m", "a", false, null))
        myRepo.insert(BEFriend(0, "a", "l", 1.1, 1.1, "r", "i", "d", "s", false, null))
    }

    var cache: List<BEFriend> = ArrayList<BEFriend>()

    /*  private fun setAdapterForListView(friends: List<BEFriend>) {
        Log.d(TAG, "Listview initialized")
        val asStrings = friends.map { f -> "${f.id}, ${f.name}" }
        val adapter: ListAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                asStrings.toTypedArray()
        )
        lst_friends.adapter = adapter
        lst_friends.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ -> onClickFriend(pos) }
    } */

    /*  private fun onClickFriend(pos: Int) {
        val friend = cache[pos]
        Toast.makeText(this, "${friend}", Toast.LENGTH_SHORT).show()
    } */

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
                onClickInsert()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onClickInsert() {
        myRepo.insert(BEFriend(0, field_name.text.toString(), " ", 0.0, 0.0, " ", " ", " ", " ", true, null))
        //setAdapterForListView(myRepo.getAll())
    }

/*    private fun onClickAdd() {
        val intent = Intent(this,DetailsActivity::class.java)
        intent.putExtra("isCreation", true)
        startActivityForResult(intent, REQUEST_CODE)
    }
*/

    fun onListItemClick(parent: ListView?, v: View?, position: Int, id: Int) {
        // position is in the list!
        // first get the name of the person clicked
        val friendForDetails = myRepo.getById(id) as Serializable
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
}
/*
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
                        lst_friends.adapter = setAdapterForListView(myRepo.getAll())
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
        //friendsLst[friendPosition] = updatedFriend
    }
 */