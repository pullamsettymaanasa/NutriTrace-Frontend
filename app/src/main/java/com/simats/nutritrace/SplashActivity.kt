package com.simats.nutritrace

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.simats.nutritrace.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request POST_NOTIFICATIONS permission for Android 13+ at launch
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Start notification scheduling right when the app opens
        NotificationScheduler.start(this)

        // Animate the progress bar
        binding.progressTrack.post {
            val totalWidth = binding.progressTrack.width
            val progressAnimator = android.animation.ValueAnimator.ofInt(0, totalWidth)
            progressAnimator.duration = 2500 // 2.5 seconds to fill

            progressAnimator.addUpdateListener { animator ->
                val params = binding.progressView.layoutParams
                params.width = animator.animatedValue as Int
                binding.progressView.layoutParams = params
            }
            progressAnimator.start()
        }

        // Simulate a 3-second system check/loading delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
