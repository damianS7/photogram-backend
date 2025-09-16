package com.damian.photogram.core;


import com.damian.photogram.domain.notification.NotificationRepository;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.setting.SettingRepository;
import com.damian.photogram.domain.user.repository.AccountRepository;
import com.damian.photogram.domain.user.repository.CustomerRepository;
import com.damian.photogram.domain.user.repository.FollowRepository;
import com.damian.photogram.domain.user.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@DataJpaTest
public abstract class AbstractRepositoryTest {
    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withReuse(true);

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