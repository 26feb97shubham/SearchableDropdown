package com.searchabledropdown

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.searchabledropdown.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val arrayList = arrayListOf<String>("1", "2", "3", "4", "5")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchableSpinner = SearchableSpinner(this)
        searchableSpinner.windowTitle = "ABC"
        searchableSpinner.isAnimationAllowed(false)
        searchableSpinner.showKeyboardByDefault = false
        searchableSpinner.setSpinnerListItems(arrayList)

        binding.tv.setOnClickListener {
            searchableSpinner.show()
        }
    }
}