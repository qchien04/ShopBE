package com.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse2<T> {
    private Integer error;
    private String message;
    private T data;

    public static <T> ApiResponse2<T> success(T data) {
        return new ApiResponse2<>(0, "success", data);
    }

    public static <T> ApiResponse2<T> success(String message, T data) {
        return new ApiResponse2<>(0, message, data);
    }

    public static <T> ApiResponse2<T> error(String message) {
        return new ApiResponse2<>(-1, message, null);
    }

    public static <T> ApiResponse2<T> error(int code, String message) {
        return new ApiResponse2<>(code, message, null);
    }
}

