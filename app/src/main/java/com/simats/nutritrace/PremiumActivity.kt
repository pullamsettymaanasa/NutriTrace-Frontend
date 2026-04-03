package com.simats.nutritrace

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.simats.nutritrace.databinding.ActivityPremiumBinding

class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hasHealthProfile = intent.getBooleanExtra("hasHealthProfile", false)

        binding.btnMaybeLater.setOnClickListener {
            val nextIntent = if (hasHealthProfile) {
                Intent(this, HomeActivity::class.java)
            } else {
                Intent(this, AgeSelectionActivity::class.java)
            }
            nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(nextIntent)
            finish()
        }

        binding.btnStartPremium.setOnClickListener {
            // Show the Play Console mock error message
            binding.layoutMockError.visibility = View.VISIBLE
            
            // Optional: Hide it after 5 seconds to mimic a Toast
            Handler(Looper.getMainLooper()).postDelayed({
                binding.layoutMockError.visibility = View.GONE
            }, 5000)
        }
    }
}
