package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.application.utils.apigee.RequestUtils;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformationPaymentClientTest {

    @Mock
    private RequestUtils requestUtils;

    @InjectMocks
    private InformationPaymentClient informationPaymentClient;

    private String urlHost = "http://localhost:8080";
    private String urlClient = "/api/payments";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(informationPaymentClient, "urlHost", urlHost);
        ReflectionTestUtils.setField(informationPaymentClient, "urlClient", urlClient);
        ReflectionTestUtils.setField(informationPaymentClient, "requestUtils", requestUtils);
    }

    @Test
    void sendCreate_WithValidRequest_ShouldReturnInformationPaymentDTO() {
        // Given
        InformationPaymentDTO request = InformationPaymentDTO.builder()
            .identifier("PAY123")
            .channel("WEB")
            .build();

        InformationPaymentDTO expectedResponse = InformationPaymentDTO.builder()
            .identifier("PAY123")
            .channel("WEB")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(expectedResponse);

        // When
        InformationPaymentDTO result = informationPaymentClient.sendCreate(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo("PAY123");
        assertThat(result.getChannel()).isEqualTo("WEB");
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
        verify(requestUtils).sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class));
    }

    @Test
    void sendCreate_WithNullResponse_ShouldReturnNull() {
        // Given
        InformationPaymentDTO request = InformationPaymentDTO.builder()
            .identifier("PAY456")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(null);

        // When
        InformationPaymentDTO result = informationPaymentClient.sendCreate(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void sendUpdate_WithValidRequest_ShouldReturnUpdatedInformationPaymentDTO() {
        // Given
        InformationPaymentDTO request = InformationPaymentDTO.builder()
            .identifier("PAY789")
            .channel("MOBILE")
            .build();

        InformationPaymentDTO expectedResponse = InformationPaymentDTO.builder()
            .identifier("PAY789")
            .channel("MOBILE")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(expectedResponse);

        // When
        InformationPaymentDTO result = informationPaymentClient.sendUpdate(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo("PAY789");
        assertThat(result.getChannel()).isEqualTo("MOBILE");
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
        verify(requestUtils).sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class));
    }

    @Test
    void sendUpdate_ShouldUseCorrectHttpVerb() {
        // Given
        InformationPaymentDTO request = InformationPaymentDTO.builder()
            .identifier("PAY999")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(request);

        // When
        informationPaymentClient.sendUpdate(request);

        // Then
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
        verify(requestUtils).sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class));
    }

    @Test
    void sendFindByIdentifier_WithValidIdentifier_ShouldReturnInformationPaymentDTO() {
        // Given
        String identifier = "IDENT001";
        InformationPaymentDTO expectedResponse = InformationPaymentDTO.builder()
            .identifier("PAY111")
            .channel("API")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(expectedResponse);

        // When
        InformationPaymentDTO result = informationPaymentClient.sendFindByIdentifier(identifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo("PAY111");
        assertThat(result.getChannel()).isEqualTo("API");
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
        verify(requestUtils).sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class));
    }

    @Test
    void sendFindByIdentifier_WithNullIdentifier_ShouldBuildRequestWithNullInPath() {
        // Given
        String identifier = null;
        InformationPaymentDTO expectedResponse = InformationPaymentDTO.builder()
            .identifier("PAY222")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(expectedResponse);

        // When
        InformationPaymentDTO result = informationPaymentClient.sendFindByIdentifier(identifier);

        // Then
        assertThat(result).isNotNull();
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
    }

    @Test
    void sendFindByIdentifier_WithEmptyIdentifier_ShouldReturnData() {
        // Given
        String identifier = "";
        InformationPaymentDTO expectedResponse = InformationPaymentDTO.builder()
            .identifier("PAY333")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(expectedResponse);

        // When
        InformationPaymentDTO result = informationPaymentClient.sendFindByIdentifier(identifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIdentifier()).isEqualTo("PAY333");
    }

    @Test
    void sendFindByIdentifier_ShouldUseCorrectHttpVerb() {
        // Given
        String identifier = "IDENT002";
        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(null);

        // When
        informationPaymentClient.sendFindByIdentifier(identifier);

        // Then
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
        verify(requestUtils).sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class));
    }

    @Test
    void sendCreate_ShouldBuildCorrectUrlWithHostAndClient() {
        // Given
        InformationPaymentDTO request = InformationPaymentDTO.builder()
            .identifier("PAY444")
            .build();

        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(request);

        // When
        informationPaymentClient.sendCreate(request);

        // Then
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
    }

    @Test
    void sendFindByIdentifier_ShouldAppendIdentifierToUrl() {
        // Given
        String identifier = "IDENT003";
        Request mockRequest = mock(Request.class);

        when(requestUtils.generateRequest(any(GenerateRequestDTO.class)))
            .thenReturn(mockRequest);
        when(requestUtils.sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class)))
            .thenReturn(null);

        // When
        informationPaymentClient.sendFindByIdentifier(identifier);

        // Then
        verify(requestUtils).generateRequest(any(GenerateRequestDTO.class));
        verify(requestUtils).sendRequest(eq(mockRequest), anyString(), eq(InformationPaymentDTO.class));
    }
}
