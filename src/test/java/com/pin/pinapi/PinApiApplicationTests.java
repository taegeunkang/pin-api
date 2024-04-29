package com.pin.pinapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class PinApiApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("test start");
    }

}
