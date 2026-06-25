package com.example.tanksolarheaterbe.seed;

import com.example.tanksolarheaterbe.dto.UserRequest;
import com.example.tanksolarheaterbe.entities.Account;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import com.example.tanksolarheaterbe.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @Override
    public void run(String @NonNull ... args) throws Exception{
        if(!accountRepository.existsByEmail("admin@gmail.com")) {
            Account admin = new Account();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("1"));
            admin.setName("Admin");
            admin.setPhone("09000000000");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            accountRepository.save(admin);
        }

        if(!accountRepository.existsByEmail("cus1@gmail.com")){
            UserRequest cus1 = new UserRequest();
            cus1.setEmail("cus1@gmail.com");
            cus1.setPassword(passwordEncoder.encode("1"));
            cus1.setName("Cus1");
            cus1.setPhone("09000000000");
            accountService.create(cus1);
        }

        if(!accountRepository.existsByEmail("cus2@gmail.com")){
            UserRequest cus2 = new UserRequest();
            cus2.setEmail("cus2@gmail.com");
            cus2.setPassword(passwordEncoder.encode("1"));
            cus2.setName("Cus2");
            cus2.setPhone("09000000000");
            accountService.create(cus2);
        }
    }
}
