package com.damian.photogram.service.notification;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.security.user.User;
import com.damian.photogram.core.util.JwtUtil;
import com.damian.photogram.domain.notification.Notification;
import com.damian.photogram.domain.notification.NotificationType;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.notification.dto.NotificationEvent;
import com.damian.photogram.web.rest.notification.dto.response.NotificationDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
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
        customer = Customer.create()
                           .setEmail("customer@test.com")
                           .setPassword(passwordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(UserRole.ADMIN)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setUsername("johnwick")
                                   .setAboutMe("hello im john")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
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
                                  .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
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
               .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    @DisplayName("Should get real time notifications for the logged user")
    void shouldGetRealTimeNotifications() throws Exception {
        // given
        loginWithCustomer(customer);

        NotificationEvent notificationEvent = new NotificationEvent(
                customer.getId(),
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
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                                  .andExpect(status().is(HttpStatus.OK.value()))
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
                .extracting(
                        NotificationDto::type,
                        NotificationDto::message,
                        NotificationDto::metadata,
                        NotificationDto::createdAt
                ).containsExactly(
                        notificationEvent.type(),
                        notificationEvent.message(),
                        notificationEvent.metadata(),
                        notificationEvent.createdAt()
                );
    }
}
