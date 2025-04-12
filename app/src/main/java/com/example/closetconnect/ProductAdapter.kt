import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.closetconnect.Product
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import com.example.closetconnect.R

class ProductAdapter(private var products: List<Product>,private val onItemClick:(Product)->Unit) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        holder.itemView.setOnClickListener{
            onItemClick(product)
        }
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productRating: RatingBar = itemView.findViewById(R.id.product_rating)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)

        private val executorService = Executors.newSingleThreadExecutor()
        private val handler = Handler(Looper.getMainLooper())

        fun bind(product: Product) {
            // Load image from URL
            loadImage(product.image)
            productName.text = product.title
            productRating.rating = product.rating.rate

            // Format price with local currency
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            productPrice.text = currencyFormatter.format(product.price)
        }

        private fun loadImage(imageUrl: String) {
            // Set a placeholder or default image initially
            // productImage.setImageResource(R.drawable.placeholder_image)

            executorService.execute {
                try {
                    val inputStream = URL(imageUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    handler.post {
                        productImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    handler.post {
                        // Set error image if loading fails
                        // productImage.setImageResource(R.drawable.error_image)
                    }
                }
            }
        }


        // Cleanup to avoid potential memory leaks
        fun cleanup() {
            executorService.shutdown()
        }
    }
}