package com.example.sparfuchs.backend;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(TransactionEntity transaction);

    @Insert
    void insertAll(List<TransactionEntity> transactions);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("DELETE FROM transactions")
    void deleteAll();

    @Update
    void update(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE category = :category")
    List<TransactionEntity> getTransactionsByCategory(String category);

}

