package com.study.workers.service;

import com.study.workers.DTO.UserDTO;
import com.study.workers.entities.BusynessType;
import com.study.workers.entities.GrantRoleStatus;
import com.study.workers.entities.Role;
import com.study.workers.entities.User;
import com.study.workers.repository.RoleRepository;
import com.study.workers.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role("ROLE_USER");
        testRole.setId(1L);

        testUser = new User("John", "Doe", "johndoe", "encodedPassword", "john@example.com", "1234567890");
        testUser.setId(1L);
        testUser.setRoles(new HashSet<>(Collections.singleton(testRole)));
        testUser.setBusyness(BusynessType.FREE);

        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(testUser);
        assertEquals("johndoe", userService.loadUserByUsername("johndoe").getUsername());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    void findUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        assertEquals(1L, userService.findUserById(1L).getId());
    }

    @Test
    void findUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(userService.findUserById(99L).getId());
    }

    @Test
    void allUsers() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        assertEquals(1, userService.allUsers().size());
    }

    @Test
    void allUsersWithRoles() {
        Object[] row = {"johndoe", testRole};
        when(userRepository.findAllUsersWithRoles()).thenReturn(Collections.singletonList(row));
        List<Pair<String, String>> result = userService.allUsersWithRoles();
        assertEquals(1, result.size());
    }

    @Test
    void allRoles() {
        when(roleRepository.findAll()).thenReturn(Collections.singletonList(testRole));
        assertEquals(1, userService.allRoles().size());
    }

    @Test
    void saveUser_Success() {
        UserDTO dto = new UserDTO("newuser", "pass", "First", "Last", "email@example.com", "12345");

        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        assertTrue(userService.saveUser(dto));
    }

    @Test
    void saveUser_Exists() {
        UserDTO dto = new UserDTO("johndoe", "password", "John", "Doe", "john@example.com", "1234567890");
        when(userRepository.findByUsername("johndoe")).thenReturn(testUser);
        assertFalse(userService.saveUser(dto));
    }

    @Test
    void saveRole_Success() {
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(null);
        assertTrue(userService.saveRole("ROLE_ADMIN"));
    }

    @Test
    void saveRole_Exists() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(testRole);
        assertFalse(userService.saveRole("ROLE_USER"));
    }

    @Test
    void grantRole_Success() {
        mockGetUserByUsername("johndoe", testUser);

        Role adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(2L);
        mockGetRoleByName("ROLE_ADMIN", adminRole);

        jakarta.persistence.Query countQuery = mock(jakarta.persistence.Query.class);
        when(em.createNativeQuery("SELECT COUNT(*) FROM t_user_roles WHERE user_id = :userId AND roles_id = :roleId")).thenReturn(countQuery);
        when(countQuery.setParameter("userId", 1L)).thenReturn(countQuery);
        when(countQuery.setParameter("roleId", 2L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        jakarta.persistence.Query insertQuery = mock(jakarta.persistence.Query.class);
        when(em.createNativeQuery("INSERT INTO t_user_roles (user_id, roles_id) VALUES (:userId, :roleId)")).thenReturn(insertQuery);
        when(insertQuery.setParameter("userId", 1L)).thenReturn(insertQuery);
        when(insertQuery.setParameter("roleId", 2L)).thenReturn(insertQuery);
        when(insertQuery.executeUpdate()).thenReturn(1);

        assertEquals(GrantRoleStatus.GRANT_SUCCESS, userService.grantRole("johndoe", "ROLE_ADMIN"));
    }

    @Test
    void grantRole_UserNotFound() {
        mockGetUserByUsernameThrows("unknown");
        assertEquals(GrantRoleStatus.USER_NOT_FOUND, userService.grantRole("unknown", "ROLE_ADMIN"));
    }

    @Test
    void grantRole_RoleNotFound() {
        mockGetUserByUsername("johndoe", testUser);
        mockGetRoleByNameThrows("ROLE_ADMIN");
        assertEquals(GrantRoleStatus.ROLE_NOT_FOUND, userService.grantRole("johndoe", "ROLE_ADMIN"));
    }

    @Test
    void grantRole_AlreadyExists() {
        mockGetUserByUsername("johndoe", testUser);
        mockGetRoleByName("ROLE_USER", testRole);

        jakarta.persistence.Query countQuery = mock(jakarta.persistence.Query.class);
        when(em.createNativeQuery("SELECT COUNT(*) FROM t_user_roles WHERE user_id = :userId AND roles_id = :roleId")).thenReturn(countQuery);
        when(countQuery.setParameter("userId", 1L)).thenReturn(countQuery);
        when(countQuery.setParameter("roleId", 1L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        assertEquals(GrantRoleStatus.USER_ROLE_ALREADY_EXISTS, userService.grantRole("johndoe", "ROLE_USER"));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        assertTrue(userService.deleteUser(1L));
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(userService.deleteUser(99L));
    }

    @Test
    void usergtList() {
        TypedQuery<User> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT u FROM User u WHERE u.id > :paramId", User.class)).thenReturn(query);
        when(query.setParameter("paramId", 10L)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(testUser));
        assertEquals(1, userService.usergtList(10L).size());
    }

    @Test
    void getCurrentUsername_Authenticated() {
        try (var mock = mockStatic(SecurityContextHolder.class)) {
            mock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("johndoe");
            assertEquals("johndoe", userService.getCurrentUsername());
        }
    }

    @Test
    void getCurrentUsername_Null() {
        try (var mock = mockStatic(SecurityContextHolder.class)) {
            mock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            assertNull(userService.getCurrentUsername());
        }
    }

    @Test
    void getCurrentUser_Success() {
        try (var mock = mockStatic(SecurityContextHolder.class)) {
            mock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("johndoe");
            mockGetUserByUsername("johndoe", testUser);
            assertEquals("johndoe", userService.getCurrentUser().getUsername());
        }
    }

    @Test
    void getCurrentUser_Null() {
        try (var mock = mockStatic(SecurityContextHolder.class)) {
            mock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(null);
            assertNull(userService.getCurrentUser());
        }
    }

    @Test
    void updateUser() {
        try (var mock = mockStatic(SecurityContextHolder.class)) {
            mock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("johndoe");
            mockGetUserByUsername("johndoe", testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            assertNotNull(userService.updateUser("Jane", "Doe", "janedoe", "jane@example.com", "0987654321"));
        }
    }

    @Test
    void updateUsersBusyness() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User result = userService.updateUsersBusyness(testUser, BusynessType.FREE);
        assertEquals(BusynessType.FREE, result.getBusyness());
    }

    @Test
    void findAllFreeUsers_Found() {
        when(userRepository.findAllByBusyness(BusynessType.FREE)).thenReturn(Collections.singletonList(testUser));
        assertEquals(1, userService.findAllFreeUsers("ROLE_USER").size());
    }

    @Test
    void findAllFreeUsers_Empty() {
        when(userRepository.findAllByBusyness(BusynessType.FREE)).thenReturn(Collections.emptyList());
        assertTrue(userService.findAllFreeUsers("ROLE_USER").isEmpty());
    }

    @Test
    void findAllFreeUsers_WrongRole() {
        Role otherRole = new Role("ROLE_ADMIN");
        otherRole.setId(2L);
        User otherUser = new User("Test", "User", "test", "pass", "t@t.com", "123");
        otherUser.setId(2L);
        otherUser.setRoles(new HashSet<>(Collections.singleton(otherRole)));
        otherUser.setBusyness(BusynessType.FREE);

        when(userRepository.findAllByBusyness(BusynessType.FREE)).thenReturn(Collections.singletonList(otherUser));
        assertTrue(userService.findAllFreeUsers("ROLE_USER").isEmpty());
    }

    @Test
    void findRandomFreeUser_Found() {
        when(userRepository.findAllByBusyness(BusynessType.FREE)).thenReturn(Collections.singletonList(testUser));
        assertNotNull(userService.findRandomFreeUser("ROLE_USER"));
    }

    @Test
    void findRandomFreeUser_NotFound() {
        when(userRepository.findAllByBusyness(BusynessType.FREE)).thenReturn(Collections.emptyList());
        assertNull(userService.findRandomFreeUser("ROLE_USER"));
    }

    // Вспомогательные методы
    private void mockGetUserByUsername(String username, User user) {
        CriteriaQuery<User> cq = mock(CriteriaQuery.class);
        Root<User> root = mock(Root.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<User> typedQuery = mock(TypedQuery.class);

        when(criteriaBuilder.createQuery(User.class)).thenReturn(cq);
        when(cq.from(User.class)).thenReturn(root);
        when(criteriaBuilder.equal(root.get("username"), username)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(user);
    }

    private void mockGetUserByUsernameThrows(String username) {
        CriteriaQuery<User> cq = mock(CriteriaQuery.class);
        Root<User> root = mock(Root.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<User> typedQuery = mock(TypedQuery.class);

        when(criteriaBuilder.createQuery(User.class)).thenReturn(cq);
        when(cq.from(User.class)).thenReturn(root);
        when(criteriaBuilder.equal(root.get("username"), username)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
    }

    private void mockGetRoleByName(String name, Role role) {
        CriteriaQuery<Role> cq = mock(CriteriaQuery.class);
        Root<Role> root = mock(Root.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<Role> typedQuery = mock(TypedQuery.class);

        when(criteriaBuilder.createQuery(Role.class)).thenReturn(cq);
        when(cq.from(Role.class)).thenReturn(root);
        when(criteriaBuilder.equal(root.get("name"), name)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(role);
    }

    private void mockGetRoleByNameThrows(String name) {
        CriteriaQuery<Role> cq = mock(CriteriaQuery.class);
        Root<Role> root = mock(Root.class);
        Predicate predicate = mock(Predicate.class);
        TypedQuery<Role> typedQuery = mock(TypedQuery.class);

        when(criteriaBuilder.createQuery(Role.class)).thenReturn(cq);
        when(cq.from(Role.class)).thenReturn(root);
        when(criteriaBuilder.equal(root.get("name"), name)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
    }
}