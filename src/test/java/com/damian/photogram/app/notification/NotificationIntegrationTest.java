package com.damian.photogram.app.notification;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.app.notification.dto.NotificationDto;
import com.damian.photogram.app.notification.dto.NotificationEvent;
import com.damian.photogram.core.security.user.User;
import com.damian.photogram.core.common.JwtUtil;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.UserRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationIntegrationTest extends AbstractIntegrationTest {
    private Customer customer;

    @Autowired
    private JwtUtil jwtUtil;

    @SpyBean
    private NotificationService notificationService;

    @BeforeAll
    void setUp() {
        customer = new Customer();
        customer.setRole(UserRole.ADMIN);
        customer.setEmail("customer@demo.com");
        customer.setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD));
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setImageFilename("no photoPath");

        customerRepository.save(customer);
    }

    @Test
    @DisplayName("Should get notifications for the logged user")
    void shouldGetNotifications() throws Exception {
        // given
        Notification notification = Notification
                .create(customer)
                .setMessage("Alice has follow you.")
                .setType(NotificationType.FOLLOW)
                .setMetadata(
                        Map.of(
                                "username", "alice"
                        )
                );
        notificationRepository.save(notification);

        // when
        loginWithCustomer(customer);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .get("/api/v1/notifications")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(200))
                                  .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // then
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode contentNode = root.get("content");

        NotificationDto[] notificationsDto = objectMapper.treeToValue(contentNode, NotificationDto[].class);

        // then
        assertThat(notificationsDto.length).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should delete notifications for the logged user")
    void shouldDeleteNotifications() throws Exception {
        // given
        Notification notification = Notification
                .create(customer)
                .setMessage("Alice has follow you.")
                .setType(NotificationType.FOLLOW)
                .setMetadata(
                        Map.of(
                                "username", "alice"
                        )
                );
        notificationRepository.save(notification);

        // when
        loginWithCustomer(customer);
        mockMvc.perform(MockMvcRequestBuilders
                       .delete("/api/v1/notifications")
                       .contentType(MediaType.APPLICATION_JSON)
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(204))
               .andReturn();

    }

    @Test
    @DisplayName("Should get real time notifications for the logged user")
    void shouldGetRealTimeNotifications() throws Exception {
        // given
        final String givenToken = jwtUtil.generateToken(
                customer.getEmail(),
                new Date(System.currentTimeMillis() + 1000 * 60 * 60)
        );

        NotificationEvent notificationEvent = new NotificationEvent(
                1L,
                NotificationType.LIKE,
                Map.of("postId", 123),
                "Message",
                "2025-09-10T00:00:00"
        );

        User user = new User(customer);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // when
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

        when(notificationService.getNotificationsForUser())
                .thenReturn(Flux.just(notificationEvent));

        MvcResult result = mockMvc.perform(get("/api/v1/notifications/stream")
                                          .accept(MediaType.TEXT_EVENT_STREAM)
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + givenToken))
                                  .andExpect(status().isOk())
                                  .andReturn();

        String rawResponse = result.getResponse().getContentAsString();
        // Cada evento SSE va en una línea, empieza con "data:"
        String json = rawResponse.replaceFirst("data:", "").trim();

        NotificationDto notificationDto = objectMapper.readValue(
                json,
                NotificationDto.class
        );

        assertThat(notificationDto)
                .isNotNull()
                .extracting("type", "message", "metadata", "createdAt")
                .containsExactly(
                        notificationEvent.type(),
                        notificationEvent.message(),
                        notificationEvent.metadata(),
                        notificationEvent.createdAt()
                );
    }
}
