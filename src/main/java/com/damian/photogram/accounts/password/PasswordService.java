package com.damian.photogram.accounts.password;

import com.damian.photogram.accounts.AccountRepository;
import com.damian.photogram.accounts.token.AccountTokenRepository;
import com.damian.photogram.common.utils.JWTUtil;
import com.damian.photogram.customers.CustomerService;
import com.damian.photogram.mail.EmailSenderService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccountRepository accountRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final EmailSenderService emailSenderService;

    public PasswordService(
            JWTUtil jwtUtil,
            AuthenticationManager authenticationManager,
            CustomerService customerService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AccountRepository accountRepository,
            AccountTokenRepository accountTokenRepository,
            EmailSenderService emailSenderService
    ) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.accountRepository = accountRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.emailSenderService = emailSenderService;
    }


}
