package com.damian.photogram;


import com.damian.photogram.app.notification.NotificationRepository;
import com.damian.photogram.domain.account.repository.AccountRepository;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.repository.FollowRepository;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.setting.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@DataJpaTest
public abstract class AbstractRepositoryTest {
    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    protected final String RAW_PASSWORD = "123456";

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
    protected CommentRepository commentRepository;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected LikeRepository likeRepository;

    @Autowired
    protected AccountRepository accountRepository;
}