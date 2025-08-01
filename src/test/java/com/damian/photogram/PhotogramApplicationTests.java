package com.damian.photogram;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "JWT_SECRET_KEY=THIS-IS-A-BIG-SECRET!-KEEP-IT-SAFE")
class PhotogramApplicationTests {

    @Test
    void contextLoads() {
    }

}
