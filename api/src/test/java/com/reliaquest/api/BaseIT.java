package com.reliaquest.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public abstract class BaseIT {
    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();
    }

    protected static String readStringFromFile(String filePath) throws IOException {
        return Files.readString(Path.of("src/test/resources/" + filePath));
    }
}
