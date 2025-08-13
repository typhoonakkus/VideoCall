package com.example.tvvideocall

import android.content.Context
import org.webrtc.*

class WebRtcClient(private val context: Context, private val localId: String, private val remoteId: String, private val onRemoteTrack: (VideoTrack?) -> Unit) {
    private val eglBase = EglBase.create()
    private val peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null

    init {
        val options = PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        val pcfOptions = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(pcfOptions)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection(): PeerConnection? {
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        return peerConnectionFactory.createPeerConnection(rtcConfig, object: PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { FirebaseSignaling.sendIceCandidate(localId, remoteId, it.sdp, it.sdpMLineIndex, it.sdpMid) }
            }
            override fun onAddStream(stream: MediaStream?) {}
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                val t = receiver?.track()
                if (t is VideoTrack) onRemoteTrack(t)
            }
        })
    }

    fun start(localView: SurfaceViewRenderer) {
        localView.init(eglBase.eglBaseContext, null)
        val videoCapturer = createCameraCapturer() ?: return
        val videoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer.initialize(SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext), context, videoSource.capturerObserver)
        videoCapturer.startCapture(640, 480, 30)
        localVideoTrack = peerConnectionFactory.createVideoTrack("v0", videoSource)
        localVideoTrack?.addSink(localView)

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("a0", audioSource)

        peerConnection = createPeerConnection()
        val stream = peerConnectionFactory.createLocalMediaStream("localStream")
        localVideoTrack?.let { stream.addTrack(it) }
        stream.addTrack(audioTrack)
        peerConnection?.addStream(stream)
    }

    fun createOfferAndSend() {
        val pc = peerConnection ?: createPeerConnection().also { peerConnection = it }
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        pc?.createOffer(object: SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (desc == null) return
                pc.setLocalDescription(object: SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() { FirebaseSignaling.sendOffer(localId, remoteId, desc.description) }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    fun onRemoteOffer(from: String, sdp: String) {
        val pc = peerConnection ?: createPeerConnection().also { peerConnection = it }
        val desc = SessionDescription(SessionDescription.Type.OFFER, sdp)
        pc.setRemoteDescription(object: SdpObserver {
            override fun onSetSuccess() {
                pc.createAnswer(object: SdpObserver {
                    override fun onCreateSuccess(answer: SessionDescription?) {
                        if (answer == null) return
                        pc.setLocalDescription(object: SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() { FirebaseSignaling.sendAnswer(localId, from, answer.description) }
                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        }, answer)
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, MediaConstraints())
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, desc)
    }

    fun onRemoteAnswer(sdp: String) {
        val pc = peerConnection ?: return
        val desc = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        pc.setRemoteDescription(object: SdpObserver {
            override fun onSetSuccess() {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, desc)
    }

    fun onRemoteIce(candidate: String, sdpMLineIndex: Int, sdpMid: String?) {
        val pc = peerConnection ?: return
        val ice = IceCandidate(sdpMid, sdpMLineIndex, candidate)
        pc.addIceCandidate(ice)
    }

    fun close() {
        try {
            peerConnection?.close()
            peerConnection?.dispose()
        } catch (_: Exception) {}
        peerConnection = null
        localVideoTrack = null
    }

    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        for (name in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        // fallback: first available camera
        val legacy = Camera1Enumerator(false)
        val names = legacy.deviceNames
        if (names.isNotEmpty()) return legacy.createCapturer(names[0], null)
        return null
    }
}
