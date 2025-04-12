package com.example.closetconnect

import ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SingleCategory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_category)
        val categoryName = intent.getStringExtra("categoryName")
        val productAdapter = ProductAdapter(listOf()) { product ->
            val intent = Intent(this, SingleItem::class.java)
            intent.putExtra("PRODUCT_DATA", Gson().toJson(product)) // Pass product data as JSON
            startActivity(intent)
        }
        // Set up RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = productAdapter
        val retrofit = Retrofit.Builder().baseUrl("https://fakestoreapi.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val apiService = retrofit.create(ApiService::class.java)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            this.onBackPressed()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = apiService.getInCategory(categoryName.toString())
                withContext(Dispatchers.Main) {
                    // Update the RecyclerView with the fetched products
                    productAdapter.updateProducts(products)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.categorynavigation)
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