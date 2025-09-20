package com.damian.photogram.core;


import com.damian.photogram.core.util.JwtUtil;
import com.damian.photogram.domain.notification.NotificationRepository;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.setting.SettingRepository;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.*;
import com.damian.photogram.web.rest.auth.dto.AuthenticationRequest;
import com.damian.photogram.web.rest.auth.dto.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {
    @Container
    @ServiceConnection
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withReuse(true);

    protected final String RAW_PASSWORD = "123456";

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected AccountTokenRepository accountTokenRepository;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected ProfileRepository profileRepository;

    @Autowired
    protected SettingRepository settingRepository;

    @Autowired
    protected FollowRepository followRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    protected String token;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected LikeRepository likeRepository;

    @AfterAll
    void tearDown() {
        accountTokenRepository.deleteAll();
        accountRepository.deleteAll();
        commentRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
        followRepository.deleteAll();
        notificationRepository.deleteAll();
        settingRepository.deleteAll();
        profileRepository.deleteAll();
        customerRepository.deleteAll();
    }

    protected void loginWithCustomer(Customer customer) throws Exception {
        // given
        final HashMap<String, Object> claims = new HashMap<>();
        claims.put("email", customer.getEmail());
        claims.put("role", customer.getRole());

        token = jwtUtil.generateToken(claims, customer.getEmail());
    }

    protected void loginWithPost(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        token = response.token();

    }
}