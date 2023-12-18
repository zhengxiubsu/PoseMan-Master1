package com.SCU.pose.service;

import com.SCU.pose.model.User;
import com.SCU.pose.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        //check if a user with the same name already exists
        return userRepository.save(user);
    }


    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);

    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public User updateUser(User user) {
        return userRepository.save(user); // save 方法用于创建和更新
    }

}
