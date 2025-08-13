package com.example.tvvideocall

import com.google.firebase.database.*

object FirebaseSignaling {
    private var db: DatabaseReference? = null
    private var listener: ChildEventListener? = null

    interface SignalingCallback {
        fun onOffer(from: String, sdp: String)
        fun onAnswer(from: String, sdp: String)
        fun onIceCandidate(from: String, candidate: String, sdpMLineIndex: Int, sdpMid: String?)
    }

    fun connect(localId: String, callback: SignalingCallback) {
        db = FirebaseDatabase.getInstance().getReference("signaling")
        listener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val map = snapshot.value as? Map<*, *> ?: return
                val type = map["type"] as? String ?: return
                val from = map["from"] as? String ?: return
                val to = map["to"] as? String ?: return
                if (to != localId && to != "broadcast") return
                when(type) {
                    "offer" -> callback.onOffer(from, map["sdp"] as String)
                    "answer" -> callback.onAnswer(from, map["sdp"] as String)
                    "ice" -> {
                        val candidate = map["candidate"] as? String ?: return
                        val sdpMLineIndex = (map["sdpMLineIndex"] as? Long)?.toInt() ?: 0
                        val sdpMid = map["sdpMid"] as? String
                        callback.onIceCandidate(from, candidate, sdpMLineIndex, sdpMid)
                    }
                }
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onCancelled(p0: DatabaseError) {}
        }
        db?.addChildEventListener(listener!!)
    }

    fun sendOffer(from: String, to: String, sdp: String) {
        db?.push()?.setValue(mapOf("type" to "offer","from" to from,"to" to to,"sdp" to sdp))
    }
    fun sendAnswer(from: String, to: String, sdp: String) {
        db?.push()?.setValue(mapOf("type" to "answer","from" to from,"to" to to,"sdp" to sdp))
    }
    fun sendIceCandidate(from: String, to: String, candidate: String, sdpMLineIndex: Int, sdpMid: String?) {
        db?.push()?.setValue(mapOf("type" to "ice","from" to from, "to" to to, "candidate" to candidate, "sdpMLineIndex" to sdpMLineIndex, "sdpMid" to sdpMid))
    }

    fun disconnect() {
        listener?.let { db?.removeEventListener(it) }
        listener = null
        db = null
    }

    fun clearBetween(a: String, b: String) {
        db?.orderByChild("from")?.equalTo(a)?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (c in snapshot.children) {
                    val map = c.value as? Map<*, *> ?: continue
                    val to = map["to"] as? String ?: continue
                    if (to == b || to == "broadcast") {
                        c.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        db?.orderByChild("from")?.equalTo(b)?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (c in snapshot.children) {
                    val map = c.value as? Map<*, *> ?: continue
                    val to = map["to"] as? String ?: continue
                    if (to == a || to == "broadcast") {
                        c.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
