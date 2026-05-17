package com.hotelBackend.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;
    private String detail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructor sin timestamp (se establece automáticamente)
    public ErrorResponse(int code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor con solo code y message
    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}

