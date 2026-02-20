package com.reliaquest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.client.dto.UpstreamDeleteEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class EmployeeControllerIT extends BaseIT {

    @Test
    void getAllEmployees_returnsAllEmployees() throws IOException {

        stubFor(get(urlPathMatching("/employee"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/all-employees-full-client-response.json")))));

        List<Employee> response = webTestClient
                .get()
                .uri("/api/employee")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Employee>>() {})
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(15, response.size());
    }

    @Test
    void getAllEmployees_clientCallFails_Return500() {

        stubFor(get(urlPathMatching("/employee"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("Server Error"))));

        webTestClient
                .get()
                .uri("/api/employee")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(500)
                .jsonPath("$.message")
                .isEqualTo("Server Error");
    }

    @Test
    void getEmployeesByNameSearch_whenPartialMatch_returnsMatchingEmployees() throws IOException {
        stubFor(get(urlPathMatching("/employee"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/all-employees-full-client-response.json")))));

        List<Employee> response = webTestClient
                .get()
                .uri("/api/employee/search/ak")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<Employee>>() {})
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Naruto Uzumaki", response.get(0).getName());
    }

    @Test
    void getEmployeeById_whenValidId_returnsEmployee() throws IOException {
        stubFor(get(urlPathMatching("/employee/9001"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/single-employee-9001-full-client-response.json")))));

        Employee response = webTestClient
                .get()
                .uri("/api/employee/9001")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<Employee>() {})
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("Naruto Uzumaki", response.getName());
    }

    @Test
    void getHighestSalaryOfEmployees_returnsHighestSalary() throws IOException {
        stubFor(get(urlPathMatching("/employee"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/all-employees-full-client-response.json")))));

        Integer response = webTestClient
                .get()
                .uri("/api/employee/highestSalary")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<Integer>() {})
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(950000, response);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_returnTopTenHighestEarningEmployeesNames() throws IOException {
        stubFor(get(urlPathMatching("/employee"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/all-employees-full-client-response.json")))));

        List<String> response = webTestClient
                .get()
                .uri("/api/employee/topTenHighestEarningEmployeeNames")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<String>>() {})
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(10, response.size());
        assertEquals("Tony Stark", response.get(0));
        assertEquals("Bruce Wayne", response.get(1));
        assertEquals("Taylor Swift", response.get(2));
        assertEquals("Diana Prince", response.get(3));
        assertEquals("Hermione Granger", response.get(4));
        assertEquals("Clark Kent", response.get(5));
        assertEquals("Geralt of Rivia", response.get(6));
        assertEquals("Keanu Reeves", response.get(7));
        assertEquals("Zendaya Coleman", response.get(8));
        assertEquals("Selena Kyle", response.get(9));
    }

    @Test
    void createEmployee_validRequest_returnsCreatedEmployee() throws IOException {
        stubFor(post(urlPathMatching("/employee"))
                .withRequestBody(equalToJson(readStringFromFile("request/create-employee-good-30.json")))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/employee-30-created-client-response.json")))));

        Employee response = webTestClient
                .post()
                .uri("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(readStringFromFile("request/create-employee-good-30.json"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<Employee>() {})
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("Steph Curry", response.getName());
    }

    @Test
    void createEmployee_invalidRequest_returnsBadRequest_andDoesNotCallUpstream() throws IOException {
        webTestClient
                .post()
                .uri("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(readStringFromFile("request/create-employee-bad.json"))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("age: must be greater than or equal to 16")));

        verify(0, postRequestedFor(urlEqualTo("/employee")));
    }

    @Test
    void deleteEmployee_validRequest_returnsDeletedEmployeeName() throws IOException {
        UpstreamDeleteEmployeeRequest deleteRequest = new UpstreamDeleteEmployeeRequest();
        deleteRequest.setName("Naruto Uzumaki");

        stubFor(get(urlPathMatching("/employee/9001"))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/single-employee-9001-full-client-response.json")))));

        stubFor(delete(urlPathMatching("/employee"))
                .withRequestBody(equalToJson(
                        """
                            { "name": "Naruto Uzumaki" }
                        """))
                .willReturn((aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("response/delete-employee-successful-client-response.json")))));

        String response = webTestClient
                .delete()
                .uri("/api/employee/9001")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("Naruto Uzumaki", response);
    }
}
