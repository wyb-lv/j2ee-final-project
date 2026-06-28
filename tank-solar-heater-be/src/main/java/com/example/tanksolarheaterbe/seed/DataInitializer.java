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
    private static final String SEED_PASSWORD = "123456";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @Override
    public void run(String @NonNull ... args) throws Exception {

        // Admin is created directly so we can set role = ADMIN.
        accountRepository.findByEmail("admin@gmail.com").ifPresentOrElse(
                this::syncPassword,
                () -> {
                    Account admin = new Account();
                    admin.setEmail("admin@gmail.com");
                    admin.setPassword(passwordEncoder.encode(SEED_PASSWORD));
                    admin.setName("Admin");
                    admin.setPhone("09000000000");
                    admin.setRole("ADMIN");
                    admin.setEnabled(true);
                    accountRepository.save(admin);
                });

        ensureCustomer("cus1@gmail.com", "Cus1", "0900000000");
        ensureCustomer("cus2@gmail.com", "Cus2", "0900000000");
    }

    private void ensureCustomer(String email, String name, String phone) {
        accountRepository.findByEmail(email).ifPresentOrElse(
                this::syncPassword,
                () -> {
                    UserRequest req = new UserRequest();
                    req.setEmail(email);
                    req.setPassword(SEED_PASSWORD);
                    req.setName(name);
                    req.setPhone(phone);
                    accountService.create(req);
                });
    }

    private void syncPassword(Account account) {
        if (!passwordEncoder.matches(SEED_PASSWORD, account.getPassword())) {
            account.setPassword(passwordEncoder.encode(SEED_PASSWORD));
            accountRepository.save(account);
        }
    }
}
