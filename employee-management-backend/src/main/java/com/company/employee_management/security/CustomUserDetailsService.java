package com.company.employee_management.security;

import com.company.employee_management.entity.User;
import com.company.employee_management.repository.EmployeeRepository;
import com.company.employee_management.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 1. Try finding by username or email
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);

        // 2. If not found, try finding by Employee ID (e.g. EMP001)
        if (userOpt.isEmpty()) {
            userOpt = employeeRepository.findByEmployeeCode(identifier.trim().toUpperCase())
                    .map(com.company.employee_management.entity.Employee::getUser);
        }

        User user = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("User not found with identifier or employee code: " + identifier));

        return UserPrincipal.create(user);
    }
}

