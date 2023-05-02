package com.lex.xeldoprojectmanagement.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.activities.MainActivity
import com.lex.xeldoprojectmanagement.activities.SignInActivity
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.utils.Constants

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // To get where message is from
        Log.d(TAG, "FROM: ${remoteMessage.from}")

        // Check if message is not empty
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            /**
             * Get the title and message we created in the MembersActivity
             * SendNotificationToUserAsyncTask class
             */
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

            sendNotificationToUserPhone(title, message)
        }

        // Check if message contains a notification
        remoteMessage.notification.let {
            Log.d(TAG, "Message Notification Body: ${it?.body}")
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. This is called when the FCM registration
     * token is initially generated and the token is retrieved here
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "Refreshed token: $token")

        // To send messages to this application instance or
        // manage apps subscriptions on the server side, we send
        // the FCM registration token to the app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {

    }

    // To send a notication, this will be called
    private fun sendNotificationToUserPhone(title: String, message: String?) {
        /**
         * When the notification is clicked and the current user is active and logged in
         * send the app to the MainActivity and if not send to the SignIn Activity
         */
        val intent = if (FirestoreClass().getCurrentUserId().isNotEmpty()){
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, SignInActivity::class.java)
        }

        /**
         * To make sure that the activity are not overlapping each other or that too many
         * of the same activity are opened at the same time, so there will only be one
         * MainActivity opened at a time
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,
            0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(
            this, channelId
        ).setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) //Ensures notification is cancelled when clicked
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
            "Channel XeldoProject title",
            NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        const val TAG = "FirebaseMessageService"
    }

}