package com.example.tvvideocall

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.webrtc.SurfaceViewRenderer

class CallActivity : AppCompatActivity() {
    private lateinit var webRtcClient: WebRtcClient
    private lateinit var localId: String
    private lateinit var remoteId: String
    private lateinit var localView: SurfaceViewRenderer
    private lateinit var remoteView: SurfaceViewRenderer
    private var isCaller = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        localView = findViewById(R.id.local_video_view)
        remoteView = findViewById(R.id.remote_video_view)
        val btnAccept = findViewById<Button?>(R.id.btn_accept)
        val btnHangup = findViewById<Button?>(R.id.btn_hangup)

        localId = intent.getStringExtra("localId") ?: "local-" + System.currentTimeMillis().toString()
        remoteId = intent.getStringExtra("remoteId") ?: "peer-123"
        isCaller = intent.getBooleanExtra("isCaller", false)

        webRtcClient = WebRtcClient(this, localId, remoteId) { remoteTrack ->
            // attach remote track to remoteView on UI thread
            runOnUiThread {
                remoteTrack?.addSink(remoteView)
            }
        }

        // start local preview
        webRtcClient.start(localView)

        FirebaseSignaling.connect(localId, object: FirebaseSignaling.SignalingCallback {
            override fun onOffer(from: String, sdp: String) {
                // incoming offer (we are callee)
                runOnUiThread {
                    btnAccept?.visibility = View.VISIBLE
                }
                // store incoming offer; actual handling happens when user accepts
                incomingOfferFrom = from
                incomingOfferSdp = sdp
            }

            override fun onAnswer(from: String, sdp: String) {
                // set remote answer
                webRtcClient.onRemoteAnswer(sdp)
            }

            override fun onIceCandidate(from: String, candidate: String, sdpMLineIndex: Int, sdpMid: String?) {
                webRtcClient.onRemoteIce(candidate, sdpMLineIndex, sdpMid)
            }
        })

        btnAccept?.setOnClickListener {
            // we accept incoming offer
            if (incomingOfferFrom != null && incomingOfferSdp != null) {
                webRtcClient.onRemoteOffer(incomingOfferFrom!!, incomingOfferSdp!!)
                btnAccept.visibility = View.GONE
            }
        }

        btnHangup?.setOnClickListener {
            finish()
        }

        // If caller, create offer after small delay to ensure signaling listener is ready
        if (isCaller) {
            // create offer and send
            webRtcClient.createOfferAndSend()
        }
    }

    private var incomingOfferFrom: String? = null
    private var incomingOfferSdp: String? = null

    override fun onDestroy() {
        super.onDestroy()
        webRtcClient.close()
        FirebaseSignaling.clearBetween(localId, remoteId)
        FirebaseSignaling.disconnect()
    }
}
