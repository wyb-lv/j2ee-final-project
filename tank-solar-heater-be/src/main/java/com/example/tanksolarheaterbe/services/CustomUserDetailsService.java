package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .disabled(Boolean.FALSE.equals(account.getEnabled()))
                .authorities(List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + account.getRole().toUpperCase()
                        )
                ))
                .build();
    }
}
