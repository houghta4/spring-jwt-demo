package com.example.codercampusdemo.service;

import com.example.codercampusdemo.domain.User;
import com.example.codercampusdemo.repository.UserRepository;
import com.example.codercampusdemo.util.CustomPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepo.findByUsername(username); //findBy prefix is special with spring data. automatically figures out property

        return userOpt.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
}
