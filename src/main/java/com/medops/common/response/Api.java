package com.medops.common.response;

import com.medops.common.error.ErrorCodeInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Api<T> {

    private ApiResult result;

    @Valid
    private T body;

    public static <T> Api<T> OK(T data){
        return Api.<T>builder()
            .body(data)
            .result(ApiResult.OK())
            .build();
    }

    public static Api<Object> ERROR(ApiResult result){
        return Api.<Object>builder()
            .result(result)
            .build();
    }

    public static Api<Object> ERROR(ErrorCodeInterface errorCodeInterface){
        return Api.<Object>builder()
            .result(ApiResult.ERROR(errorCodeInterface))
            .build();
    }

    public static Api<Object> ERROR(ErrorCodeInterface errorCodeInterface, Throwable tw){
        return Api.<Object>builder()
            .result(ApiResult.ERROR(errorCodeInterface, tw))
            .build();
    }

    public static Api<Object> ERROR(ErrorCodeInterface errorCodeInterface, String description){
        return Api.<Object>builder()
            .result(ApiResult.ERROR(errorCodeInterface, description))
            .build();
    }
}