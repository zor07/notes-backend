package com.zor07.notesbackend.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.annotations.Test;

public class BCryptPasswordEncoderTest {

    @Test
    void printPassword() {
        System.out.println(
                new BCryptPasswordEncoder()
                        .encode("1234")
        );
    }


}
