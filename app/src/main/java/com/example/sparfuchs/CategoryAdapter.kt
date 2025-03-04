import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.example.sparfuchs.CategoryDiffCallback
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.CategoryEntity

class CategoryAdapter(private val onItemClick: (CategoryEntity) -> Unit) :
    ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_cv, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)

        // 🔥 Klick auf eine Kategorie -> Daten zum Bearbeiten setzen
        holder.itemView.setOnClickListener {
            onItemClick(category)
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.text_category_name)

        fun bind(category: CategoryEntity) {
            categoryName.text = "${category.name} (${category.keywords})"
        }
    }
}
