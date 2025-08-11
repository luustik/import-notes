package dev.newsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OldClientNotesRequest(

        @NotBlank
        String agency,

        @NotNull
        LocalDate dateFrom,

        @NotNull
        LocalDate dateTo,

        @NotBlank
        String clientGuid
) {
}
