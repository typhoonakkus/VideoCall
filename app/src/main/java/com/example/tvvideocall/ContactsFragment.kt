package com.example.tvvideocall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*

class ContactsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_contacts, container, false)
        val callBtn = v.findViewById<Button>(R.id.btn_call)
        val etRemote = v.findViewById<EditText>(R.id.et_remote_id)
        val tvLocal = v.findViewById<TextView>(R.id.tv_local_id)

        val prefs = requireContext().getSharedPreferences("tvcall", Context.MODE_PRIVATE)
        var localId = prefs.getString("local_id", null)
        if (localId == null) {
            localId = UUID.randomUUID().toString().substring(0,8)
            prefs.edit().putString("local_id", localId).apply()
        }
        tvLocal.text = localId

        callBtn.setOnClickListener {
            val remoteId = etRemote.text.toString().ifBlank { "peer-123" }
            val i = Intent(requireContext(), CallActivity::class.java)
            i.putExtra("peerId", remoteId) // legacy key
            i.putExtra("localId", localId)
            i.putExtra("remoteId", remoteId)
            i.putExtra("isCaller", true)
            startActivity(i)
        }
        return v
    }
}
