package com.example.emoji_status_app

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class login_activity : AppCompatActivity() {

    private lateinit var btnSignIn: SignInButton
    private companion object{
        private const val tag = "login_activity"
        private const val RC_GOOGLE_SIGN_IN = 4926
    }
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
     // whether go to main activity or try again
        if(user == null){
            Log.w(TAG,"Authentication failed! Try Again!")
            return
        }
        startActivity(Intent(this, MainActivity::class.java!!))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

         btnSignIn = findViewById(R.id.btnSignIn)
        auth = Firebase.auth

        //configure google sign-in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

         val client = GoogleSignIn.getClient(this, gso)

          btnSignIn.setOnClickListener {
              val signInintent = client.signInIntent
              startActivityForResult(signInintent,RC_GOOGLE_SIGN_IN)

          }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

       if(requestCode == RC_GOOGLE_SIGN_IN){
           val task = GoogleSignIn.getSignedInAccountFromIntent(data)
           try{
               val account = task.getResult(ApiException::class.java)!!
               Log.d(TAG,"firbaseAuthwithGoogle:"+account.id)
               firebaseAuthWithGoogle(account.idToken!!)
           } catch (e:ApiException){
               Log.w(TAG,"Google Sign In failed", e)

           }
       }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }
}