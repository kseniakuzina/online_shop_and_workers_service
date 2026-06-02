package com.study.online_shop.service;

import com.study.online_shop.DTO.UserDTO;
import com.study.online_shop.entities.Role;
import com.study.online_shop.entities.User;
import com.study.online_shop.repository.RoleRepository;
import com.study.online_shop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_USER");

        testUserDTO = new UserDTO("newuser", "password", "New", "User", "new@example.com", "0987654321");
    }

    @Test
    void loadUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        UserDetails result = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("unknown");
        });
    }

    @Test
    void findUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findUserById_ShouldReturnEmptyUser_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        User result = userService.findUserById(99L);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
    }

    @Test
    void allUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> expectedUsers = Collections.singletonList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.allUsers();

        // Assert
        assertEquals(expectedUsers, result);
        assertEquals(1, result.size());
    }

    @Test
    void allUsersWithRoles_ShouldReturnPairsOfUsernameAndRole() {
        // Arrange
        Object[] row = new Object[]{"testuser", testRole};
        List<Object[]> results = Collections.singletonList(row);
        when(userRepository.findAllUsersWithRoles()).thenReturn(results);

        // Act
        List<Pair<String, String>> result = userService.allUsersWithRoles();

        // Assert
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getFirst());
        assertEquals("ROLE_USER", result.get(0).getSecond());
    }

    @Test
    void allRoles_ShouldReturnAllRoles() {
        // Arrange
        List<Role> expectedRoles = Collections.singletonList(testRole);
        when(roleRepository.findAll()).thenReturn(expectedRoles);

        // Act
        List<Role> result = userService.allRoles();

        // Assert
        assertEquals(expectedRoles, result);
        assertEquals(1, result.size());
    }

    @Test
    void saveUser_ShouldReturnTrue_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = userService.saveUser(testUserDTO);

        // Assert
        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void saveUser_ShouldReturnFalse_WhenUserAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(testUser);

        // Act
        boolean result = userService.saveUser(testUserDTO);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveRole_ShouldReturnTrue_WhenRoleDoesNotExist() {
        // Arrange
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(null);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        boolean result = userService.saveRole("ROLE_ADMIN");

        // Assert
        assertTrue(result);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void saveRole_ShouldReturnFalse_WhenRoleAlreadyExists() {
        // Arrange
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);

        // Act
        boolean result = userService.saveRole("ROLE_USER");

        // Assert
        assertFalse(result);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void grantRole_ShouldReturnTrue_WhenUserAndRoleExist() {
        // Arrange
        String username = "testuser";
        String roleName = "ROLE_ADMIN";

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> userQuery = mock(CriteriaQuery.class);
        Root<User> userRoot = mock(Root.class);
        Predicate userPredicate = mock(Predicate.class);
        TypedQuery<User> typedUserQuery = mock(TypedQuery.class);

        CriteriaQuery<Role> roleQuery = mock(CriteriaQuery.class);
        Root<Role> roleRoot = mock(Root.class);
        Predicate rolePredicate = mock(Predicate.class);
        TypedQuery<Role> typedRoleQuery = mock(TypedQuery.class);

        Query nativeQuery = mock(Query.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);

        // User query setup
        when(cb.createQuery(User.class)).thenReturn(userQuery);
        when(userQuery.from(User.class)).thenReturn(userRoot);
        when(cb.equal(userRoot.get("username"), username)).thenReturn(userPredicate);
        when(userQuery.where(userPredicate)).thenReturn(userQuery);
        when(em.createQuery(userQuery)).thenReturn(typedUserQuery);
        when(typedUserQuery.getSingleResult()).thenReturn(testUser);

        // Role query setup
        when(cb.createQuery(Role.class)).thenReturn(roleQuery);
        when(roleQuery.from(Role.class)).thenReturn(roleRoot);
        when(cb.equal(roleRoot.get("name"), roleName)).thenReturn(rolePredicate);
        when(roleQuery.where(rolePredicate)).thenReturn(roleQuery);
        when(em.createQuery(roleQuery)).thenReturn(typedRoleQuery);
        when(typedRoleQuery.getSingleResult()).thenReturn(testRole);

        // Native query setup
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(eq("userId"), anyLong())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(eq("roleId"), anyLong())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userService.grantRole(username, roleName);

        // Assert
        assertTrue(result);
        verify(nativeQuery).executeUpdate();
    }

    @Test
    void grantRole_ShouldReturnFalse_WhenUserNotFound() {
        // Arrange
        String username = "unknown";
        String roleName = "ROLE_ADMIN";

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> userQuery = mock(CriteriaQuery.class);
        Root<User> userRoot = mock(Root.class);
        Predicate userPredicate = mock(Predicate.class);
        TypedQuery<User> typedUserQuery = mock(TypedQuery.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(User.class)).thenReturn(userQuery);
        when(userQuery.from(User.class)).thenReturn(userRoot);
        when(cb.equal(userRoot.get("username"), username)).thenReturn(userPredicate);
        when(userQuery.where(userPredicate)).thenReturn(userQuery);
        when(em.createQuery(userQuery)).thenReturn(typedUserQuery);
        when(typedUserQuery.getSingleResult()).thenThrow(new NoResultException());

        // Act
        boolean result = userService.grantRole(username, roleName);

        // Assert
        assertFalse(result);
        verify(em, never()).createNativeQuery(anyString());
    }

    @Test
    void deleteUser_ShouldReturnTrue_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertTrue(result);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldReturnFalse_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.deleteUser(99L);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void usergtList_ShouldReturnUsersWithIdGreaterThan() {
        // Arrange
        List<User> expectedUsers = Collections.singletonList(testUser);
        TypedQuery<User> typedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("paramId"), anyLong())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.usergtList(0L);

        // Assert
        assertEquals(expectedUsers, result);
    }

    @Test
    void getCurrentUsername_ShouldReturnUsername_WhenAuthenticated() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");

        // Act
        String result = userService.getCurrentUsername();

        // Assert
        assertEquals("testuser", result);

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_ShouldReturnNull_WhenNotAuthenticated() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        String result = userService.getCurrentUsername();

        // Assert
        assertNull(result);

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ShouldReturnUser_WhenAuthenticated() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> userQuery = mock(CriteriaQuery.class);
        Root<User> userRoot = mock(Root.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<User> typedQuery = mock(TypedQuery.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(User.class)).thenReturn(userQuery);
        when(userQuery.from(User.class)).thenReturn(userRoot);
        when(cb.equal(userRoot.get("username"), "testuser")).thenReturn(predicate);
        when(userQuery.where(predicate)).thenReturn(userQuery);
        when(em.createQuery(userQuery)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(testUser);

        // Act
        User result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUser_ShouldUpdateAndSaveUser() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<User> userQuery = mock(CriteriaQuery.class);
        Root<User> userRoot = mock(Root.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<User> typedQuery = mock(TypedQuery.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(User.class)).thenReturn(userQuery);
        when(userQuery.from(User.class)).thenReturn(userRoot);
        when(cb.equal(userRoot.get("username"), "testuser")).thenReturn(predicate);
        when(userQuery.where(predicate)).thenReturn(userQuery);
        when(em.createQuery(userQuery)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser("Updated", "Name", "updateduser", "updated@example.com", "5555555555");

        // Assert
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("5555555555", result.getPhone());
        verify(userRepository).save(testUser);

        // Cleanup
        SecurityContextHolder.clearContext();
    }
}