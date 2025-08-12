package com.example.tvvideocall

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.tvvideocall.ProfileFragment
import com.example.tvvideocall.ContactsFragment

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Simple fragment swap using buttons (D-pad friendly)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProfileFragment())
                .commit()
        }
    }
}
