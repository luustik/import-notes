package dev.newsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static dev.newsystem.util.Constants.DATE_TIME_PATTERN;

public record OldClientResponse(

        String agency,

        String guid,

        String firstName,

        String lastName,

        String status,

        LocalDate dob,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime createdDateTime

) {
}
