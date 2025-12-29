package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.DataDTO;
import com.omp.hub.callback.domain.model.dto.journey.HeadersDTO;
import com.omp.hub.callback.domain.ports.client.UtilsPort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class UtilsClient implements UtilsPort {

    public String getStorePdv(HeadersDTO headers) {

        if (headers.getSalesPoint() != null && headers.getStore() != null) {
            return headers.getStore() + "_" + headers.getSalesPoint();
        } else {
            return "DEFAULT";
        }
    }

    public InformationPaymentDTO generateInfoPayment(UUID uuid, DataDTO dto, HeadersDTO headers, String identifier) {

        return InformationPaymentDTO.builder()
                .uuid(uuid)
                .identifier(identifier)
                .channel(headers.getChannel())
                .store(headers.getStore() != null ? headers.getStore() : "DEFAULT")
                .pdv(headers.getSalesPoint() != null ? headers.getSalesPoint() : "DEFAULT")
                .payments(Collections.singletonList(PaymentDTO.builder()
                        .type(PaymentTypeEnum.UNDEFINED)
                        .journey(dto)
                        .callback("{}")
                        .paymentStatus(PaymentStatusEnum.PENDING)
                        .build()))
                .paymentStatus(PaymentStatusEnum.PENDING)
                .build();
    }
}
