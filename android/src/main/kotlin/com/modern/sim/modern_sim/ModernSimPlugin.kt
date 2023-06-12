package com.modern.sim.modern_sim

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.Registrar

/** ModernSimPlugin */
class ModernSimPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
    private val REQUEST_CODE_SEND_SMS = 205

      override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "modern_sim")
    channel.setMethodCallHandler(this)
  }
//    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
//        setupCallbackChannels(flutterPluginBinding.binaryMessenger)
//    }
//
//    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
//        teardown()
//    }
//
//    private fun setupCallbackChannels(messenger: BinaryMessenger) {
//        mChannel = MethodChannel(messenger, "flutter_sms")
//        mChannel.setMethodCallHandler(this)
//    }
//
//    private fun teardown() {
//        mChannel.setMethodCallHandler(null)
//    }
//
//    // V1 embedding entry point. This is deprecated and will be removed in a future Flutter
//    // release but we leave it here in case someone's app does not utilize the V2 embedding yet.
//    companion object {
//        @JvmStatic
//        fun registerWith(registrar: Registrar) {
//            val inst = FlutterSmsPlugin()
//            inst.activity = registrar.activity()
//            inst.setupCallbackChannels(registrar.messenger())
//        }
//    }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull  result: MethodChannel.Result) {
     when (call.method) {
            "getSimInfo" -> getSimInfo(result)
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "sendSMS" -> {
                val message = call.argument<String?>("message") ?: ""
                val phone = call.argument<String?>("phone") ?: ""
                val subId: Int? = call.argument<Int?>("subId")
                val externalId: Int? = call.argument<Int?>("externalId")
                val localId: Int? = call.argument<Int?>("localId")
                sendSMS(result, phone, message, subId, externalId, localId)
            }
            else -> result.notImplemented()
        }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

   private fun getSimInfo(result: MethodChannel.Result){
        val context = activity!!.applicationContext // shortcut to use context
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subManager: SubscriptionManager = SubscriptionManager.from(context)
                val localList: List<SubscriptionInfo> = subManager.activeSubscriptionInfoList
                val allInfo = "{\"data\": [${localList.joinToString { e ->  subJson(e) + if (localList.last() == e) "" else ""}} ], \"type\":\"full\"}"
                result.success(allInfo)

            } else {
                result.error("too_low_android_version", "Target < 22 is not supported", "")
            }
        }catch (e: Exception ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val subManager: SubscriptionManager = SubscriptionManager.from(context)
                val b0 = subManager.getSubscriptionIds(0)
                val b1 = subManager.getSubscriptionIds(1);
                var res = """
                    {
                        "type": "low",
                        "data":
                            [
                            """
                if (b0!=null && b0.isNotEmpty()){
                    res += """ { "slotIndex": "0", "subId":"${b0[0]}" } """
                }
                if (b0!=null && b0.isNotEmpty() && b1!=null && b1.isNotEmpty()) {
                    res+=""","""
                }
                if (b1!=null && b1.isNotEmpty()){
                    res += """ { "slotIndex": "1", "subId":"${b1[0]}" } """

                }
                 res+="""
                            ]
                        }
                """.trimIndent()

                result.success(res)
            } else {
                result.error("too_low_android_version_no_permission", "Target < 29 is not supported", "")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun subJson(e: SubscriptionInfo): String {
        return "{" +
                "\"slotIndex\": \"${e.simSlotIndex}\"," +
                "\"number\": \"${e.number}\"," +
                "\"subId\": \"${e.subscriptionId}\"," +
                "\"carrierName\": \"${e.carrierName}\"," +
                "\"displayName\": \"${e.displayName}\"" +
                "}"
    }

    private fun sendSMS(result: MethodChannel.Result, phones: String, message: String, subId: Int?, externalId: Int?, localId: Int?) {
        val context = activity!!.applicationContext // shortcut to use context

//        if (ContextCompat.checkSelfPermission(context,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            val SENT_ACTION = "SMS_SENT_ACTION"
            val DELIVERED_ACTION = "SMS_DELIVERED_ACTION"
            val intentSentAction = Intent(SENT_ACTION);
            intentSentAction.putExtra("externalId", externalId.toString())
            val intentDeliveredAction = Intent(DELIVERED_ACTION);
            intentDeliveredAction.putExtra("externalId", externalId.toString())

            val sentIntent = PendingIntent.getBroadcast(activity, 0, intentSentAction, PendingIntent.FLAG_IMMUTABLE)
            val deliveredIntent = PendingIntent.getBroadcast(activity, 0, intentDeliveredAction, PendingIntent.FLAG_IMMUTABLE)


            val numbers = phones.split(";")


            val mSmsManager: SmsManager? =
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && subId != null) {
                // Android 5.1+ Dual Sim
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } else {
                // Before Android 5.1. NO DUAL SIM
                SmsManager.getDefault()
            }



            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("SMS ", "sent" + intent?.getStringExtra("externalId") + " ------>" + externalId)
                    channel.invokeMethod("sendComplete", "$resultCode;$externalId;$localId")
                    context!!.unregisterReceiver(this)
                }
            }, IntentFilter(SENT_ACTION))

            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("SMS ", "delivered " + intent?.getStringExtra("externalId") + " ------>" + externalId)
                    channel.invokeMethod("deliverComplete", "$resultCode;$externalId;$localId")
                    context!!.unregisterReceiver(this)
                }
            }, IntentFilter(DELIVERED_ACTION))


            // send sms
            for (num in numbers) {
                Log.d("Flutter SMS", "msg.length() : " + message.toByteArray().size)
                if (message.toByteArray().size > 80) {
                    val partMessage = mSmsManager!!.divideMessage(message)
                    mSmsManager.sendMultipartTextMessage(num, null, partMessage, arrayListOf(sentIntent), arrayListOf(deliveredIntent))
                } else {
                    mSmsManager!!.sendTextMessage(num, null, message, sentIntent, deliveredIntent)
                }
            }

            result.success("{\"data\":${externalId}, \"result\":\"success\"}")
//        } else {
//            // do your getLocation here
//            result.error("no_permission", "No ACCESS_COARSE_LOCATION", "No ACCESS_COARSE_LOCATION");
//        }
    }
}
