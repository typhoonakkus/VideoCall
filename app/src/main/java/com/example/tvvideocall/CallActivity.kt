package com.example.tvvideocall

import android.os.Bundle
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
    private var incomingOfferFrom: String? = null
    private var incomingOfferSdp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        localView = findViewById(R.id.local_video_view)
        remoteView = findViewById(R.id.remote_video_view)
        val btnAccept = findViewById<Button?>(R.id.btn_accept)
        val btnHangup = findViewById<Button?>(R.id.btn_hangup)

        localId = intent.getStringExtra("localId") ?: ("local-" + System.currentTimeMillis())
        remoteId = intent.getStringExtra("remoteId") ?: "peer-123"
        val isCaller = intent.getBooleanExtra("isCaller", false)

        webRtcClient = WebRtcClient(this, localId, remoteId) { remoteTrack ->
            runOnUiThread { remoteTrack?.addSink(remoteView) }
        }
        webRtcClient.start(localView)

        FirebaseSignaling.connect(localId, object: FirebaseSignaling.SignalingCallback {
            override fun onOffer(from: String, sdp: String) {
                incomingOfferFrom = from
                incomingOfferSdp = sdp
                runOnUiThread { btnAccept?.visibility = View.VISIBLE }
            }
            override fun onAnswer(from: String, sdp: String) {
                webRtcClient.onRemoteAnswer(sdp)
            }
            override fun onIceCandidate(from: String, candidate: String, sdpMLineIndex: Int, sdpMid: String?) {
                webRtcClient.onRemoteIce(candidate, sdpMLineIndex, sdpMid)
            }
        })

        btnAccept?.setOnClickListener {
            val from = incomingOfferFrom
            val sdp = incomingOfferSdp
            if (from != null && sdp != null) {
                webRtcClient.onRemoteOffer(from, sdp)
                btnAccept.visibility = View.GONE
            }
        }
        btnHangup?.setOnClickListener { finish() }

        if (isCaller) {
            webRtcClient.createOfferAndSend()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseSignaling.clearBetween(localId, remoteId)
        FirebaseSignaling.disconnect()
        webRtcClient.close()
    }
}
