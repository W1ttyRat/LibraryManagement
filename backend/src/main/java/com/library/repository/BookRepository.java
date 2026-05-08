package com.library.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.model.Book;

public interface BookRepository extends JpaRepository<Book,Integer>{
    @Query("SELECT b FROM Book b " +
           "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(b.publisher.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(b.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(Pageable pageable, @Param("keyword") String keyword);
}