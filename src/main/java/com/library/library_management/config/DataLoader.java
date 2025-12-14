package com.library.library_management.config;

import com.library.library_management.entity.Book;
import com.library.library_management.entity.User;
import com.library.library_management.entity.enums.Genre;
import com.library.library_management.entity.enums.ReadingStatus;
import com.library.library_management.entity.enums.Role;
import com.library.library_management.repository.BookRepository;
import com.library.library_management.repository.UserRepository;
import com.library.library_management.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Cleaning up database for fresh test...");
        verificationTokenRepository.deleteAll();
        bookRepository.deleteAll(); // Delete books first (Foreign Key)
        userRepository.deleteAll(); // Then delete users

        log.info("Loading initial test data...");
        if (userRepository.count() > 0) {
            log.info("Database already has data, skipping initialization");
            return;
        }

        log.info("Loading initial test data...");

        // Create admin user (password: admin123)
        User admin = User.builder()
                .email("admin@library.com")
                .password(passwordEncoder.encode("admin123"))
                .name("Admin User")
                .role(Role.ADMIN)
                .emailVerified(true)
                .build();
        admin = userRepository.save(admin);
        log.info("Created admin user: {} (password: admin123)", admin.getEmail());

        // Create regular users (password: password123)
        User alice = User.builder()
                .email("alice@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("Alice Johnson")
                .role(Role.USER)
                .emailVerified(true)
                .build();
        alice = userRepository.save(alice);
        log.info("Created user: {} (password: password123)", alice.getEmail());

        User bob = User.builder()
                .email("bob@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("Bob Smith")
                .emailVerified(true)
                .role(Role.USER)
                .build();
        bob = userRepository.save(bob);
        log.info("Created user: {} (password: password123)", bob.getEmail());

        // Create books for Alice (5 books)
        createBook(alice, "The Name of the Wind", "Patrick Rothfuss", Genre.FANTASY,
                ReadingStatus.COMPLETED, new BigDecimal("15.99"), 662, 2007);
        createBook(alice, "Dune", "Frank Herbert", Genre.SCIENCE_FICTION,
                ReadingStatus.READING, new BigDecimal("12.99"), 412, 1965);
        createBook(alice, "The Hobbit", "J.R.R. Tolkien", Genre.FANTASY,
                ReadingStatus.COMPLETED, new BigDecimal("14.99"), 310, 1937);
        createBook(alice, "1984", "George Orwell", Genre.FICTION,
                ReadingStatus.TO_READ, new BigDecimal("11.99"), 328, 1949);
        createBook(alice, "Clean Code", "Robert C. Martin", Genre.TECHNOLOGY,
                ReadingStatus.READING, new BigDecimal("35.99"), 464, 2008);

        // Create books for Bob (4 books)
        createBook(bob, "The Great Gatsby", "F. Scott Fitzgerald", Genre.FICTION,
                ReadingStatus.COMPLETED, new BigDecimal("10.99"), 180, 1925);
        createBook(bob, "Sapiens", "Yuval Noah Harari", Genre.HISTORY,
                ReadingStatus.COMPLETED, new BigDecimal("18.99"), 443, 2011);
        createBook(bob, "The Hobbit", "J.R.R. Tolkien", Genre.FANTASY,
                ReadingStatus.TO_READ, new BigDecimal("14.99"), 310, 1937);
        createBook(bob, "Atomic Habits", "James Clear", Genre.SELF_HELP,
                ReadingStatus.READING, new BigDecimal("16.99"), 320, 2018);

        // Create books for Admin (2 books)
        createBook(admin, "The Pragmatic Programmer", "David Thomas", Genre.TECHNOLOGY,
                ReadingStatus.COMPLETED, new BigDecimal("49.99"), 352, 2019);
        createBook(admin, "Design Patterns", "Gang of Four", Genre.TECHNOLOGY,
                ReadingStatus.READING, new BigDecimal("54.99"), 416, 1994);

        log.info("===========================================");
        log.info("Test data loaded successfully!");
        log.info("Users created: {}", userRepository.count());
        log.info("Books created: {}", bookRepository.count());
        log.info("===========================================");
        log.info("Test Credentials:");
        log.info("  Admin: admin@library.com / admin123");
        log.info("  Alice: alice@example.com / password123");
        log.info("  Bob:   bob@example.com / password123");
        log.info("===========================================");
    }

    private void createBook(User user, String title, String author, Genre genre,
                            ReadingStatus status, BigDecimal price, Integer pageCount,
                            Integer publicationYear) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .status(status)
                .price(price)
                .pageCount(pageCount)
                .publicationYear(publicationYear)
                .user(user)
                .build();
        bookRepository.save(book);
    }
}
