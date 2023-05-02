package com.lex.xeldoprojectmanagement.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.adapters.MembersListItemsAdapter
import com.lex.xeldoprojectmanagement.databinding.ActivityMembersBinding
import com.lex.xeldoprojectmanagement.databinding.DialogAddMemberBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Board
import com.lex.xeldoprojectmanagement.models.Users
import com.lex.xeldoprojectmanagement.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding

    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<Users>
    private var anyChangesMade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMembersBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar(binding.toolbarMembersActivity, resources.getString(R.string.members))

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    fun setupMembersListAdapter(list: ArrayList<Users>){
        //Initialize mAssignedMembersList by assigning to list gotten from the firebase class
        mAssignedMembersList = list
        hideProgressDialog()

        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter = MembersListItemsAdapter(this, list)
        binding.rvMembersList.adapter = adapter
    }

    fun memberDetails(user: Users){
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member ->{
                dialogAddMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogAddMember(){
        val dialogBinding: DialogAddMemberBinding = DialogAddMemberBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.tvAdd.setOnClickListener {
            val email = dialogBinding.etEmailSearchMember.text.toString()

            if (email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this, email)

            }else{
                showErrorSnackBar(resources.getString(R.string.please_enter_member_email))
            }
        }
        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user: Users){
        hideProgressDialog()
        anyChangesMade = true
        mAssignedMembersList.add(user)

        /**
         * To update the members list with the new added member,
         * we call the setupMemberListAdapter with the new mAssignedMemberList
         */
        setupMembersListAdapter(mAssignedMembersList)

        /**
         * Call SendNotificationToUserAsyncTask class to perfom the push notification
         * of member assigned succesfully to the member
         */
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) : AsyncTask<Any, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }
        override fun doInBackground(vararg p0: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null

            try {
                var url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod= "POST"

                // Set Request Property of the connection
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false

                var writer = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,
                    "You have been to the board" +
                            " by ${mAssignedMembersList[0].name}")

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                writer.writeBytes(jsonRequest.toString())
                writer.flush()
                writer.close()

                // In case something goes wrong in the process
                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(
                        InputStreamReader(inputStream)
                    )

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line+"\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException){
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            Log.e("JSON Response Result", result?: "")
        }

    }
}