package com.example.closetconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat
import java.util.Locale

class CartPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var checkoutButton: Button
    val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_page)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        totalPriceTextView = findViewById(R.id.totalPriceTextView)
        checkoutButton = findViewById(R.id.checkoutButton)

        // Set up RecyclerView
        cartAdapter = CartAdapter(cartItems,
            onQuantityChanged = { position, newQuantity ->
                updateItemQuantity(position, newQuantity)
            },
            onDeleteItem = { position ->
                deleteCartItem(position)
            }
        )
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter

        // Load cart items
        loadCartItems()

        // Checkout button
        checkoutButton.setOnClickListener {
            checkout()
        }

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.cartNavigation)

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

    private fun loadCartItems() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("cart")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                cartItems.clear()
                for (document in querySnapshot.documents) {
                    val cartItem = document.toObject(CartItem::class.java)?.copy(
                        documentId = document.id
                    )
                    cartItem?.let { cartItems.add(it) }
                }
                cartAdapter.notifyDataSetChanged()
                calculateTotalPrice()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateItemQuantity(position: Int, newQuantity: Int) {
        val item = cartItems[position]

        firestore.collection("cart")
            .document(item.documentId)
            .update("quantity", newQuantity)
            .addOnSuccessListener {
                item.quantity = newQuantity
                cartAdapter.notifyItemChanged(position)
                calculateTotalPrice()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCartItem(position: Int) {
        val item = cartItems[position]

        firestore.collection("cart")
            .document(item.documentId)
            .delete()
            .addOnSuccessListener {
                cartItems.removeAt(position)
                cartAdapter.notifyItemRemoved(position)
                calculateTotalPrice()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkout() {
        val currentUser = auth.currentUser ?: return

        // Get a reference to the cart collection
        val cartRef = firestore.collection("cart")

        // Query to find all cart items for the current user
        cartRef.whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Create a batch write to delete multiple documents efficiently
                val batch = firestore.batch()

                // Add each document to the batch delete
                querySnapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                // Commit the batch
                batch.commit()
                    .addOnSuccessListener {
                        // Clear local cart items
                        cartItems.clear()
                        cartAdapter.notifyDataSetChanged()

                        // Update total price
                        calculateTotalPrice()

                        // Show success message
                        Toast.makeText(
                            this,
                            "Checkout successful! Cart has been cleared.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Optional: Navigate to order confirmation page
                        // startActivity(Intent(this, OrderConfirmationActivity::class.java))
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Checkout failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to process checkout: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun calculateTotalPrice(){
        val total = cartItems.sumByDouble { it.price * it.quantity }
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        totalPriceTextView.text = currencyFormatter.format(total)
    }
}

// Data class for Cart Item
data class CartItem(
    val userId: String = "",
    val productId: Long = 0,
    val title: String = "",
    val price: Double = 0.0,
    val image: String = "",
    var quantity: Int = 1,
    var documentId: String = ""
)