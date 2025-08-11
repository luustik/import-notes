package dev.newsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import static dev.newsystem.util.Constants.DATE_TIME_PATTERN;

public record OldNoteResponse(

        String comments,

        String guid,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime modifiedDateTime,

        String clientGuid,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime createdDateTime,

        String loggedUser
) {
}
