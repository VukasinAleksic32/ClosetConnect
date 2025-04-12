package com.example.closetconnect

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue


class SingleItem : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_item)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val productJson = intent.getStringExtra("PRODUCT_DATA")
        val product = Gson().fromJson(productJson, Product::class.java)
        val productImage: ImageView = findViewById(R.id.image)
        val productName: TextView = findViewById(R.id.title)
        val productCategory: TextView = findViewById(R.id.category)
        val productRating: RatingBar = findViewById(R.id.rating_bar)
        val productPrice: TextView = findViewById(R.id.price)
        val productDescription: TextView = findViewById(R.id.description)
        val productCount: TextView = findViewById(R.id.rating_count)
        productName.text = product.title
        productRating.rating = product.rating.rate
        productCount.text = product.rating.count.toString()
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        productPrice.text = currencyFormatter.format(product.price)
        productDescription.text = product.description
        productCategory.text = product.category
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(product.image)
                val bitmap = BitmapFactory.decodeStream(url.openStream())
                withContext(Dispatchers.Main) {
                    productImage.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle error, e.g., set a default image
                    productImage.setImageResource(R.drawable.outline_shopping_cart_24)
                }
            }
        }
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            this.onBackPressed()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.itemnavigation)
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
        val cartButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        cartButton.setOnClickListener {
            addToCart(product)
        }
    }

    private fun addToCart(product: Product) {
        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPage::class.java))
            return
        }

        // Reference to the cart collection
        val cartRef = firestore.collection("cart")

        // Query to check if the product already exists in the user's cart
        cartRef.whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("productId", product.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // No existing cart item - create new document
                    val cartItem = hashMapOf(
                        "userId" to currentUser.uid,
                        "productId" to product.id,
                        "title" to product.title,
                        "price" to product.price,
                        "image" to product.image,
                        "quantity" to 1
                    )

                    cartRef.add(cartItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to add item to cart: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Existing cart item found - update quantity
                    val documentId = querySnapshot.documents[0].id

                    cartRef.document(documentId)
                        .update("quantity", FieldValue.increment(1))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Quantity updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to update cart: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Cart check failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}