package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.UserRequest;
import com.example.tanksolarheaterbe.dto.UserResponse;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAll() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getById(Integer id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(account);
    }

    public UserResponse create(UserRequest request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Account account = new Account();
        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setAddress(request.getAddress());
        account.setPhone(request.getPhone());
        account.setRole("customer");
        account.setEnabled(true);

        Account saved = accountRepository.save(account);

        return mapToResponse(saved);
    }

    public void delete(Integer id) {
        accountRepository.deleteById(id);
    }

    private UserResponse mapToResponse(Account account) {

        return UserResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .address(account.getAddress())
                .phone(account.getPhone())
                .role(account.getRole())
                .enabled(account.getEnabled())
                .build();
    }
}
