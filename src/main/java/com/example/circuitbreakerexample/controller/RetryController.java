package com.example.circuitbreakerexample.controller;

import com.example.circuitbreakerexample.model.Response;
import com.example.circuitbreakerexample.service.ServiceProcess;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/api/retry")
@AllArgsConstructor
public class RetryController {

    private final ServiceProcess serviceProcess;

    @GetMapping(
            value = "/delay/{delay}/timeout/{timeout}"
    )
    public Mono<ResponseEntity<Response<Boolean>>> retryTest(@PathVariable long delay, @PathVariable long timeout) {
        return this.serviceProcess.generateRevert(delay, timeout)
                .flatMap(result -> Mono.just(toResponse(result, true)));
    }

    private ResponseEntity<Response<Boolean>> toResponse(HttpStatus httpStatus, Boolean result) {
        Response<Boolean> response = Response.<Boolean>builder()
                .code(httpStatus.value())
                .status(httpStatus.getReasonPhrase())
                .data(result)
                .build();
        return new ResponseEntity<Response<Boolean>>(response, httpStatus);
    }


}
