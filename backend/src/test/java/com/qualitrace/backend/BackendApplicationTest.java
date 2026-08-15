package com.qualitrace.backend;

import org.junit.jupiter.api.Test;

public class BackendApplicationTest {

    @Test
    void testMainApplication() {
        BackendApplication.main(new String[] { "--server.port=0" });
    }
}
