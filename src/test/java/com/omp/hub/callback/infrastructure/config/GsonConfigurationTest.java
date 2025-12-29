package com.omp.hub.callback.infrastructure.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;

@ExtendWith(MockitoExtension.class)
class GsonConfigurationTest {

    private GsonConfiguration gsonConfiguration;
    private Gson gson;

    @BeforeEach
    void setUp() {
        gsonConfiguration = new GsonConfiguration();
        gson = gsonConfiguration.gson();
    }

    @Test
    void should_CreateGsonBean_When_Called() {
        // When
        Gson result = gsonConfiguration.gson();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Gson.class);
    }

    @Test
    void should_SerializeLocalDateTime_When_GsonIsUsed() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        TestObject testObject = new TestObject(dateTime);

        // When
        String json = gson.toJson(testObject);

        // Then
        String expectedDateTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(json).contains(expectedDateTime);
    }

    @Test
    void should_DeserializeLocalDateTime_When_GsonIsUsed() {
        // Given
        String json = "{\"dateTime\":\"2023-12-25T10:30:45\"}";

        // When
        TestObject result = gson.fromJson(json, TestObject.class);

        // Then
        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        assertThat(result.getDateTime()).isEqualTo(expectedDateTime);
    }

    @Test
    void should_HandleNullLocalDateTime_When_Serializing() {
        // Given
        TestObject testObject = new TestObject(null);

        // When
        String json = gson.toJson(testObject);

        // Then
        // Por padrão o Gson não inclui campos null no JSON, então o JSON será {}
        assertThat(json).isEqualTo("{}");
    }

    @Test
    void should_HandleNullLocalDateTime_When_Deserializing() {
        // Given
        String json = "{\"dateTime\":null}";

        // When
        TestObject result = gson.fromJson(json, TestObject.class);

        // Then
        assertThat(result.getDateTime()).isNull();
    }

    @Test
    void should_ThrowException_When_DeserializingInvalidDateTime() {
        // Given
        String json = "{\"dateTime\":\"invalid-date\"}";

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(
            java.time.format.DateTimeParseException.class,
            () -> gson.fromJson(json, TestObject.class)
        );
    }

    @Test
    void should_SerializeAndDeserialize_MultipleLocalDateTimes() {
        // Given
        LocalDateTime dateTime1 = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime dateTime2 = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        MultiDateTestObject testObject = new MultiDateTestObject(dateTime1, dateTime2);

        // When
        String json = gson.toJson(testObject);
        MultiDateTestObject result = gson.fromJson(json, MultiDateTestObject.class);

        // Then
        assertThat(result.getStartDate()).isEqualTo(dateTime1);
        assertThat(result.getEndDate()).isEqualTo(dateTime2);
    }

    @Test
    void should_HandleSpecialCharacters_When_SerializingLocalDateTime() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2023, 2, 14, 14, 30, 0);
        TestObjectWithName testObject = new TestObjectWithName("Test/Name", dateTime);

        // When
        String json = gson.toJson(testObject);
        TestObjectWithName result = gson.fromJson(json, TestObjectWithName.class);

        // Then
        assertThat(result.getName()).isEqualTo("Test/Name");
        assertThat(result.getDateTime()).isEqualTo(dateTime);
    }

    // Helper classes for testing
    private static class TestObject {
        private LocalDateTime dateTime;

        public TestObject(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
    }

    private static class MultiDateTestObject {
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        public MultiDateTestObject(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }
    }

    private static class TestObjectWithName {
        private String name;
        private LocalDateTime dateTime;

        public TestObjectWithName(String name, LocalDateTime dateTime) {
            this.name = name;
            this.dateTime = dateTime;
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
    }
}