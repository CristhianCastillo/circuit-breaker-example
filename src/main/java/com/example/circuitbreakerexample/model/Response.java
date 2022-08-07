package com.example.circuitbreakerexample.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private int code;
    private String status;
    private T data;
}
