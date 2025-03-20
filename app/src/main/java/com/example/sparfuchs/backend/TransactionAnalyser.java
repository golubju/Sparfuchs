package com.example.sparfuchs.backend;

import java.util.List;

public class TransactionAnalyser {
    List<Category> categories;

    public TransactionAnalyser(List<Category> pCategories) {
        this.categories=pCategories;
    }
    public Category getCategory(TransactionEntity transactionEntity){
        String text= transactionEntity.getDescription().toUpperCase();
        for (Category category: categories){
            for(String words: category.getDescription()){
                if(text.contains(words.toUpperCase())){
                    return category;
                }
            }
        }
            return new Category("nicht zugeordnete Transaktionen",null);
    }
}
