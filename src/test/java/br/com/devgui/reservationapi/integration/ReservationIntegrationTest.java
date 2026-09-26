package br.com.devgui.reservationapi.integration;

import br.com.devgui.reservationapi.api.dto.error.ErrorResponse;
import br.com.devgui.reservationapi.api.dto.error.ValidationErrorResponse;
import br.com.devgui.reservationapi.domain.model.Reservation;
import br.com.devgui.reservationapi.domain.model.enums.ReservationStatus;
import br.com.devgui.reservationapi.testconfig.TestcontainersConfiguration;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class ReservationIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    @LocalServerPort
    private int port;
    private static final String BASE_PATH = "/api/reservations";
    private static final String CONTENT_TYPE = "application/json";

    @BeforeAll
    static void setup(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void beforeEach(){
        specification = new RequestSpecBuilder()
                .setBasePath(BASE_PATH)
                .setPort(port)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @DisplayName("Given a valid reservation When creating a reservation Then return the created reservation")
    void givenValidReservation_WhenCreateReservation_ThenReturnCreatedReservation(){
        Reservation reservation = new Reservation(
                "Guilherme", "guilherme@email.com",
                LocalDateTime.of(2026, 7, 10, 10, 0),
                LocalDateTime.of(2026, 7, 12, 10, 0),
                3
        );

        String content = given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(content, Reservation.class);

        assertNotNull(createdReservation);
        assertNotNull(createdReservation.getId());
        assertNotNull(createdReservation.getCustomerName());
        assertNotNull(createdReservation.getCustomerEmail());
        assertNotNull(createdReservation.getStartAt());
        assertNotNull(createdReservation.getEndAt());
        assertNotNull(createdReservation.getStatus());
        assertNotNull(createdReservation.getPeople());
        assertNotNull(createdReservation.getCreatedAt());
        assertNotNull(createdReservation.getUpdatedAt());

        assertEquals(reservation.getCustomerName(), createdReservation.getCustomerName());
        assertEquals(reservation.getCustomerEmail(), createdReservation.getCustomerEmail());
        assertEquals(reservation.getStartAt(), createdReservation.getStartAt());
        assertEquals(reservation.getEndAt(), createdReservation.getEndAt());
        assertEquals(reservation.getStatus(), createdReservation.getStatus());
        assertEquals(reservation.getPeople(), createdReservation.getPeople());
    }

    @Test
    @DisplayName("Given a reservation with conflicting time When creating a reservation Then return conflict error")
    void givenReservationWithConflictingTime_WhenCreateReservation_ThenReturnConflictError(){
        Reservation reservation1 = new Reservation(
                "ennyedja", "ennyedja@email.com",
                LocalDateTime.of(2026, 3, 11, 10, 0),
                LocalDateTime.of(2026, 3, 13, 10, 0),
                7
        );

        given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation1)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().body().asString();

        Reservation reservation2 = new Reservation(
                "bernardo", "bernardo@email.com",
                LocalDateTime.of(2026, 3, 12, 10, 0),
                LocalDateTime.of(2026, 3, 13, 10, 0),
                9
        );

        String content = given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation2)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .extract().body().asString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertNotNull(errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertNotNull(errorResponse.path());

        assertEquals(HttpStatus.CONFLICT.value(), errorResponse.statusCode());
        assertEquals(BASE_PATH, errorResponse.path());
    }

    @Test
    @DisplayName("Given an invalid reservation When creating a reservation Then return validation error")
    void givenInvalidReservation_WhenCreateReservation_ThenReturnValidationError(){
        Reservation reservation = new Reservation(
                "", "",
                null, null, -1
        );

        String content = given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().body().asString();

        ValidationErrorResponse errorResponse = objectMapper.readValue(content, ValidationErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertNotNull(errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertNotNull(errorResponse.path());
        assertNotNull(errorResponse.details());
        assertEquals(5, errorResponse.details().size());

        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.statusCode());
        assertEquals(BASE_PATH,  errorResponse.path());
    }

    @Test()
    @DisplayName("Given an existing reservation ID When finding a reservation Then return the reservation")
    void givenExistingReservationId_WhenFindReservationById_ThenReturnReservation(){
        Reservation reservation = new Reservation(
                "Gabriela", "Gabriela@email.com",
                LocalDateTime.of(2026, 7, 13, 10, 0),
                LocalDateTime.of(2026, 7, 15, 10, 0),
                2
        );
        String postContent = given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(postContent, Reservation.class);

        String getContent = given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                    .get("/{id}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().body().asString();

        Reservation foundReservation = objectMapper.readValue(getContent, Reservation.class);

        assertNotNull(foundReservation);
        assertNotNull(foundReservation.getId());
        assertNotNull(foundReservation.getCustomerName());
        assertNotNull(foundReservation.getCustomerEmail());
        assertNotNull(foundReservation.getStartAt());
        assertNotNull(foundReservation.getEndAt());
        assertNotNull(foundReservation.getStatus());
        assertNotNull(foundReservation.getPeople());
        assertNotNull(foundReservation.getCreatedAt());
        assertNotNull(foundReservation.getUpdatedAt());

        assertEquals(reservation.getCustomerName(), foundReservation.getCustomerName());
        assertEquals(reservation.getCustomerEmail(), foundReservation.getCustomerEmail());
        assertEquals(reservation.getStartAt(), foundReservation.getStartAt());
        assertEquals(reservation.getEndAt(), foundReservation.getEndAt());
        assertEquals(reservation.getStatus(), foundReservation.getStatus());
        assertEquals(reservation.getPeople(), foundReservation.getPeople());
    }

    @Test()
    @DisplayName("Given a non-existing reservation ID When finding a reservation Then return not found error")
    void givenNonExistingReservationId_WhenFindReservationById_ThenReturnNotFoundError(){
        UUID id = UUID.randomUUID();

        String content = given().spec(specification)
                .pathParam("id", id)
                .when()
                    .get("/{id}")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().body().asString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertNotNull(errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertNotNull(errorResponse.path());

        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.statusCode());
        assertEquals(BASE_PATH + "/" + id,  errorResponse.path());
    }

    @Test()
    @DisplayName("Given an existing reservation ID When confirming the reservation Then return a confirmed reservation")
    void givenExistingReservationId_WhenConfirmReservation_ThenReturnConfirmedReservation(){
        Reservation reservation = new Reservation(
                "Juliano", "juliano@email.com",
                LocalDateTime.of(2026, 7, 16, 10, 0),
                LocalDateTime.of(2026, 7, 18, 10, 0),
                6
        );

        String postContent = given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(postContent, Reservation.class);

        String patchContent = given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                    .patch("/{id}/confirm")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().body().asString();

        Reservation confirmedReservation = objectMapper.readValue(patchContent, Reservation.class);

        assertNotNull(confirmedReservation);
        assertNotNull(confirmedReservation.getId());
        assertNotNull(confirmedReservation.getCustomerName());
        assertNotNull(confirmedReservation.getCustomerEmail());
        assertNotNull(confirmedReservation.getStartAt());
        assertNotNull(confirmedReservation.getEndAt());
        assertNotNull(confirmedReservation.getStatus());
        assertNotNull(confirmedReservation.getPeople());
        assertNotNull(confirmedReservation.getCreatedAt());
        assertNotNull(confirmedReservation.getUpdatedAt());

        assertEquals(reservation.getCustomerName(), confirmedReservation.getCustomerName());
        assertEquals(reservation.getCustomerEmail(), confirmedReservation.getCustomerEmail());
        assertEquals(reservation.getStartAt(), confirmedReservation.getStartAt());
        assertEquals(reservation.getEndAt(), confirmedReservation.getEndAt());
        assertEquals(ReservationStatus.CONFIRMED, confirmedReservation.getStatus());
        assertEquals(reservation.getPeople(), confirmedReservation.getPeople());
    }

    @Test()
    @DisplayName("Given an existing reservation ID When cancelling the reservation Then return a cancelled reservation")
    void givenExistingReservationId_WhenCancelReservation_ThenReturnCancelledReservation(){
        Reservation reservation = new Reservation(
                "Yasmin", "yasmin@email.com",
                LocalDateTime.of(2026, 8, 16, 10, 0),
                LocalDateTime.of(2026, 8, 18, 10, 0),
                4
        );

        String postContent = given().spec(specification)
                .contentType(CONTENT_TYPE)
                .body(reservation)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(postContent, Reservation.class);

        String patchContent = given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                    .patch("/{id}/cancel")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().body().asString();

        Reservation cancelledReservation = objectMapper.readValue(patchContent, Reservation.class);

        assertNotNull(cancelledReservation);
        assertNotNull(cancelledReservation.getId());
        assertNotNull(cancelledReservation.getCustomerName());
        assertNotNull(cancelledReservation.getCustomerEmail());
        assertNotNull(cancelledReservation.getStartAt());
        assertNotNull(cancelledReservation.getEndAt());
        assertNotNull(cancelledReservation.getStatus());
        assertNotNull(cancelledReservation.getPeople());
        assertNotNull(cancelledReservation.getCreatedAt());
        assertNotNull(cancelledReservation.getUpdatedAt());

        assertEquals(reservation.getCustomerName(), cancelledReservation.getCustomerName());
        assertEquals(reservation.getCustomerEmail(), cancelledReservation.getCustomerEmail());
        assertEquals(reservation.getStartAt(), cancelledReservation.getStartAt());
        assertEquals(reservation.getEndAt(), cancelledReservation.getEndAt());
        assertEquals(ReservationStatus.CANCELLED, cancelledReservation.getStatus());
        assertEquals(reservation.getPeople(), cancelledReservation.getPeople());
    }

    @Test()
    @DisplayName("Given a cancelled reservation When completing the reservation Then return unprocessable content error")
    void givenCancelledReservation_WhenCompleteReservation_ThenReturnUnprocessableContentError(){
        Reservation reservation = new Reservation(
                "Juliano", "juliano@email.com",
                LocalDateTime.of(2026, 9, 16, 10, 0),
                LocalDateTime.of(2026, 9, 18, 10, 0),
                15
        );

        String postContent = given().spec(specification)
                    .contentType(CONTENT_TYPE)
                    .body(reservation)
                .when()
                    .post()
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(postContent, Reservation.class);

        given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                    .patch("/{id}/cancel")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().body().asString();

        String patchContent = given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                    .patch("/{id}/complete")
                .then()
                    .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value())
                    .extract().body().asString();

        ErrorResponse errorResponse = objectMapper.readValue(patchContent, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertNotNull(errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertNotNull(errorResponse.path());

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), errorResponse.statusCode());
        assertEquals(BASE_PATH + "/" + createdReservation.getId() + "/complete",  errorResponse.path());
    }

    @Test()
    @DisplayName("Given a cancelled reservation When confirming the reservation Then return unprocessable content error")
    void givenCancelledReservation_WhenConfirmReservation_ThenReturnUnprocessableContentError(){
        Reservation reservation = new Reservation(
                "João", "joao@email.com",
                LocalDateTime.of(2026, 11, 16, 10, 0),
                LocalDateTime.of(2026, 11, 18, 10, 0),
                15
        );

        String postContent = given().spec(specification)
                .contentType(CONTENT_TYPE)
                .body(reservation)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(postContent, Reservation.class);

        given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                .patch("/{id}/cancel")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        String patchContent = given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                .patch("/{id}/confirm")
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .extract().body().asString();

        ErrorResponse errorResponse = objectMapper.readValue(patchContent, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertNotNull(errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertNotNull(errorResponse.path());

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), errorResponse.statusCode());
        assertEquals(BASE_PATH + "/" + createdReservation.getId() + "/confirm",  errorResponse.path());
    }

    @Test()
    @DisplayName("Given a completed reservation When cancelling the reservation Then return unprocessable content error")
    void givenCompletedReservation_WhenCancelReservation_ThenReturnUnprocessableContentError(){
        Reservation reservation = new Reservation(
                "Elias", "elias@email.com",
                LocalDateTime.of(2026, 12, 16, 10, 0),
                LocalDateTime.of(2026, 12, 18, 10, 0),
                15
        );

        String postContent = given().spec(specification)
                .contentType(CONTENT_TYPE)
                .body(reservation)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().body().asString();

        Reservation createdReservation = objectMapper.readValue(postContent, Reservation.class);

        given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                .patch("/{id}/complete")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().asString();

        String patchContent = given().spec(specification)
                .pathParam("id", createdReservation.getId())
                .when()
                .patch("/{id}/cancel")
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .extract().body().asString();

        ErrorResponse errorResponse = objectMapper.readValue(patchContent, ErrorResponse.class);

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.timestamp());
        assertNotNull(errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertNotNull(errorResponse.message());
        assertNotNull(errorResponse.path());

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), errorResponse.statusCode());
        assertEquals(BASE_PATH + "/" + createdReservation.getId() + "/cancel",  errorResponse.path());
    }
}
