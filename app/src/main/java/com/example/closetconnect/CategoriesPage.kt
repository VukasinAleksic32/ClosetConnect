package com.example.closetconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class CategoriesPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories_page)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.categoriesnavigation)
        val mens = findViewById<CardView>(R.id.mensclothingcard)
        val womens = findViewById<CardView>(R.id.womensclothingcard)
        val jewelry = findViewById<CardView>(R.id.jewelrycard)
        val electronics = findViewById<CardView>(R.id.electronicscard)
        mens.setOnClickListener {
            val intent = Intent(this, SingleCategory::class.java)
            intent.putExtra("categoryName","men's clothing")
            this.startActivity(intent)
        }
        womens.setOnClickListener {
            val intent = Intent(this, SingleCategory::class.java)
            intent.putExtra("categoryName","women's clothing")
            this.startActivity(intent)
        }
        jewelry.setOnClickListener {
            val intent = Intent(this, SingleCategory::class.java)
            intent.putExtra("categoryName","jewelery")
            this.startActivity(intent)
        }
        electronics.setOnClickListener {
            val intent = Intent(this, SingleCategory::class.java)
            intent.putExtra("categoryName","electronics")
            this.startActivity(intent)
        }

        // Set up the navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Start HomeActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.categories -> {
                    // Start SearchActivity
                    startActivity(Intent(this, CategoriesPage::class.java))
                    true
                }

                R.id.cart -> {
                    // Start ProfileActivity
                    startActivity(Intent(this, CartPage::class.java))
                    true
                }

                else -> false
            }
        }
    }

}