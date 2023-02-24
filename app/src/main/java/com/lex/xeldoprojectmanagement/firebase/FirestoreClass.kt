package com.lex.xeldoprojectmanagement.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.lex.xeldoprojectmanagement.activities.SignInActivity
import com.lex.xeldoprojectmanagement.activities.SignUpActivity
import com.lex.xeldoprojectmanagement.models.Users
import com.lex.xeldoprojectmanagement.utils.Constants

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: Users){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                e ->
                Log.e(activity.javaClass.simpleName, "Error registering account")
            }
    }

    fun signInUser(activity: SignInActivity){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                run {
                    val loggedInUser = document.toObject(Users::class.java)

                    if (loggedInUser != null) {
                        activity.signInSuccess(loggedInUser)
                    }
                }
            }.addOnFailureListener {
                    e ->
                Log.e(activity.javaClass.simpleName, "Error signing in")
            }
    }

    fun getCurrentUserId(): String{

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

}