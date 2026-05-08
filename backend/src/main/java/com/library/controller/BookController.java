package com.library.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.PublisherRepository;

import com.library.DTO.BookRequestDto;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    public BookController(BookRepository bookRepository,
                          AuthorRepository authorRepository,
                          PublisherRepository publisherRepository,
                          CategoryRepository categoryRepository,
                          BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    // ✅ Get all books
    @GetMapping
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    @GetMapping("/search")
    public Page<Book> searchBooks(Pageable pageable, @RequestParam("q") String keyword) {
        return bookRepository.searchBooks(pageable, keyword);
    }
    
    // ✅ Get single book (with borrow info)
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BorrowRecord borrowed = borrowRecordRepository.findFirstByBookIdOrderByBorrowDateDesc(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", book.getId());
        response.put("title", book.getTitle());
        response.put("description", book.getDescription());
        response.put("image", book.getImage());
        response.put("shelf", book.getShelf());
        response.put("author", book.getAuthor());
        response.put("publisher", book.getPublisher());
        response.put("category", book.getCategory());
        response.put("status", borrowed != null ? "BORROWED" : "AVAILABLE");
        response.put("borrowedBy", borrowed != null ? borrowed.getMember().getName() : null);

        return ResponseEntity.ok(response);
    }

    // ✅ Add book (with optional image)
    @PostMapping(consumes = {"multipart/form-data"})
    public String addBook(@RequestParam String title,
                          @RequestParam(required = false) Integer authorId,
                          @RequestParam(required = false) Integer publisherId,
                          @RequestParam(required = false) Integer categoryId,
                          @RequestParam(required = false) String shelf,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) MultipartFile image) {
        Book book = new Book();
        book.setTitle(title);
        book.setShelf(shelf);
        book.setDescription(description);

        authorId = setAuthorPublisherCategory(book, authorId, publisherId, categoryId);
        handleImageUpload(book, image);

        bookRepository.save(book);
        return "Book added successfully!";
    }

    @PostMapping("/listAdd")
    public List<Book> addBooks(@RequestBody List<BookRequestDto> bookRequests) {
        List<Book> books = bookRequests.stream().map(req -> {
            Book book = new Book();
            book.setTitle(req.getTitle());
            book.setShelf(req.getShelf());
            book.setDescription(req.getDescription());

            setAuthorPublisherCategory(book, req.getAuthorId(), req.getPublisherId(), req.getCategoryId());

            return book;
        }).toList();

        return bookRepository.saveAll(books);
    }

    // ✅ Update book
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public String updateBook(@PathVariable int id,
                             @RequestParam String title,
                             @RequestParam(required = false) Integer authorId,
                             @RequestParam(required = false) Integer publisherId,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) String shelf,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) MultipartFile image) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return "Book not found!";

        book.setTitle(title);
        book.setShelf(shelf);
        book.setDescription(description);

        setAuthorPublisherCategory(book, authorId, publisherId, categoryId);
        handleImageUpload(book, image);

        bookRepository.save(book);
        return "Book updated successfully!";
    }

    // ✅ Delete book
    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable int id) {
        if (!bookRepository.existsById(id)) return "Book not found!";
        bookRepository.deleteById(id);
        return "Book deleted successfully!";
    }



    // 🔧 Helper: assign related entities
    private Integer setAuthorPublisherCategory(Book book, Integer authorId, Integer publisherId, Integer categoryId) {
        if (authorId != null) authorRepository.findById(authorId).ifPresent(book::setAuthor);
        if (publisherId != null) publisherRepository.findById(publisherId).ifPresent(book::setPublisher);
        if (categoryId != null) categoryRepository.findById(categoryId).ifPresent(book::setCategory);
        return authorId;
    }

    // 🔧 Helper: handle image upload
    private void handleImageUpload(Book book, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, image.getBytes());
                book.setImage("/uploads/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image!", e);
            }
        }
    }
}
