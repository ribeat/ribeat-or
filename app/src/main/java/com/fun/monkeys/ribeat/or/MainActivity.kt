package com.`fun`.monkeys.ribeat.or

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val fragment = CategoryListFragment().apply {
            arguments = Bundle()
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragment)
            .commit()


    }
}
