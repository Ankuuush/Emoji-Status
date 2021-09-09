package com.ankush.emojistatus

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class UserViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private companion object{
        private const val TAG="MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        val query: CollectionReference = db.collection("users")
        val options: FirestoreRecyclerOptions<getUser> = FirestoreRecyclerOptions.Builder<getUser>()
            .setQuery(query, getUser::class.java)
            .setLifecycleOwner(this).build()
        val adapter=object:FirestoreRecyclerAdapter<getUser, UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
               val view= LayoutInflater.from(this@MainActivity).inflate(android.R.layout.simple_list_item_2,parent,false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: getUser) {
                val tvName:TextView=holder.itemView.findViewById(android.R.id.text1)
                val tvEmoji:TextView=holder.itemView.findViewById(android.R.id.text2)
                tvName.text=model.displayName
                tvEmoji.text=model.emoji
            }

        }
        rvUsers.adapter=adapter
        rvUsers.layoutManager=LinearLayoutManager(this)
    }
    data class getUser(
        val displayName:String?="",
        val emoji:String?=""
    )
    val db=Firebase.firestore

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.mILogot){
            Log.i(TAG,"Logout")
            auth.signOut()
            val logoutIntent=Intent(this,LoginActivity::class.java)
            logoutIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }else if(item.itemId==R.id.miEdit){
            showAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }
inner class EmojiFilter:InputFilter{
    override fun filter(p0: CharSequence?, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int): CharSequence {
        if (p0==null || p0.isBlank())
            return ""
        val validCharTypes= listOf(Character.NON_SPACING_MARK, // 6
            Character.DECIMAL_DIGIT_NUMBER, // 9
            Character.LETTER_NUMBER, // 10
            Character.OTHER_NUMBER, // 11
            Character.SPACE_SEPARATOR, // 12
            Character.FORMAT, // 16
            Character.SURROGATE, // 19
            Character.DASH_PUNCTUATION, // 20
            Character.START_PUNCTUATION, // 21
            Character.END_PUNCTUATION, // 22
            Character.CONNECTOR_PUNCTUATION, // 23
            Character.OTHER_PUNCTUATION, // 24
            Character.MATH_SYMBOL, // 25
            Character.CURRENCY_SYMBOL, //26
            Character.MODIFIER_SYMBOL, // 27
            Character.OTHER_SYMBOL).map{it.toInt()}
        for (inputChar in p0){
            val type=Character.getType(inputChar)
            if (!validCharTypes.contains(type)){
                Toast.makeText(this@MainActivity,"Only Emojis are Allowed",Toast.LENGTH_LONG).show()
                return ""
            }
        }
        return p0
    }
}
    private fun showAlertDialog() {
        val emojiFilter=EmojiFilter()
        val editText=EditText(this)
        val lengthFilter=InputFilter.LengthFilter(9)
        editText.filters= arrayOf(lengthFilter,emojiFilter)
        val dialog=AlertDialog.Builder(this)
            .setTitle("Update your Emojis")
            .setView(editText)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok",null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
            val emojisEntered=editText.text.toString()
            if (emojisEntered.isBlank()){
                Toast.makeText(this,"Cannot submit empty text",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val currentUser=auth.currentUser
            if (currentUser==null){
                Toast.makeText(this,"Please SignIn",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            db.collection("users").document(currentUser.uid)
                .update("emoji",emojisEntered)
            dialog.dismiss()
        }
    }
}