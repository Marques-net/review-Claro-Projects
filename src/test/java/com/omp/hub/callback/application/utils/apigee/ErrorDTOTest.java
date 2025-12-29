package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDTOTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void builder_ShouldCreateErrorDTO() {
        // Arrange & Act
        ErrorDTO error = ErrorDTO.builder()
                .httpCode(400)
                .errorCode("INVALID_REQUEST")
                .message("Bad Request")
                .link(LinkErrorDTO.builder().rel("help").href("/help").build())
                .build();

        // Assert
        assertNotNull(error);
        assertEquals(400, error.getHttpCode());
        assertEquals("INVALID_REQUEST", error.getErrorCode());
        assertEquals("Bad Request", error.getMessage());
        assertNotNull(error.getLink());
        assertEquals("help", error.getLink().getRel());
        assertEquals("/help", error.getLink().getHref());
    }

    @Test
    void getDetailedMessage_WithNullNode_ShouldReturnNull() {
        // Arrange
        ErrorDTO error = new ErrorDTO();

        // Act
        String result = error.getDetailedMessage();

        // Assert
        assertNull(result);
    }

    @Test
    void getDetailedMessage_WithTextualNode_ShouldReturnText() {
        // Arrange
        ErrorDTO error = new ErrorDTO();
        TextNode textNode = TextNode.valueOf("Simple error message");
        error.setDetailedMessageNode(textNode);

        // Act
        String result = error.getDetailedMessage();

        // Assert
        assertEquals("Simple error message", result);
    }

    @Test
    void getDetailedMessage_WithArrayNode_ShouldJoinWithNewlines() {
        // Arrange
        ErrorDTO error = new ErrorDTO();
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add("First error");
        arrayNode.add("Second error");
        arrayNode.add("Third error");
        error.setDetailedMessageNode(arrayNode);

        // Act
        String result = error.getDetailedMessage();

        // Assert
        assertEquals("First error\nSecond error\nThird error", result);
    }

    @Test
    void getDetailedMessage_WithSingleElementArray_ShouldReturnSingleElement() {
        // Arrange
        ErrorDTO error = new ErrorDTO();
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add("Only error");
        error.setDetailedMessageNode(arrayNode);

        // Act
        String result = error.getDetailedMessage();

        // Assert
        assertEquals("Only error", result);
    }

    @Test
    void getDetailedMessage_WithEmptyArray_ShouldReturnEmptyString() {
        // Arrange
        ErrorDTO error = new ErrorDTO();
        ArrayNode arrayNode = mapper.createArrayNode();
        error.setDetailedMessageNode(arrayNode);

        // Act
        String result = error.getDetailedMessage();

        // Assert
        assertEquals("", result);
    }

    @Test
    void getDetailedMessage_WithOtherNodeType_ShouldReturnToString() {
        // Arrange
        ErrorDTO error = new ErrorDTO();
        JsonNode objectNode = mapper.createObjectNode().put("key", "value");
        error.setDetailedMessageNode(objectNode);

        // Act
        String result = error.getDetailedMessage();

        // Assert
        assertEquals("{\"key\":\"value\"}", result);
    }

    @Test
    void setDetailedMessageNode_ShouldSetNode() {
        // Arrange
        ErrorDTO error = new ErrorDTO();
        TextNode textNode = TextNode.valueOf("test message");

        // Act
        error.setDetailedMessageNode(textNode);

        // Assert
        assertEquals(textNode, error.getDetailedMessageNode());
    }

    @Test
    void allArgsConstructor_ShouldCreateCompleteErrorDTO() {
        // Arrange
        LinkErrorDTO link = LinkErrorDTO.builder().rel("self").href("/error").build();
        TextNode detailedMessage = TextNode.valueOf("Detailed error");

        // Act
        ErrorDTO error = new ErrorDTO(500, "INTERNAL_ERROR", "Internal server error", detailedMessage, link);

        // Assert
        assertEquals(500, error.getHttpCode());
        assertEquals("INTERNAL_ERROR", error.getErrorCode());
        assertEquals("Internal server error", error.getMessage());
        assertEquals(detailedMessage, error.getDetailedMessageNode());
        assertEquals(link, error.getLink());
        assertEquals("Detailed error", error.getDetailedMessage());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyErrorDTO() {
        // Act
        ErrorDTO error = new ErrorDTO();

        // Assert
        assertNull(error.getHttpCode());
        assertNull(error.getErrorCode());
        assertNull(error.getMessage());
        assertNull(error.getDetailedMessageNode());
        assertNull(error.getLink());
        assertNull(error.getDetailedMessage());
    }
}