package com.damian.photogram.domain.post;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.post.dto.request.CommentCreateRequest;
import com.damian.photogram.domain.post.dto.response.CommentDto;
import com.damian.photogram.domain.post.model.Comment;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customer;
    private String token;

    @AfterAll
    void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @BeforeAll
    void setUp() {
        customer = new Customer();
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode("123456"));
        customer.getAccount().setAccountStatus(AccountStatus.ACTIVE);

        customer.getProfile().setUsername("John");
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customer);
    }

    void loginWithCustomer(Customer customer) throws Exception {
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

    @Test
    @DisplayName("Should get post comments paginated")
    void shouldGetPostCommentsPaged() throws Exception {
        // given
        loginWithCustomer(customer);


        Post post = new Post(customer);
        post.setDescription("Hello world.");
        postRepository.save(post);

        Comment comment1 = new Comment(customer, post);
        comment1.setComment("Hello this is my post!");
        commentRepository.save(comment1);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/posts/{id}/comments", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode contentNode = root.get("content");

        CommentDto[] commentsDto = objectMapper.treeToValue(contentNode, CommentDto[].class);

        // then
        assertThat(commentsDto[0])
                .isNotNull()
                .extracting(
                        CommentDto::id,
                        CommentDto::postId,
                        CommentDto::username,
                        CommentDto::content,
                        CommentDto::createdAt
                )
                .containsExactly(
                        comment1.getId(),
                        post.getId(),
                        comment1.getAuthor().getProfile().getUsername(),
                        comment1.getComment(),
                        comment1.getCreatedAt().toString()
                );

    }

    @Test
    @DisplayName("Should comment in post")
    void shouldAddComment() throws Exception {
        // given
        loginWithCustomer(customer);


        Post post = new Post(customer);
        post.setDescription("Hello world.");
        postRepository.save(post);

        CommentCreateRequest request = new CommentCreateRequest("This is my first comment.");

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/posts/{id}/comment", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        CommentDto commentDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CommentDto.class
        );

        // then
        assertThat(commentDto)
                .isNotNull()
                .extracting(
                        CommentDto::id,
                        CommentDto::postId,
                        CommentDto::username,
                        CommentDto::content,
                        CommentDto::createdAt
                )
                .containsExactly(
                        commentDto.id(),
                        post.getId(),
                        customer.getProfile().getUsername(),
                        request.comment(),
                        commentDto.createdAt()
                );
    }

    @Test
    @DisplayName("Should delete comment in post")
    void shouldDeleteComment() throws Exception {
        // given
        loginWithCustomer(customer);


        Post post = new Post(customer);
        post.setDescription("Hello world.");
        postRepository.save(post);

        Comment comment = new Comment(customer, post);
        comment.setComment("hehehe");
        commentRepository.save(comment);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/comments/{id}", comment.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(204))
                .andReturn();
    }


}