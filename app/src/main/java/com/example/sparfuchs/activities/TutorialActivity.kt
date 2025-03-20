package com.example.sparfuchs.activities

import android.R
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.sparfuchs.R.layout.activity_tutorial)
        val buttonFinish: Button = findViewById(com.example.sparfuchs.R.id.button_alles_klar);

        buttonFinish.setOnClickListener {
            finish() // Closes tutorial
        }
    }
}
