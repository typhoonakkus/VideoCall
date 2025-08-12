package com.example.tvvideocall

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        val tvName = v.findViewById<TextView>(R.id.tv_name)
        val btnReg = v.findViewById<Button?>(R.id.btn_regen)
        val prefs = requireContext().getSharedPreferences("tvcall", Context.MODE_PRIVATE)
        var localId = prefs.getString("local_id", null)
        if (localId == null) {
            localId = UUID.randomUUID().toString().substring(0,8)
            prefs.edit().putString("local_id", localId).apply()
        }
        tvName.text = "Your ID: $localId"
        btnReg?.setOnClickListener {
            val newId = UUID.randomUUID().toString().substring(0,8)
            prefs.edit().putString("local_id", newId).apply()
            tvName.text = "Your ID: $newId"
        }
        return v
    }
}
