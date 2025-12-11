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
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }

    public User findUserById(Long userId) {
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }
    public List<Pair<String,String>> allUsersWithRoles(){
        List<Object[]> results = userRepository.findAllUsersWithRoles();
        List<Pair<String,String>> userRolesList = new ArrayList<>();
        for(Object[] ob : results){
            String username = (String)ob[0];
            String role = ((Role)ob[1]).getName();
            userRolesList.add(Pair.of(username,role));
        }
        return userRolesList;
    }

    public List<Role> allRoles() {
        return roleRepository.findAll();
    }

    public boolean saveUser(UserDTO dto) {
        User userFromDB = userRepository.findByUsername(dto.getUsername());

        if (userFromDB != null) {
            return false;
        }

        User user = dto.toUser(passwordEncoder);
        Role role = roleRepository.findByName("ROLE_USER");
        user.setRoles(Collections.singleton(role));
        userRepository.save(user);
        return true;
    }
    @Transactional
    public boolean saveRole(String name) {
        Role roleFromDB = roleRepository.findByName(name);

        if (roleFromDB != null) {
            return false;
        }
        Role role = new Role();
        role.setName(name);
        roleRepository.save(role);
        return true;
    }
    @Transactional
    public GrantRoleStatus grantRole(String username, String name){
        Long userId = getUserIdByUsername(username);
        if (userId == null) {
            return GrantRoleStatus.USER_NOT_FOUND;
        }

        Long roleId = getRoleIdByName(name);
        if (roleId == null) {
            return GrantRoleStatus.ROLE_NOT_FOUND;
        }

        String checkSql = "SELECT COUNT(*) FROM t_user_roles WHERE user_id = :userId AND roles_id = :roleId";
        Long count = (Long) em.createNativeQuery(checkSql)
                .setParameter("userId", userId)
                .setParameter("roleId", roleId)
                .getSingleResult();

        if (count > 0) {
            return GrantRoleStatus.USER_ROLE_ALREADY_EXISTS;
        }

        String sql = "INSERT INTO t_user_roles (user_id, roles_id) VALUES (:userId, :roleId)";
        int rowsInserted = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("roleId", roleId)
                .executeUpdate();

        if (rowsInserted > 0) return GrantRoleStatus.GRANT_SUCCESS;
        return GrantRoleStatus.GRANT_FAILURE;
    }
    public boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public List<User> usergtList(Long idMin) {
        return em.createQuery("SELECT u FROM User u WHERE u.id > :paramId", User.class)
                .setParameter("paramId", idMin).getResultList();
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null; // Проверка на null
    }

    public User getUserByUsername(String username) {
        CriteriaQuery<User> criteriaQuery = em.getCriteriaBuilder().createQuery(User.class);
        Root<User> userRequest = criteriaQuery.from(User.class);

        Predicate predicate = em.getCriteriaBuilder().equal(userRequest.get("username"), username);
        criteriaQuery.where(predicate);

        try {
            return em.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null; // Или выбросьте исключение, если это более уместно
        }
    }

    public Role getRoleByName(String name) {
        CriteriaQuery<Role> criteriaQuery = em.getCriteriaBuilder().createQuery(Role.class);
        Root<Role> roleRequest = criteriaQuery.from(Role.class);

        Predicate predicate = em.getCriteriaBuilder().equal(roleRequest.get("name"), name);
        criteriaQuery.where(predicate);

        try {
            return em.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null; // Или выбросьте исключение, если это более уместно
        }
    }

    public Long getUserIdByUsername(String username) {
        User user = getUserByUsername(username);
        return user != null ? user.getId() : null;
    }
    public Long getRoleIdByName(String name) {
        Role role = getRoleByName(name);
        return role != null ? role.getId() : null;
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null) {
            return getUserByUsername(username);
        }
        return null; // Или выбросьте исключение, если пользователь не найден
    }

    @Transactional
    public User updateUser(String firstName, String lastName, String username, String email, String phone ){
        User user = getCurrentUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        return userRepository.save(user);
    }
    public User updateUsersBusyness(User user, BusynessType busyness){
        user.setBusyness(busyness);
        return userRepository.save(user);
    }
    public User findRandomFreeUser(String roleName){
        List<User> freeUsersByRole = findAllFreeUsers(roleName);

        if (!freeUsersByRole.isEmpty()) {
            Random random = new Random();
            return freeUsersByRole.get(random.nextInt(freeUsersByRole.size()));
        }

        System.out.println("нет свободных " + roleName);
        return null;
    }

    public List<User> findAllFreeUsers(String roleName){
        List<User> freeUsers = userRepository.findAllByBusyness(BusynessType.FREE);
        List<User> freeUsersByRole = freeUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> roleName.equals(role.getName())))
                .collect(Collectors.toList());

        return freeUsersByRole;
    }

}