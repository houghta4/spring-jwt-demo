package com.example.codercampusdemo.web;

import com.example.codercampusdemo.domain.User;
import com.example.codercampusdemo.dto.AuthCredentialsRequest;
import com.example.codercampusdemo.repository.UserRepository;
import com.example.codercampusdemo.service.UserDetailsServiceImpl;
import com.example.codercampusdemo.util.CustomPasswordEncoder;
import com.example.codercampusdemo.util.JwtUtil;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    CustomPasswordEncoder customPasswordEncoder;

    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token, @AuthenticationPrincipal UserDetails user) {
        try{
            Boolean isTokenValid = jwtUtil.validateToken(token, user);
            return ResponseEntity.ok(isTokenValid);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("signup")
    public ResponseEntity<?> signup(@RequestBody AuthCredentialsRequest request){
        if(userDetailsService.loadUserByUsername(request.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            PasswordEncoder passwordEncoder = customPasswordEncoder.getPasswordEncoder();
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCohortStartDate(LocalDate.now());
            userDetailsService.saveUser(user);

            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            return ResponseEntity.ok().header(HttpHeaders.ACCEPT, "Accepted").body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody AuthCredentialsRequest request){
        try{
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword());
            String s = customPasswordEncoder.getPasswordEncoder().encode("asdfasdf");
            Authentication authenticate = authenticationManager
                    .authenticate(
                            token
                    );
            User user = (User) authenticate.getPrincipal();
            user.setPassword(null); //maybe will cause issues later
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            jwtUtil.generateToken(user)
                    )
                    .body(user);
            //Adding Auth Login Endpoint for JWT Token Creation 20:22, can make dto and return that if we don't want to expose password
        }catch (BadCredentialsException ex){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
