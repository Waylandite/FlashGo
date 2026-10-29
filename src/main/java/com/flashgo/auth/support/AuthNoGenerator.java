package com.flashgo.auth.support;

import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthNoGenerator {

    public String userNo() {
        return "U" + shortId();
    }

    public String merchantNo() {
        return "M" + shortId();
    }

    public String riderNo() {
        return "R" + shortId();
    }

    private String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
