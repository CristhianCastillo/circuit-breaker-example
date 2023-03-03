package com.example.circuitbreakerexample.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class ServiceProcess {


    public Mono<HttpStatus> generateRevert(long delayTime, long timeOut) {
        return this.callFinacle(delayTime, false)
                .doOnSubscribe(subscription -> log.info("Calling Revert Process..."))
                .timeout(Duration.ofSeconds(timeOut))
                .flatMap(response -> Mono.just(HttpStatus.OK))
                .onErrorResume(Exception.class, error -> {
                    log.error("Error on Revert Call: {}", error.getMessage());
                    this.callRetries().subscribe();
                    return Mono.just(HttpStatus.ACCEPTED);
                });
    }

    private Mono<String> callFinacle(long delayTime, boolean isRetry) {
        return Mono.just("Finacle Processing Content")
                .delayElement(Duration.ofSeconds(delayTime))
                .flatMap(process -> {
                    if (isRetry && getRandomValue() == 3) {
                        return Mono.just("200");
                    } else if (isRetry) {
                        return Mono.error(new Exception("Finacle Response: Internal Server Error!"));
                    }
                    return Mono.just("200");
                })
                .doOnNext(log::info)
                .then(Mono.just("Finacle Response: OK!"));
    }

    private Mono<Void> callRetries() {
        log.info("Not Success yet! Retried 1");
        return callFinacle(2, true)
                .doOnSubscribe(subscription -> {
                    log.info("Calling Retries Process...");
                })
                .doOnSuccess(result -> {
                    log.info(result);
                    log.info("Call CallBack End-Point");
                })
                .then()
                .retryWhen(Retry.fixedDelay(4, Duration.ofSeconds(3))
                        .doAfterRetry(retrySignal -> {
                            log.info("Not Success yet! Retried " + (retrySignal.totalRetries() + 2));
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Max Attempts Possible: {}, Attempts done: {}",
                                    retryBackoffSpec.maxAttempts, retrySignal.totalRetries());
                            throw new RuntimeException("Finacle Service failed to process after max retries");
                        })
                )
                .doOnError(error -> log.error(error.getMessage()))
                .then();
    }

    private static int getRandomValue() {
        int min = 0;
        int max = 5;
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }
}
