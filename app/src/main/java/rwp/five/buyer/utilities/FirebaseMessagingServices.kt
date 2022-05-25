package rwp.five.buyer.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import rwp.five.buyer.Main
import rwp.five.buyer.R

class FirebaseMessagingServices : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val intent = Intent(this, Main::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "EnsolChannel"
        val builder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.main_logo)

            .setContentTitle(p0.notification?.title)
            .setContentText(p0.notification?.body).setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, "Ensol", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setShowBadge(true)
        manager.createNotificationChannel(channel)

        manager.notify(1, builder.build())
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}