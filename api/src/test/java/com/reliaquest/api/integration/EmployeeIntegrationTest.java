package com.reliaquest.api.integration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeDeleteRequest;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeDeleteApiResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.restassured.RestAssured;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.model.ClearType;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class EmployeeIntegrationTest {

    private static final String API_V1_PATH = "/api/v1/employee";
    private static final String API_V2_PATH = "/api/v2/employee";

    @LocalServerPort
    private Integer port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${resilience4j.retry.instances.mockServiceApiRetry.maxAttempts:3}")
    private int maxRetries;

    @Container
    static MockServerContainer mockServerContainer =
            new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"));

    static MockServerClient mockServerClient;

    private TestData testData;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort());
        registry.add("app.mock-employee-service.url", mockServerContainer::getEndpoint);
    }

    @BeforeEach
    void setUp() {
        initializeRestAssured();
        resetMockServer();
        testData = new TestData();
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("mockServiceApiCircuitBreaker");
        circuitBreaker.reset();
    }

    @Nested
    class GetEmployeeTests {
        @Test
        void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
            mockGetAllEmployees(testData.employee1, testData.employee2);

            executeGetAllEmployeesRequest()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(2))
                    .body("[0].id", is(testData.employee1.getId().toString()))
                    .body("[0].employee_name", is(testData.employee1.getEmployeeName()))
                    .body("[1].id", is(testData.employee2.getId().toString()))
                    .body("[1].employee_name", is(testData.employee2.getEmployeeName()));

            verifyMockServerCall(API_V1_PATH, 1);
        }

        @Test
        void getEmployeeById_ShouldReturnEmployee() throws Exception {
            UUID employeeId = testData.employee1.getId();
            mockGetEmployeeById(employeeId, testData.employee1);

            executeGetEmployeeByIdRequest(employeeId)
                    .statusCode(HttpStatus.OK.value())
                    .body("id", is(employeeId.toString()))
                    .body("employee_name", is(testData.employee1.getEmployeeName()))
                    .body("employee_salary", is(testData.employee1.getEmployeeSalary()))
                    .body("employee_age", is(testData.employee1.getEmployeeAge()));
        }

        @Test
        void getEmployeeById_WithInvalidUUID_ShouldReturnBadRequest() {
            given().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .get(API_V2_PATH + "/invalid-uuid")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class CreateEmployeeTests {
        @Test
        void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
            EmployeeRequest request = createEmployeeRequest();
            Employee newEmployee = createEmployeeFromRequest(request);
            mockCreateEmployee(request, newEmployee);

            executeCreateEmployeeRequest(request)
                    .statusCode(HttpStatus.CREATED.value())
                    .body("employee_name", is(request.getName()))
                    .body("employee_salary", is(request.getSalary()))
                    .body("employee_age", is(request.getAge()))
                    .body("employee_title", is(request.getTitle()));
        }

        @Test
        void createEmployee_WithInvalidData_ShouldReturnBadRequest() {
            executeCreateEmployeeRequest(new EmployeeRequest()).statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class DeleteEmployeeTests {

        @Test
        void deleteEmployeeByName_ShouldReturnSuccess() throws Exception {
            UUID employeeId = testData.employee3.getId();
            EmployeeDeleteRequest request = new EmployeeDeleteRequest("Varys");
            EmployeeDeleteApiResponse response = new EmployeeDeleteApiResponse(true, "success");
            mockGetEmployeeById(employeeId, testData.employee3);
            mockDeleteEmployee(request, response);

            executeDeleteEmployeeByIdRequest(employeeId)
                    .statusCode(HttpStatus.OK.value())
                    .body(is("Varys"));
        }

        @Test
        void deleteEmployeeById_WithInvalidUUID_ShouldReturnBadRequest() {
            given().contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .delete(API_V2_PATH + "/invalid-uuid")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void deleteEmployeeById_WhenEmployeeNotFound_ShouldReturnNotFound() {
            UUID employeeId = UUID.randomUUID();
            mockServerClient
                    .when(request().withMethod("DELETE").withPath(API_V1_PATH + "/" + employeeId))
                    .respond(response().withStatusCode(HttpStatus.NOT_FOUND.value()));

            executeDeleteEmployeeByIdRequest(employeeId).statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class ResilienceTests {

        @Test
        void getEmployeeById_ShouldRetryAndGetSuccess() throws Exception {
            UUID employeeId = testData.employee1.getId();
            mockRetryScenario(employeeId, testData.employee1);

            executeGetEmployeeByIdRequest(employeeId)
                    .statusCode(HttpStatus.OK.value())
                    .body("id", is(employeeId.toString()))
                    .body("employee_name", is(testData.employee1.getEmployeeName()));

            verifyMockServerCall(API_V1_PATH + "/" + employeeId, maxRetries);
        }

        @Test
        void getEmployeeById_ShouldHandleCircuitBreaker() {
            UUID employeeId = testData.employee1.getId();
            mockCircuitBreakerScenario(employeeId);
            await().atMost(30, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        // Test circuit breaker opening
                        for (int i = 1; i < 5; i += maxRetries) {
                            executeGetEmployeeByIdRequest(employeeId).statusCode(HttpStatus.TOO_MANY_REQUESTS.value());
                        }

                        executeGetEmployeeByIdRequest(employeeId).statusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                    });
        }
    }

    // Helper method to execute delete request
    private io.restassured.response.ValidatableResponse executeDeleteEmployeeByIdRequest(UUID id) {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(API_V2_PATH + "/" + id)
                .then();
    }

    private void mockDeleteEmployee(EmployeeDeleteRequest request, EmployeeDeleteApiResponse response)
            throws Exception {
        mockServerClient
                .when(request().withMethod("DELETE").withPath(API_V1_PATH).withBody(json(request)))
                .respond(createJsonResponse(HttpStatus.OK, objectMapper.writeValueAsString(response)));
    }

    // Helper methods for setup and mocking
    private void initializeRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    private void resetMockServer() {
        if (mockServerClient != null) {
            mockServerClient.reset();
            clearExpectations();
        }
    }

    private void clearExpectations() {
        mockServerClient.clear(request().withPath(".*"));
        mockServerClient.clear(request().withMethod("GET").withPath(API_V1_PATH), ClearType.EXPECTATIONS);
        mockServerClient.clear(request().withMethod("PUT").withPath(API_V1_PATH), ClearType.EXPECTATIONS);
        mockServerClient.clear(request().withMethod("POST").withPath(API_V1_PATH), ClearType.EXPECTATIONS);
        mockServerClient.clear(request().withMethod("DELETE").withPath(API_V1_PATH), ClearType.EXPECTATIONS);
        mockServerClient.clear(request().withMethod("PATCH").withPath(API_V1_PATH), ClearType.EXPECTATIONS);
    }

    private void mockGetAllEmployees(Employee... employees) throws Exception {
        String responseBody = createResponseBody(employees);
        mockServerClient
                .when(request().withMethod("GET").withPath(API_V1_PATH))
                .respond(createJsonResponse(HttpStatus.OK, responseBody));
    }

    private void mockGetEmployeeById(UUID id, Employee employee) throws Exception {
        mockServerClient
                .when(request().withMethod("GET").withPath(API_V1_PATH + "/" + id))
                .respond(createJsonResponse(HttpStatus.OK, createResponseBody(employee)));
    }

    private void mockCreateEmployee(EmployeeRequest request, Employee response) throws Exception {
        mockServerClient
                .when(request().withMethod("POST").withPath(API_V1_PATH).withBody(json(request)))
                .respond(createJsonResponse(HttpStatus.CREATED, createResponseBody(response)));
    }

    private void mockRetryScenario(UUID id, Employee employee) throws Exception {
        mockServerClient
                .when(request().withMethod("GET").withPath(API_V1_PATH + "/" + id), Times.exactly(maxRetries - 1))
                .respond(response()
                        .withStatusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                        .withBody("{\"error\":\"Too Many Requests\"}"));

        mockSuccessfulResponse(id, employee);
    }

    private void mockSuccessfulResponse(UUID id, Employee employee) throws Exception {
        mockServerClient
                .when(request().withMethod("GET").withPath(API_V1_PATH + "/" + id))
                .respond(createJsonResponse(HttpStatus.OK, createResponseBody(employee)));
    }

    private void mockCircuitBreakerScenario(UUID id) {
        mockServerClient
                .when(request().withMethod("GET").withPath(API_V1_PATH + "/" + id))
                .respond(response()
                        .withStatusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                        .withBody("{\"error\":\"Too Many Requests\"}"));
    }

    // Helper methods for request execution
    private io.restassured.response.ValidatableResponse executeGetAllEmployeesRequest() {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_V2_PATH)
                .then();
    }

    private io.restassured.response.ValidatableResponse executeGetEmployeeByIdRequest(UUID id) {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(API_V2_PATH + "/" + id)
                .then();
    }

    private io.restassured.response.ValidatableResponse executeCreateEmployeeRequest(EmployeeRequest request) {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(API_V2_PATH)
                .then();
    }

    // Helper methods for response creation
    private String createResponseBody(Employee... employees) throws Exception {
        if (employees.length == 1) {
            return String.format(
                    """
                            {
                                "status": "success",
                                "data": %s
                            }
                            """,
                    objectMapper.writeValueAsString(employees[0]));
        }

        StringBuilder employeesJson = new StringBuilder();
        for (int i = 0; i < employees.length; i++) {
            employeesJson.append(objectMapper.writeValueAsString(employees[i]));
            if (i < employees.length - 1) {
                employeesJson.append(", ");
            }
        }

        return String.format(
                """
                        {
                            "status": "success",
                            "data": [%s]
                        }
                        """,
                employeesJson);
    }

    private org.mockserver.model.HttpResponse createJsonResponse(HttpStatus status, String body) {
        return response()
                .withStatusCode(status.value())
                .withHeaders(new Header("Content-Type", "application/json"))
                .withBody(json(body));
    }

    private void verifyMockServerCall(String path, int times) {
        mockServerClient.verify(request().withMethod("GET").withPath(path), VerificationTimes.exactly(times));
    }

    // Test data class
    private static class TestData {
        private final Employee employee1;
        private final Employee employee2;
        private final Employee employee3;

        TestData() {
            UUID uuid1 = UUID.randomUUID();
            employee1 = createEmployee(uuid1, "Hanumant Shinde", 75000, 30);
            UUID uuid2 = UUID.randomUUID();
            employee2 = createEmployee(uuid2, "Peter Dinklage", 85000, 35);
            UUID uuid3 = UUID.randomUUID();
            employee3 = createEmployee(uuid3, "Varys", 90000, 20);
        }

        private Employee createEmployee(UUID id, String name, int salary, int age) {
            Employee employee = new Employee();
            employee.setId(id);
            employee.setEmployeeName(name);
            employee.setEmployeeSalary(salary);
            employee.setEmployeeAge(age);
            return employee;
        }
    }

    private EmployeeRequest createEmployeeRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setName("New Employee");
        request.setSalary(65000);
        request.setAge(25);
        request.setTitle("Engineer");
        return request;
    }

    private Employee createEmployeeFromRequest(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setEmployeeName(request.getName());
        employee.setEmployeeSalary(request.getSalary());
        employee.setEmployeeAge(request.getAge());
        employee.setEmployeeTitle(request.getTitle());
        return employee;
    }
}
