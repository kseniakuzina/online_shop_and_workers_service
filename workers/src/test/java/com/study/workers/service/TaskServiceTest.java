package com.study.workers.service;

import com.study.common.entities.*;
import com.study.workers.DTO.TaskDTO;
import com.study.workers.entities.*;
import com.study.workers.repository.TaskRepository;
import com.study.workers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User("John", "Doe", "johndoe", "pass", "john@example.com", "1234567890");
        testUser.setId(1L);
        testUser.setRoles(new HashSet<>());
        testUser.setBusyness(BusynessType.FREE);

        testTask = new Task(testUser, "Test Task", "Description", TaskType.IN_WORK, TaskPurpose.COMMON, 0L);
        testTask.setId(1L);
    }

    @Test
    void saveTask_Success() {
        TaskDTO dto = new TaskDTO("johndoe", "Task", "Desc");
        when(userRepository.findByUsername("johndoe")).thenReturn(testUser);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        String result = taskService.saveTask(dto);

        assertEquals("johndoe", result);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void saveTask_UserNotFound() {
        TaskDTO dto = new TaskDTO("unknown", "Task", "Desc");
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        assertNull(taskService.saveTask(dto));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void findTasksByUserId() {
        when(taskRepository.findByUserId(1L)).thenReturn(Collections.singletonList(testTask));

        List<Task> result = taskService.findTasksByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void findActiveTasksByUserId() {
        Task waitingTask = new Task(testUser, "Waiting", "Desc", TaskType.WAITING, TaskPurpose.COMMON, 0L);
        Task finishedTask = new Task(testUser, "Finished", "Desc", TaskType.FINISHED, TaskPurpose.COMMON, 0L);

        when(taskRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTask, waitingTask, finishedTask));

        List<Task> result = taskService.findActiveTasksByUserId(1L);

        assertEquals(2, result.size());
    }

    @Test
    void newOrder_CollectorAndCurierFound() {
        OrderDTO order = createOrderDTO();
        HttpHeaders headers = new HttpHeaders();

        User collector = new User("Collector", "User", "collector", "pass", "c@c.com", "111");
        collector.setId(2L);
        collector.setRoles(new HashSet<>(Collections.singleton(new Role("ROLE_COLLECTOR"))));
        collector.setBusyness(BusynessType.FREE);

        User curier = new User("Curier", "User", "curier", "pass", "d@d.com", "222");
        curier.setId(3L);
        curier.setRoles(new HashSet<>(Collections.singleton(new Role("ROLE_CURIER"))));
        curier.setBusyness(BusynessType.FREE);

        when(userService.findRandomFreeUser("ROLE_COLLECTOR")).thenReturn(collector);
        when(userService.findRandomFreeUser("ROLE_CURIER")).thenReturn(curier);
        when(taskRepository.save(any(Task.class))).thenReturn(new Task());
        when(userService.updateUsersBusyness(any(User.class), eq(BusynessType.IN_WORK))).thenReturn(collector);

        taskService.newOrder(order, headers);

        verify(taskRepository, times(2)).save(any(Task.class));
        verify(userService, times(2)).updateUsersBusyness(any(User.class), eq(BusynessType.IN_WORK));
    }

    @Test
    void newOrder_NoCollectorNoCurier() {
        OrderDTO order = createOrderDTO();
        HttpHeaders headers = new HttpHeaders();

        when(userService.findRandomFreeUser("ROLE_COLLECTOR")).thenReturn(null);
        when(userService.findRandomFreeUser("ROLE_CURIER")).thenReturn(null);
        when(taskRepository.save(any(Task.class))).thenReturn(new Task());

        taskService.newOrder(order, headers);

        verify(taskRepository, times(2)).save(any(Task.class));
    }

    @Test
    void newStorageRequest_StorageFound() {
        ProductDTOWithId product = new ProductDTOWithId();
        product.setId(1L);
        product.setName("Product");
        product.setMaterial("Wood");

        User storage = new User("Storage", "User", "storage", "pass", "s@s.com", "333");
        storage.setId(4L);
        storage.setRoles(new HashSet<>(Collections.singleton(new Role("ROLE_STORAGE"))));
        storage.setBusyness(BusynessType.FREE);

        when(userService.findRandomFreeUser("ROLE_STORAGE")).thenReturn(storage);
        when(taskRepository.save(any(Task.class))).thenReturn(new Task());
        when(userService.updateUsersBusyness(any(User.class), eq(BusynessType.IN_WORK))).thenReturn(storage);

        taskService.newStorageRequest(product);

        verify(taskRepository).save(any(Task.class));
        verify(userService).updateUsersBusyness(any(User.class), eq(BusynessType.IN_WORK));
    }

    @Test
    void newStorageRequest_NoStorage() {
        ProductDTOWithId product = new ProductDTOWithId();
        product.setId(1L);
        product.setName("Product");
        product.setMaterial("Wood");

        when(userService.findRandomFreeUser("ROLE_STORAGE")).thenReturn(null);
        when(taskRepository.save(any(Task.class))).thenReturn(new Task());

        taskService.newStorageRequest(product);

        verify(taskRepository).save(any(Task.class));
        verify(userService, never()).updateUsersBusyness(any(User.class), any(BusynessType.class));
    }

    @Test
    void findAllTasks() {
        when(taskRepository.findAll()).thenReturn(Collections.singletonList(testTask));

        List<Task> result = taskService.findAllTasks();

        assertEquals(1, result.size());
    }

    @Test
    void completeTask_Success() {
        HttpHeaders headers = new HttpHeaders();
        testTask.setTaskPurpose(TaskPurpose.DELIVERY);
        testTask.setOrderId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(userService.updateUsersBusyness(any(User.class), eq(BusynessType.FREE))).thenReturn(testUser);

        taskService.completeTask(1L, headers);

        assertEquals(TaskType.FINISHED, testTask.getTaskType());
        verify(taskRepository).save(testTask);
    }


    @Test
    void completeTask_NotFound() {
        HttpHeaders headers = new HttpHeaders();
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        taskService.completeTask(99L, headers);

        verify(taskRepository, never()).save(any(Task.class));
    }

    private OrderDTO createOrderDTO() {
        ProductDTOWithId product = new ProductDTOWithId(1L, "Test Product", 10, "Wood", 100.0, "image.jpg", Gender.MALE, ClothesType.OUTERWEAR);
        CartDTO cart = new CartDTO(1L, product, 2, 200.0, CartStatus.IN_CART);
        List<CartDTO> carts = Collections.singletonList(cart);
        OrderDTO order = new OrderDTO(1L, 3, 1500.0, carts, OrderStatus.ACCEPTED, "Test Address");
        return order;
    }
}