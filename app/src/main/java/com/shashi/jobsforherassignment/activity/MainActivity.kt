package com.shashi.jobsforherassignment.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.shashi.jobsforherassignment.R
import com.shashi.jobsforherassignment.base.BaseActivity
import com.shashi.jobsforherassignment.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var topic: String? =null
    private val AUTH_KEY =
        "key=AAAAH3b2sMI:APA91bG7KHbkfd04-YtPBNvHlaRO9PKte7ulWaXU1Q4-Zk3L6JquIKr5ZKk1FFdc1jQMniO2b51-gs5jVArNusxxFpZrPOISht2i99y5gKDOan1O75WViq8iggf9hch0_aEXXfeJA5Vx"
    private var token: String? = null
    private val TAG = "MyFirebaseToken"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val bundle = intent.extras
        if (bundle != null) {
            var tmp = ""
            for (key in bundle.keySet()) {
                val value = bundle[key]
                tmp += "$key: $value\n\n"
            }
            binding.txt.text = tmp
        }
        initView()

        binding.btnSubscribe.setOnClickListener(this)
        binding.btnUnSubscribe.setOnClickListener(this)
        binding.btnSendTopic.setOnClickListener(this)

        val myItems = resources.getStringArray(R.array.topics)
        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.select_dialog_item, myItems
        )
        binding.listTopic.prompt = "Click to Subscribe Topics"
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.listTopic.setAdapter(arrayAdapter)

        showHideTopicBtn()

    }

    fun showHideTopicBtn(){

        if (topic==null|| topic!!.isEmpty()) {
            binding.btnSendTopic.visibility = View.GONE
        }else binding.btnSendTopic.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        val bundle=intent.extras
        val samplename:String
        if(bundle!=null)
        {
            samplename = bundle.getString("click_action").toString()
            if (samplename.equals("Open_Login"))
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun sendWithOtherThread(type: String) {
        Thread { pushNotification(type) }.start()
    }
    private fun initView(){
        // Get token
        if ( checkGooglePlayServices() ) {
            // [START retrieve_current_token]
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, getString(R.string.token_error), task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token

                    // Log and toast
                    val msg = getString(R.string.token_prefix, token)
                    Log.d(TAG, msg)
                })

            // [END retrieve_current_token]
        } else {
            //You won't be able to send notifications to this device
            Log.w(TAG, "Device doesn't have google play services")
        }

    }

    private fun pushNotification(type: String) {
        val jPayload = JSONObject()
        val jNotification = JSONObject()
        val jData = JSONObject()
        try {
            jNotification.put("title", "Notification from " + topic)
            jNotification.put("body", "Firebase Cloud Messaging (App)")
            jNotification.put("sound", "default")
            jNotification.put("badge", "1")
            jNotification.put("click_action", "Open_" + topic)
            jNotification.put("icon", "ic_notification")
            when (type) {
                "topic" -> jPayload.put("to", "/topics/" + topic)
                else -> jPayload.put("to", token)
            }
            jPayload.put("priority", "high")
            jPayload.put("notification", jNotification)
            jPayload.put("data", jData)
            val url = URL("https://fcm.googleapis.com/fcm/send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", AUTH_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // Send FCM message content.
            val outputStream = conn.outputStream
            outputStream.write(jPayload.toString().toByteArray())

            // Read FCM response.
            val inputStream = conn.inputStream
            val resp = convertStreamToString(inputStream)
            val h = Handler(Looper.getMainLooper())
            h.post { binding.txt.text = resp }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next().replace(",", ",\n") else ""
    }
    private fun checkGooglePlayServices(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        // 2
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Error")
            // ask user to update google play services and manage the error.
            false
        } else {
            // 3
            Log.i(TAG, "Google play services updated")
            true
        }
    }

    override fun onClick(p0: View?) {
        val itemId = p0?.id
        when(itemId){
            R.id.btnSubscribe -> {
                topic = listTopic.getSelectedItem().toString()
                FirebaseMessaging.getInstance().subscribeToTopic(topic!!)
                binding.txt.text = "Subscribe to " + topic
                showHideTopicBtn()
            }
            R.id.btnUnSubscribe -> {
                topic = listTopic.getSelectedItem().toString()
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic!!)
                binding.txt.text = "UnSubscribe to " + topic
                topic = ""
                showHideTopicBtn()
            }
            R.id.btnSendTopic -> {
                sendWithOtherThread("topic")
            }
        }
    }
}