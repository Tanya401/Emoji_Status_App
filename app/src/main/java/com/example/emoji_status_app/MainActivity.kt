package com.example.emoji_status_app

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class User(
    val displayName: String = "",
    val emojis: String = ""
)


class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "MainActivity"
    }
  private lateinit var rvUsers:RecyclerView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

     rvUsers = findViewById(R.id.rvUsers)
        auth = Firebase.auth
        val db = Firebase.firestore
        val query = db.collection("users")   // taking all data from firestore
        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java).setLifecycleOwner(this).build()  // setting up the recycler view and lifecycle
        val adapter = object: FirestoreRecyclerAdapter<User, UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view =  LayoutInflater.from(this@MainActivity).inflate(android.R.layout.simple_list_item_2,parent,false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
              val tvName: TextView = holder.itemView.findViewById(android.R.id.text1)
                val tvEmojis: TextView = holder.itemView.findViewById(android.R.id.text2)
                tvName.text = model.displayName
                tvEmojis.text = model.emojis
            }
        }
        rvUsers.adapter =  adapter
        rvUsers.layoutManager = LinearLayoutManager(this)
    }


        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.menu_main, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.milogout) {
                Log.i(TAG, "Logout successfully")
                auth.signOut()
                val logoutIntent = Intent(this, login_activity::class.java)
                logoutIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(logoutIntent)
            }
            else if(item.itemId == R.id.miEdit){
                Log.i(TAG, "show alert dialog to edit status")
                showAlertDialog()
            }
            return super.onOptionsItemSelected(item)
        }
    inner class EmojiFilter: InputFilter{
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
          if(source == null || source.isBlank()){
              return ""
          }
            Log.i(TAG,"added text $source, it has length ${source.length} character")
            val validCharTypes = listOf(Character.SURROGATE, Character.NON_SPACING_MARK, Character.OTHER_SYMBOL).map{
                it.toInt()
            }
            //if invalid
            for(inputChar in source){
                val type = Character.getType(inputChar)
                Log.i(TAG,"Character type $type")
                if(!validCharTypes.contains(type)){
                    Toast.makeText(this@MainActivity,"Only emojis are allowed",Toast.LENGTH_SHORT).show()
                    return ""
                }
            }
            //if valid
            return source
        }

    }

    private fun showAlertDialog() {
        val editText = EditText(this)
        val emojiFilter = EmojiFilter()
        val lengthFilter = InputFilter.LengthFilter(9)
        editText.filters = arrayOf(lengthFilter,emojiFilter)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Update your emojis")
            .setView(editText)
            .setNegativeButton("cancel", null)
            .setPositiveButton("ok",null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            Log.i(TAG,"clicked on positive button!")
            val emojisEntered = editText.text.toString()
            if(emojisEntered.isBlank()){
                Toast.makeText(this,"Oops! U submitting empty text",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentUser = auth.currentUser
            if(currentUser == null){
                Toast.makeText(this,"No signed in user",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Update firestore with new enteries
            val db = Firebase.firestore
            db.collection("users").document(currentUser.uid)
                .update("emojis",emojisEntered)
            dialog.dismiss()
        }
    }
}
