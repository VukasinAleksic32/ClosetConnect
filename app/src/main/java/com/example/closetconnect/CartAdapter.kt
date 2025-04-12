package com.example.closetconnect

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onQuantityChanged: (Int, Int) -> Unit,
    private val onDeleteItem: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.cartItemImage)
        val titleTextView: TextView = itemView.findViewById(R.id.cartItemTitle)
        val priceTextView: TextView = itemView.findViewById(R.id.cartItemPrice)
        val quantityTextView: TextView = itemView.findViewById(R.id.cartItemQuantity)
        val decreaseButton: ImageButton = itemView.findViewById(R.id.decreaseQuantityButton)
        val increaseButton: ImageButton = itemView.findViewById(R.id.increaseQuantityButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteItemButton)
        val itemTotalPriceTextView: TextView = itemView.findViewById(R.id.cartItemTotalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_layout, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        // Load image using coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(item.image)
                val bitmap = BitmapFactory.decodeStream(url.openStream())
                withContext(Dispatchers.Main) {
                    holder.imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Set a default image if loading fails
                    holder.imageView.setImageResource(R.drawable.outline_shopping_cart_24)
                }
            }
        }

        // Set text views
        holder.titleTextView.text = item.title
        holder.priceTextView.text = currencyFormatter.format(item.price)
        holder.quantityTextView.text = item.quantity.toString()

        // Calculate and set item total price
        val itemTotalPrice = item.price * item.quantity
        holder.itemTotalPriceTextView.text = currencyFormatter.format(itemTotalPrice)

        // Quantity adjustment buttons
        holder.decreaseButton.setOnClickListener {
            if (item.quantity > 1) {
                onQuantityChanged(position, item.quantity - 1)
            }
        }

        holder.increaseButton.setOnClickListener {
            onQuantityChanged(position, item.quantity + 1)
        }

        // Delete button
        holder.deleteButton.setOnClickListener {
            onDeleteItem(position)
        }
    }

    override fun getItemCount() = cartItems.size
}