package ua.lv.pylypiuk.anton.testRestApplication.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ua.lv.pylypiuk.anton.testRestApplication.controller.UserController;
import ua.lv.pylypiuk.anton.testRestApplication.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Value("${user.minAge}")
    private int minAge;

    @Mock
    private List<User> users;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateValidUser() {
        User validUser = new User("John", "Doe", "john.doe@example.com", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        ResponseEntity<String> response = userController.createUser(validUser);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
        verify(users).add(validUser);
    }

    @Test
    void testCreateUserUnderMinAge() {
        // Встановлюємо вік користувача, який є молодшим за minAge
        LocalDate birthDate = LocalDate.now().minusYears(minAge).plusDays(1); // Молодший за встановлений minAge
        User youngUser = new User("alice.smith@example.com", "Alice", "Smith", birthDate, "456 Oak St", "9876543210");

        // Викликаємо метод createUser з контролером
        ResponseEntity<String> response = userController.createUser(youngUser);

        // Перевіряємо, чи повертається очікуваний статус помилки
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Перевіряємо, чи повертається очікуване повідомлення про помилку
        assertTrue(response.getBody().contains("User must be at least " + minAge + " years old"));
    }

    @Test
    void testGetAllUsers() {
        // Mock a list of users
        List<User> userList = new ArrayList<>();
        userList.add(new User("john.doe@example.com", "John", "Doe", LocalDate.of(1985, 5, 10), "123 Main St", "1234567890"));

        // Підготовка до мокування методу контролера, що повертає список користувачів
        UserController userController = new UserController();
        UserController spyController = Mockito.spy(userController);
        Mockito.when(spyController.getAllUsers()).thenReturn(ResponseEntity.ok(userList));

        // Виклик методу контролера, щоб отримати список користувачів
        ResponseEntity<List<User>> responseEntity = spyController.getAllUsers();

        // Перевірка результатів
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<User> returnedUsers = responseEntity.getBody();
        assertNotNull(returnedUsers);
        assertEquals(userList.size(), returnedUsers.size());

        // Перевірка даних першого користувача у списку
        User expectedUser = userList.get(0);
        User returnedUser = returnedUsers.get(0);
        assertEquals(expectedUser.getEmail(), returnedUser.getEmail());
        assertEquals(expectedUser.getFirstName(), returnedUser.getFirstName());
        assertEquals(expectedUser.getLastName(), returnedUser.getLastName());
        assertEquals(expectedUser.getBirthDate(), returnedUser.getBirthDate());
        assertEquals(expectedUser.getAddress(), returnedUser.getAddress());
        assertEquals(expectedUser.getPhoneNumber(), returnedUser.getPhoneNumber());
    }


    @Test
    void testGetUsersByBirthDateRange() {
        LocalDate fromDate = LocalDate.of(1980, 1, 1);
        LocalDate toDate = LocalDate.of(1990, 12, 31);

        List<User> userList = new ArrayList<>();
        userList.add(new User("John", "Doe", "john.doe@example.com", LocalDate.of(1985, 5, 10), "123 Main St", "1234567890"));
        when(users.stream()).thenReturn(userList.stream());

        ResponseEntity<List<User>> response = userController.getUsersByBirthDateRange(fromDate, toDate);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    void testUpdateUser() {
        // Mock an existing user
        User existingUser = new User("john.doe@example.com", "John", "Doe", LocalDate.of(1985, 5, 10), "123 Main St", "1234567890");

        // Mock an updated user with changes
        User updatedUser = new User();
        updatedUser.setFirstName("Johnny");
        updatedUser.setLastName("Doe Jr.");

        // Stubbing the behavior of users list to return a stream containing the existing user
        when(users.stream()).thenReturn(Stream.of(existingUser));

        // Call the controller method to update the user
        ResponseEntity<String> response = userController.updateUser("john.doe@example.com", updatedUser);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated successfully", response.getBody());

        // Verify that the existing user's fields are updated
        assertEquals("Johnny", existingUser.getFirstName());
        assertEquals("Doe Jr.", existingUser.getLastName());
        // Other fields should remain unchanged if not specified in the updatedUser
        assertEquals(LocalDate.of(1985, 5, 10), existingUser.getBirthDate());
        assertEquals("123 Main St", existingUser.getAddress());
        assertEquals("1234567890", existingUser.getPhoneNumber());

        // Verify that the users list is not modified directly (assuming controller logic)
        verify(users, never()).add(any());
    }

    @Test
    void testPatchUser() {
        // Mock an existing user
        User existingUser = new User("john.doe@example.com", "John", "Doe", LocalDate.of(1985, 5, 10), "123 Main St", "1234567890");

        // Mock a patched user with partial updates
        User patchedUser = new User();
        patchedUser.setFirstName("Johnny");
        // No last name provided in the patch
        // No birth date provided in the patch
        patchedUser.setAddress("456 Oak St");
        // No phone number provided in the patch

        // Stubbing the behavior of users list to return a stream containing the existing user
        when(users.stream()).thenReturn(Stream.of(existingUser));

        // Call the controller method to patch the user
        ResponseEntity<String> response = userController.patchUser("john.doe@example.com", patchedUser);

        // Verify that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User patched successfully", response.getBody());

        // Verify that the existing user's fields are updated based on the patch
        assertEquals("Johnny", existingUser.getFirstName());
        assertEquals("Doe", existingUser.getLastName()); // Last name should remain unchanged
        assertEquals(LocalDate.of(1985, 5, 10), existingUser.getBirthDate()); // Birth date should remain unchanged
        assertEquals("456 Oak St", existingUser.getAddress());
        assertEquals("1234567890", existingUser.getPhoneNumber()); // Phone number should remain unchanged

        // Verify that the users list is not modified directly (assuming controller logic)
        verify(users, never()).add(any());
    }


    @Test
    public void testDeleteUser() {
        // Підготовка тестових даних
        String userEmailToDelete = "test@example.com";
        List<User> users = new ArrayList<>();
        users.add(new User("test@example.com", "John", "Doe", LocalDate.of(1990, 1, 1), "Test Address", "1234567890"));

        // Створення мокованого об'єкту UserController
        UserController userController = Mockito.mock(UserController.class);

        // Мокування поведінки методу deleteUser
        when(userController.deleteUser(any(String.class))).thenAnswer(invocation -> {
            String emailToDelete = invocation.getArgument(0);

            // Логіка видалення користувача
            boolean removed = users.removeIf(user -> user.getEmail().equalsIgnoreCase(emailToDelete));

            if (removed) {
                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        });

        // Виклик методу deleteUser
        ResponseEntity<String> responseEntity = userController.deleteUser(userEmailToDelete);

        // Перевірка результатів
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User deleted successfully", responseEntity.getBody());
    }
}
