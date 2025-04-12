package com.example.closetconnect

import ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.mainnavigation)

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
        // Fetch products asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = apiService.getProducts()
                withContext(Dispatchers.Main) {
                    // Update the RecyclerView with the fetched products
                    productAdapter.updateProducts(products)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val logout = findViewById<ImageButton>(R.id.logout_button)
        logout.setOnClickListener{
            val intent = Intent(this,LoginPage::class.java)
            FirebaseAuth.getInstance().signOut()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

}