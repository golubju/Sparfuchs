package com.example.sparfuchs

import CategoryAdapter
import CategoryViewModel
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparfuchs.backend.CategoryEntity

class CategoryOverviewActivity : ComponentActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var categoryEditText: EditText
    private lateinit var keywordEditText: EditText
    private lateinit var updateCategoryButton: Button
    private lateinit var addCategoryButton: Button
    private lateinit var deleteCategoryButton: Button


    private var selectedCategory: CategoryEntity? = null  // Speichert die aktuelle Auswahl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_overview)

        recyclerView = findViewById(R.id.recycler_view_categories)
        categoryEditText = findViewById(R.id.edit_category_name)
        keywordEditText = findViewById(R.id.edit_category_keywords)
        updateCategoryButton = findViewById(R.id.update_category_button)
        addCategoryButton = findViewById(R.id.add_category_button)
        deleteCategoryButton= findViewById(R.id.delete_category_button)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter { category -> selectCategory(category) }
        recyclerView.adapter = adapter

        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)

        categoryViewModel.allCategories.observe(this) { categories ->
            categories?.let { adapter.submitList(it) }
        }
        addCategoryButton.setOnClickListener {
            val categoryName = categoryEditText.text.toString()
            val keywords = keywordEditText.text.toString()

            if (categoryName.isNotBlank()) {
                val newCategory = CategoryEntity(categoryName, keywords)
                categoryViewModel.insert(newCategory)
                clearInputFields()
            } else {
                Toast.makeText(this, "Kategorie kann nicht leer sein!", Toast.LENGTH_SHORT).show()
            }
        }

        // Kategorie aktualisieren
        updateCategoryButton.setOnClickListener {
            val newName = categoryEditText.text.toString()
            val newKeywords = keywordEditText.text.toString()

            if (selectedCategory != null && newName.isNotBlank()) {
                val updatedCategory = CategoryEntity(newName, newKeywords).apply {
                    id = selectedCategory!!.id  // ID beibehalten, sonst wird eine neue Kategorie erstellt
                }
                categoryViewModel.update(updatedCategory)
                clearInputFields()
            } else {
                Toast.makeText(this, "Kategorie auswählen und Name eingeben!", Toast.LENGTH_SHORT).show()
            }
        }
        deleteCategoryButton.setOnClickListener {
            selectedCategory?.let { category ->
                categoryViewModel.delete(category)
                clearInputFields()
            } ?: Toast.makeText(this, "Kategorie auswählen", Toast.LENGTH_SHORT).show()
        }

    }


    private fun selectCategory(category: CategoryEntity) {
        selectedCategory = category
        categoryEditText.setText(category.name)
        keywordEditText.setText(category.keywords)
    }

    private fun clearInputFields() {
        categoryEditText.text.clear()
        keywordEditText.text.clear()
        selectedCategory = null
    }
}
