package com.example.autoelite_android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.autoelite_android.MainActivity
import com.example.autoelite_android.R
import com.example.autoelite_android.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoEliteMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "autoelite_notificaciones"
        const val CHANNEL_NAME = "AutoElite"
        const val CHANNEL_DESC = "Notificaciones de citas, reparaciones y facturas"

         //Llamar al arrancar la app para crear el canal de notificaciones
        fun crearCanal(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                }

                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }

         //Envía el token FCM actual al backend.
         //Llamar después del login y cuando se refresque el token.
        fun enviarTokenAlBackend(token: String) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.registrarFcmToken(
                        uid = uid,
                        body = mapOf("fcmToken" to token)
                    )
                    if (response.isSuccessful) {
                        println("FCM: Token enviado al backend OK")
                    } else {
                        println("FCM: Error enviando token: ${response.code()}")
                    }
                } catch (e: Exception) {
                    println("FCM: Error de red enviando token: ${e.message}")
                }
            }
        }
    }

     //Se llama cuando Firebase genera un nuevo token (primera vez o refresco).
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM: Nuevo token generado: ${token.take(20)}...")
        enviarTokenAlBackend(token)
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val titulo = message.notification?.title
            ?: message.data["titulo"]
            ?: "AutoElite"

        val cuerpo = message.notification?.body
            ?: message.data["cuerpo"]
            ?: ""

        val pantalla = message.data["pantalla"]  // "citas", "reparaciones", "facturacion"

        mostrarNotificacion(titulo, cuerpo, pantalla)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, pantalla: String?) {
        // Intent que abre la app al pulsar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            pantalla?.let { putExtra("pantalla", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}