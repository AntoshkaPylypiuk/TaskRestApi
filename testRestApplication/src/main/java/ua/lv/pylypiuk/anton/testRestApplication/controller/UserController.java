package ua.lv.pylypiuk.anton.testRestApplication.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lv.pylypiuk.anton.testRestApplication.exception.UserValidationException;
import ua.lv.pylypiuk.anton.testRestApplication.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private List<User> users = new ArrayList<>();

    @Value("${user.minAge}")
    private int minAge;

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        if (user.getBirthDate().isAfter(LocalDate.now().minusYears(minAge))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User must be at least " + minAge + " years old");
        }

        users.add(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> getUsersByBirthDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }

        List<User> filteredUsers = users.stream()
                .filter(user -> user.getBirthDate().isAfter(from) && user.getBirthDate().isBefore(to))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredUsers);
    }

    @PutMapping("/{email}")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody User updatedUser) {
        User existingUser = users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

        return ResponseEntity.ok("User updated successfully");
    }

    @PatchMapping("/{email}")
    public ResponseEntity<String> patchUser(@PathVariable String email, @RequestBody User patchedUser) {
        User existingUser = users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Apply patch if fields are not null in the request
        if (patchedUser.getFirstName() != null) {
            existingUser.setFirstName(patchedUser.getFirstName());
        }
        if (patchedUser.getLastName() != null) {
            existingUser.setLastName(patchedUser.getLastName());
        }
        if (patchedUser.getBirthDate() != null) {
            existingUser.setBirthDate(patchedUser.getBirthDate());
        }
        if (patchedUser.getAddress() != null) {
            existingUser.setAddress(patchedUser.getAddress());
        }
        if (patchedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(patchedUser.getPhoneNumber());
        }

        return ResponseEntity.ok("User patched successfully");
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        boolean removed = users.removeIf(user -> user.getEmail().equalsIgnoreCase(email));
        if (removed) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<String> handleUserValidationException(UserValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

