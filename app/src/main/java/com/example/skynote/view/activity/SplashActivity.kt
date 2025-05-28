package com.example.skynote.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.skynote.databinding.ActivitySplashBinding
import com.example.skynote.view.activity.HomeActivity

class SplashActivity : AppCompatActivity()
{
    private lateinit var binding: ActivitySplashBinding
    private var isAnimationComplete = false
    private val splashDuration = 5000L // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lottieAnimationView: LottieAnimationView = binding.lottieAnimation

        lottieAnimationView.addAnimatorListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator)
            {
                isAnimationComplete = true
                tryNavigate()
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })

        // Ensure minimum display time
        Handler(Looper.getMainLooper()).postDelayed({
            tryNavigate()
        }, splashDuration)
    }

    private fun tryNavigate() {
        if (isAnimationComplete) {
            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}