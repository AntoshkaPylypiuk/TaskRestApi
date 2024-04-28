package ua.lv.pylypiuk.anton.testRestApplication.modelTest;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lv.pylypiuk.anton.testRestApplication.model.User;

import java.time.LocalDate;
import java.util.Set;

public class UserTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void testValidUser() {
        User user = new User(
                "valid@email.com",
                "John",
                "Doe",
                LocalDate.of(1980, 1, 1),
                "123 Main St",
                "+1234567890"
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "No validation errors expected for a valid user");
    }

    @Test
    public void testEmptyEmail() {
        User user = new User("", "John", "Doe", LocalDate.of(1980, 1, 1), "123 Main St", "+1234567890");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "One violation expected for empty email");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Email is required", violation.getMessage());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    public void testInvalidEmailFormat() {
        User user = new User("invalid_email", "John", "Doe", LocalDate.of(1980, 1, 1), "123 Main St", "+1234567890");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "One violation expected for invalid email format");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Invalid email format", violation.getMessage());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    public void testEmptyFirstName() {
        User user = new User("valid@email.com", "", "Doe", LocalDate.of(1980, 1, 1), "123 Main St", "+1234567890");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "One violation expected for empty first name");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("First name is required", violation.getMessage());
        assertEquals("firstName", violation.getPropertyPath().toString());
    }

    @Test
    public void testEmptyLastName() {
        User user = new User("valid@email.com", "John", "", LocalDate.of(1980, 1, 1), "123 Main St", "+1234567890");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "One violation expected for empty last name");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Last name is required", violation.getMessage());
        assertEquals("lastName", violation.getPropertyPath().toString());
    }

    @Test
    public void testNullBirthDate() {
        User user = new User("valid@email.com", "John", "Doe", null, "123 Main St", "+1234567890");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "One violation expected for null birth date");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Birth date is required", violation.getMessage());
        assertEquals("birthDate", violation.getPropertyPath().toString());
    }

    @Test
    public void testFutureBirthDate() {
        User user = new User(
                "valid@email.com",
                "John",
                "Doe",
                LocalDate.now().plusDays(1),
                "123 Main St",
                "+1234567890"
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), "One violation expected for future birth date");

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Birth date must be in the past", violation.getMessage());
        assertEquals("birthDate", violation.getPropertyPath().toString());
    }
}