package com.ksu.workers.service;

import com.ksu.common.entities.*;

import com.ksu.workers.DTO.TaskDTO;
import com.ksu.workers.entities.*;
import com.ksu.workers.repository.TaskRepository;
import com.ksu.workers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserService userService;
    @Autowired
    private RestTemplate restTemplate;

    private Queue<Task> tasksForCollector = new LinkedList<>();
    private Queue<Task> tasksForCurier = new LinkedList<>();
    private Queue<Task> tasksForStorage = new LinkedList<>();

    String onlineShopServiceUrl = "http://localhost:8080/home/order/changestatus";
    String onlineShopServiceProductUrl = "http://localhost:8080/home/order/changeproductquantity";

    @Transactional
    public boolean saveTask(TaskDTO dto) {
        System.out.println(dto.getUsername());
        User userFromDB = userRepository.findByUsername(dto.getUsername());
        if (userFromDB == null) {
            System.out.println("пользователя нет");
            return false;
        }
        System.out.println("сохраняем");
        Task task = new Task(userFromDB,dto.getName(),dto.getDescription(), TaskType.IN_WORK, TaskPurpose.COMMON,0L );
        taskRepository.save(task);
        return true;
    }

    public List<Task> findTasksByUserId(Long id) {
        return taskRepository.findByUserId(id);
    }

    public List<Task> findActiveTasksByUserId(Long id) {

        return taskRepository.findByUserId(id).stream().filter(task -> task.getTaskType() == TaskType.IN_WORK || task.getTaskType() == TaskType.WAITING)
                .collect(Collectors.toList());
    }

    @Transactional
    public void newOrder(OrderDTO order, HttpHeaders headers){
        System.out.println("поступил заказ " + order.getAddress());
        StringBuilder collectorDescription = new StringBuilder("Товары:");
        Integer i = 1;
        for (CartDTO cart : order.getCarts()){
            collectorDescription.append(i).append(". ").append(cart.getProduct().getName()).append(". ").append(cart.getProductQuantity()).append("шт.\n");
        }


        User collector = userService.findRandomFreeUser("ROLE_COLLECTOR");
        if (collector != null){
            System.out.println("заказ берет коллектор "+ collector.getUsername());
            Task taskForCollector = new Task(collector,"Сборка заказа номер " + order.getId().toString(), collectorDescription.toString(), TaskType.IN_WORK, TaskPurpose.COLLECTING, order.getId());
            taskRepository.save(taskForCollector);
            userService.updateUsersBusyness(collector, BusynessType.IN_WORK);
            HttpEntity<Pair<Long,OrderStatus>> statusRequest = new HttpEntity<>(Pair.of(order.getId(),OrderStatus.COLLECTING), headers);
            try {
                restTemplate.postForEntity(onlineShopServiceUrl, statusRequest, String.class);
            } catch (RestClientException e) {
                System.err.println("Ошибка при отправке запроса: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else{
            Task taskInQueue = new Task(null,"Сборка заказа номер " + order.getId().toString(), collectorDescription.toString(), TaskType.IN_QUEUE, TaskPurpose.COLLECTING, order.getId());
            taskRepository.save(taskInQueue);
            tasksForCollector.add(taskInQueue);
        }


        User curier = userService.findRandomFreeUser("ROLE_CURIER");
        if (curier != null){
            Task taskForCurier = new Task(curier,"Доставка заказа номер " + order.getId().toString(),
                    "Адрес: " + order.getAddress() + ". Общее количество товаров: " + order.getQuantity().toString() + ". Сумма: " + order.getAmount().toString() + "руб.", TaskType.WAITING, TaskPurpose.DELIVERY, order.getId());
            taskRepository.save(taskForCurier);
            userService.updateUsersBusyness(curier, BusynessType.IN_WORK);
        }
        else{
            Task taskInQueue = new Task(null,"Доставка заказа номер " + order.getId().toString(),
                    "Адрес: " + order.getAddress() + ". Общее количество товаров: " + order.getQuantity().toString() + ". Сумма: " + order.getAmount().toString() + "руб.", TaskType.IN_QUEUE, TaskPurpose.DELIVERY, order.getId());
            taskRepository.save(taskInQueue);
            tasksForCurier.add(taskInQueue);
        }

    }

    @Transactional
    public void newStorageRequest(ProductDTOWithId product){
        User storage = userService.findRandomFreeUser("ROLE_STORAGE");
        if (storage != null){
            Task taskForStorage = new Task(storage,"Пополнить склад товаром " + product.getId(), "Товар: " + product.getName() + ". Материал: " + product.getMaterial(), TaskType.IN_WORK, TaskPurpose.RESTOCKING, product.getId());
            taskRepository.save(taskForStorage);
            userService.updateUsersBusyness(storage, BusynessType.IN_WORK);
        }
        else{
            Task taskInQueue = new Task(null,"Пополнение склада  товаром " + product.getId(), "Товар: " + product.getName() + ". Материал: " + product.getMaterial(), TaskType.IN_QUEUE, TaskPurpose.RESTOCKING, product.getId());
            taskRepository.save(taskInQueue);
            tasksForStorage.add(taskInQueue);
        }

    }

    public List<Task> findAllTasks(){
        return taskRepository.findAll();
    }

    @Transactional
    public void completeTask(Long id, HttpHeaders headers){
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()){
            Task task = taskOpt.get();
            task.setTaskType(TaskType.FINISHED);
            taskRepository.save(task);
            User user = task.getUser();
            userService.updateUsersBusyness(user, BusynessType.FREE);
            if (task.getTaskPurpose() == TaskPurpose.COLLECTING){
                Task curierTask = taskRepository.findByTaskPurposeAndOrderId(TaskPurpose.DELIVERY, task.getOrderId());
                if (curierTask.getUser() != null){
                    curierTask.setTaskType(TaskType.IN_WORK);
                    taskRepository.save(curierTask);
                    HttpEntity<Pair<Long,OrderStatus>> statusRequest = new HttpEntity<>(Pair.of(task.getOrderId(),OrderStatus.DELIVERING), headers);
                    restTemplate.postForEntity(onlineShopServiceUrl, statusRequest, String.class);
                }
                else {
                    HttpEntity<Pair<Long,OrderStatus>> statusRequest = new HttpEntity<>(Pair.of(task.getOrderId(),OrderStatus.COLLECTED), headers);
                    restTemplate.postForEntity(onlineShopServiceUrl, statusRequest , String.class);
                }
            }
            if (task.getTaskPurpose() == TaskPurpose.DELIVERY){
                HttpEntity<Pair<Long,OrderStatus>> statusRequest = new HttpEntity<>(Pair.of(task.getOrderId(),OrderStatus.RECEIVED), headers);
                restTemplate.postForEntity(onlineShopServiceUrl, statusRequest, String.class);
            }
            if (task.getTaskPurpose() == TaskPurpose.RESTOCKING){
                HttpEntity<Long> productRequest = new HttpEntity<>(task.getOrderId(), headers);
                restTemplate.postForEntity(onlineShopServiceProductUrl,productRequest , String.class);
            }
        }
    }



    @Scheduled(fixedRate = 3000) // 3000 миллисекунд = 3 сек
    public void findFreeUsers() {
        if (!tasksForCurier.isEmpty()) {
            List<User> curiers = userService.findAllFreeUsers("ROLE_CURIER");
            while (!tasksForCurier.isEmpty() && !curiers.isEmpty()) {
                Task task = tasksForCurier.poll(); // Берем первую задачу из очереди
                User curier = curiers.removeFirst(); // Берем текущего курьера
                task.setUser(curier);
                task.setTaskType(TaskType.IN_WORK);
                taskRepository.save(task);
                userService.updateUsersBusyness(curier,BusynessType.IN_WORK);
                Task prevTask = taskRepository.findByTaskPurposeAndOrderId(TaskPurpose.COLLECTING, task.getOrderId());
                if (prevTask.getTaskType() == TaskType.FINISHED){
                    restTemplate.postForObject(onlineShopServiceUrl, Pair.of(task.getOrderId(),OrderStatus.DELIVERING), String.class);
                }
            }

        }

        if (!tasksForCollector.isEmpty()) {
            List<User> collectors = userService.findAllFreeUsers("ROLE_COLLECTOR");
            while (!tasksForCollector.isEmpty() && !collectors.isEmpty()) {
                Task task = tasksForCollector.poll();
                User collector = collectors.removeFirst();

                task.setUser(collector);
                task.setTaskType(TaskType.IN_WORK);
                taskRepository.save(task);
                userService.updateUsersBusyness(collector,BusynessType.IN_WORK);
                restTemplate.postForObject(onlineShopServiceUrl, Pair.of(task.getOrderId(),OrderStatus.COLLECTING), String.class);
            }
        }
        if (!tasksForStorage.isEmpty()) {
            List<User> storages = userService.findAllFreeUsers("ROLE_STORAGE");
            while (!tasksForStorage.isEmpty() && !storages.isEmpty()) {
                Task task = tasksForStorage.poll();
                User storage = storages.removeFirst();
                task.setUser(storage);
                task.setTaskType(TaskType.IN_WORK);
                taskRepository.save(task);
                userService.updateUsersBusyness(storage,BusynessType.IN_WORK);
            }

        }
    }



}