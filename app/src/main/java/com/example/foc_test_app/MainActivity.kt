package com.example.foc_test_app


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        if (savedInstanceState == null) {
            replace(LinearFormFragment())
        }
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_form -> replace(LinearFormFragment())
                R.id.menu_profile -> replace(ProfileFragment())
                R.id.menu_product -> replace(ProductFragment())
                R.id.menu_calc -> replace(CalculatorFragment())
            }
            true
        }
    }


    private fun replace(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}