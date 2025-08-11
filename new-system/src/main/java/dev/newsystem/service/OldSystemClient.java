package dev.newsystem.service;

import dev.newsystem.dto.request.OldClientNotesRequest;
import dev.newsystem.dto.response.OldClientResponse;
import dev.newsystem.dto.response.OldNoteResponse;

import java.util.List;

public interface OldSystemClient {

    List<OldClientResponse> getClients();

    List<OldNoteResponse> getNotes(OldClientNotesRequest oldClientNotesRequest);
}
