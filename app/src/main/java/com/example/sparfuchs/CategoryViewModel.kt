import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.sparfuchs.backend.AppDatabase
import com.example.sparfuchs.backend.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDao = AppDatabase.getInstance(application).categoryDao()
    val allCategories: LiveData<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun insert(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.delete(category)
        }
    }
    fun update(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.update(category)
        }
    }

}
