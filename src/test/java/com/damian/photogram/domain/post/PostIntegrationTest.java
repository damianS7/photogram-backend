package com.damian.photogram.domain.post;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.post.dto.request.PostCreateRequest;
import com.damian.photogram.domain.post.dto.response.PostDto;
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
public class PostIntegrationTest {
    private final String RAW_PASSWORD = "123456";

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
        customer = Customer.create()
                           .setMail("customer@test.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(CustomerRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setUsername("John")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.ACTIVE);
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
    @DisplayName("Should get posts paginated")
    void shouldGetPostsByUsernamePaginated() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = new Post(customer);
        post.setPhotoFilename("demo.jpg");
        post.setDescription("Hello world.");
        postRepository.save(post);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/posts/{id}", customer.getProfile().getUsername())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode contentNode = root.get("content");

        PostDto[] postsDto = objectMapper.treeToValue(contentNode, PostDto[].class);

        // then
        assertThat(postsDto[0])
                .isNotNull()
                .extracting(
                        PostDto::id,
                        PostDto::authorId,
                        PostDto::description,
                        PostDto::photoFilename,
                        PostDto::createdAt
                )
                .containsExactly(
                        postsDto[0].id(),
                        postsDto[0].authorId(),
                        postsDto[0].description(),
                        postsDto[0].photoFilename(),
                        postsDto[0].createdAt()

                );

    }

    @Test
    @DisplayName("Should create post")
    void shouldCreatePost() throws Exception {
        // given
        loginWithCustomer(customer);

        PostCreateRequest request = new PostCreateRequest(
                "photo.jpg",
                "hello world!"
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/posts")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        PostDto postDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PostDto.class
        );

        // then
        assertThat(postDto)
                .isNotNull()
                .extracting(
                        PostDto::id,
                        PostDto::authorId,
                        PostDto::description,
                        PostDto::photoFilename,
                        PostDto::createdAt
                )
                .containsExactly(
                        postDto.id(),
                        postDto.authorId(),
                        postDto.description(),
                        postDto.photoFilename(),
                        postDto.createdAt()

                );
    }

    @Test
    @DisplayName("Should delete a post")
    void shouldDeletePost() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = new Post(customer);
        post.setDescription("Hello world.");
        postRepository.save(post);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/posts/{id}", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(204))
                .andReturn();
    }
}