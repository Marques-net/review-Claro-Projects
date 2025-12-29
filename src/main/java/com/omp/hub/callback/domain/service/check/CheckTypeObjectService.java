package com.omp.hub.callback.domain.service.check;

public interface CheckTypeObjectService {

    <T> Boolean isValid(String request, Class<T> nameClass);

}
