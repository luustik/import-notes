package dev.newsystem.service.impl;

import dev.newsystem.dto.request.OldClientNotesRequest;
import dev.newsystem.dto.response.OldClientResponse;
import dev.newsystem.dto.response.OldNoteResponse;
import dev.newsystem.entity.CompanyUser;
import dev.newsystem.entity.PatientNote;
import dev.newsystem.entity.PatientProfile;
import dev.newsystem.repository.CompanyUserRepository;
import dev.newsystem.repository.PatientNoteRepository;
import dev.newsystem.repository.PatientProfileRepository;
import dev.newsystem.service.FullImportService;
import dev.newsystem.service.OldSystemClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.newsystem.util.Constants.NEW_STATUS_ACTIVE;
import static dev.newsystem.util.Constants.OLD_STATUS_ACTIVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullImportServiceImpl implements FullImportService {

    private final OldSystemClient oldSystemClient;
    private final PatientProfileRepository patientProfileRepository;
    private final PatientNoteRepository patientNoteRepository;
    private final CompanyUserRepository companyUserRepository;

    @Override
    @Transactional
    public void performFullImport() {
        log.info("=== НАЧАЛО ПОЛНОГО ИМПОРТА ДАННЫХ ===");
        String importPatientsInfo = importPatients();
        String importNotesInfo = importNotes();
        log.info(importPatientsInfo);
        log.info(importNotesInfo);
        log.info("=== ПОЛНЫЙ ИМПОРТ ДАННЫХ ЗАВЕРШЕН ===");
    }

    private String importPatients() {
        log.info("--- Начало импорта пациентов ---");
        try {
            List<OldClientResponse> activeClients = oldSystemClient.getClients().stream()
                    .filter(c -> OLD_STATUS_ACTIVE.equalsIgnoreCase(c.status()))
                    .toList();

            if (activeClients.isEmpty()) {
                String message = "Активные клиенты в старой системе не найдены";
                log.warn(message);
                return message;
            }
            log.info("Загружено активных клиентов: {}", activeClients.size());

            List<PatientProfile> existingPatients = patientProfileRepository.findAll();
            log.info("Загружено существующих пациентов: {}", existingPatients.size());

            Map<String, PatientProfile> patientsByGuid = new HashMap<>();
            Map<String, PatientProfile> patientsByFid = new HashMap<>();
            for (PatientProfile patient : existingPatients) {
                patient.getOldClientGuids().forEach(guid -> patientsByGuid.put(guid, patient));
                patientsByFid.put(createIdentityKey(patient.getFirstName(), patient.getLastName(), patient.getDob()), patient);
            }

            int created = 0;
            int updated = 0;
            int ignored = 0;
            Set<PatientProfile> patientsToSave = new HashSet<>();

            for (OldClientResponse client : activeClients) {
                String clientGuid = client.guid();
                String clientFid = createIdentityKey(client.firstName(), client.lastName(), client.dob());
                LocalDateTime clientCreationTime = client.createdDateTime();

                PatientProfile patientForGuid = patientsByGuid.get(clientGuid);
                if (patientForGuid != null) {
                    if (clientCreationTime.isAfter(patientForGuid.getCreatedDateTime())) {
                        updatePatientData(patientForGuid, client);
                        patientsToSave.add(patientForGuid);
                        updated++;
                    } else {
                        ignored++;
                    }
                    continue;
                }

                PatientProfile patientForFid = patientsByFid.get(clientFid);
                if (patientForFid != null) {
                    updated++;
                    if (clientCreationTime.isAfter(patientForFid.getCreatedDateTime())) {
                        updatePatientData(patientForFid, client);
                    }
                    patientForFid.getOldClientGuids().add(clientGuid);
                    patientsToSave.add(patientForFid);
                    continue;
                }

                PatientProfile newPatient = new PatientProfile();
                newPatient.setStatusId(NEW_STATUS_ACTIVE);
                updatePatientData(newPatient, client);
                newPatient.getOldClientGuids().add(clientGuid);
                patientsToSave.add(newPatient);
                created++;
            }

            if (!patientsToSave.isEmpty()) {
                patientProfileRepository.saveAll(patientsToSave);
            }

            return String.format(
                    "--- Импорт пациентов завершен ---%nСоздано новых пациентов: %d%nОбновлено существующих: %d%nПроигнорировано: %d",
                    created, updated, ignored
            );

        } catch (Exception e) {
            log.error("Ошибка при импорте пациентов: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при импорте пациентов", e);
        }
    }

    private void updatePatientData(PatientProfile patient, OldClientResponse client) {
        patient.setFirstName(client.firstName());
        patient.setLastName(client.lastName());
        patient.setDob(client.dob());
        patient.setCreatedDateTime(client.createdDateTime());
    }

    private String createIdentityKey(String firstName, String lastName, LocalDate dob) {
        return String.format("%s_%s_%s",
                firstName.toLowerCase(),
                lastName.toLowerCase(),
                dob.toString()
        );
    }

    private String importNotes() {
        log.info("--- Начало импорта заметок ---");
        try {
            List<PatientProfile> activePatients = patientProfileRepository.findByStatusIdIn(
                    List.of((short) 200, (short) 210, (short) 230)
            );
            if (activePatients.isEmpty()) {
                return "Активные пациенты в новой системе не найдены. Импорт заметок не требуется.";
            }
            log.info("Найдено {} активных пациентов для импорта заметок.", activePatients.size());

            Map<String, PatientProfile> guidToPatientMap = new HashMap<>();
            activePatients.forEach(p -> p.getOldClientGuids().forEach(guid -> guidToPatientMap.put(guid, p)));

            List<OldNoteResponse> oldNotes = fetchAllNotesForGuids(guidToPatientMap.keySet());
            if (oldNotes.isEmpty()) {
                return "Не найдено заметок в старой системе для активных пациентов.";
            }
            log.info("Получено {} заметок из старой системы.", oldNotes.size());

            Map<String, CompanyUser> userLoginMap = prepareUsers(oldNotes);
            Set<String> oldNoteGuids = oldNotes.stream().map(OldNoteResponse::guid).collect(Collectors.toSet());
            Map<String, PatientNote> existingNotesMap = patientNoteRepository.findByOldNoteGuidIn(oldNoteGuids)
                    .stream().collect(Collectors.toMap(PatientNote::getOldNoteGuid, Function.identity()));
            log.info("Найдено {} существующих заметок в новой системе.", existingNotesMap.size());

            return processAndSaveNotes(oldNotes, guidToPatientMap, userLoginMap, existingNotesMap);

        } catch (Exception e) {
            log.error("Критическая ошибка при импорте заметок: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при импорте заметок", e);
        }
    }

    private List<OldNoteResponse> fetchAllNotesForGuids(Set<String> clientGuids) {
        if (clientGuids.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> guidToAgencyMap = oldSystemClient.getClients().stream()
                .filter(c -> clientGuids.contains(c.guid()))
                .collect(Collectors.toMap(OldClientResponse::guid, OldClientResponse::agency, (a1, a2) -> a1));

        List<OldNoteResponse> allNotes = new ArrayList<>();
        LocalDate dateFrom = LocalDate.of(1970, 1, 1);
        LocalDate dateTo = LocalDate.now();

        for (String guid : clientGuids) {
            String agency = guidToAgencyMap.get(guid);
            if (agency == null) {
                log.warn("Не найден agency для клиента с GUID: {}. Заметки для него не будут загружены.", guid);
                continue;
            }
            try {
                allNotes.addAll(oldSystemClient.getNotes(new OldClientNotesRequest(agency, dateFrom, dateTo, guid)));
            } catch (Exception e) {
                log.error("Ошибка получения заметок для клиента {}: {}", guid, e.getMessage());
            }
        }
        return allNotes;
    }

    private Map<String, CompanyUser> prepareUsers(List<OldNoteResponse> oldNotes) {
        Set<String> logins = oldNotes.stream().map(OldNoteResponse::loggedUser).collect(Collectors.toSet());
        Map<String, CompanyUser> userMap = companyUserRepository.findByLoginIn(logins).stream()
                .collect(Collectors.toMap(CompanyUser::getLogin, Function.identity()));

        List<CompanyUser> newUsers = logins.stream()
                .filter(login -> !userMap.containsKey(login))
                .map(login -> {
                    CompanyUser newUser = new CompanyUser();
                    newUser.setLogin(login);
                    return newUser;
                })
                .toList();

        if (!newUsers.isEmpty()) {
            companyUserRepository.saveAll(newUsers).forEach(user -> userMap.put(user.getLogin(), user));
            log.info("Создано {} новых пользователей.", newUsers.size());
        }
        return userMap;
    }

    private String processAndSaveNotes(List<OldNoteResponse> oldNotes, Map<String, PatientProfile> guidToPatientMap,
                                       Map<String, CompanyUser> userLoginMap, Map<String, PatientNote> existingNotesMap) {
        int created = 0;
        int updated = 0;
        int ignored = 0;
        List<PatientNote> notesToSave = new ArrayList<>();

        for (OldNoteResponse oldNote : oldNotes) {
            PatientProfile patient = guidToPatientMap.get(oldNote.clientGuid());
            CompanyUser user = userLoginMap.get(oldNote.loggedUser());

            if (patient == null || user == null) {
                log.warn("Пропуск заметки с GUID {}: не найден пациент или пользователь.", oldNote.guid());
                continue;
            }

            PatientNote existingNote = existingNotesMap.get(oldNote.guid());

            if (existingNote == null) {
                PatientNote newNote = new PatientNote();
                newNote.setOldNoteGuid(oldNote.guid());
                newNote.setPatientProfile(patient);
                newNote.setNote(oldNote.comments());
                newNote.setCreatedByUser(user);
                newNote.setLastModifiedByUser(user);
                newNote.setCreatedDateTime(oldNote.createdDateTime());
                newNote.setLastModifiedDateTime(oldNote.modifiedDateTime());
                notesToSave.add(newNote);
                created++;
            } else {
                if (oldNote.modifiedDateTime().isAfter(existingNote.getLastModifiedDateTime())) {
                    existingNote.setNote(oldNote.comments());
                    existingNote.setLastModifiedDateTime(oldNote.modifiedDateTime());
                    existingNote.setLastModifiedByUser(user);
                    notesToSave.add(existingNote);
                    updated++;
                } else {
                    ignored++;
                }
            }
        }

        if (!notesToSave.isEmpty()) {
            patientNoteRepository.saveAll(notesToSave);
        }

        return String.format(
                "--- Импорт заметок завершен ---%nСоздано новых заметок: %d%nОбновлено существующих: %d%nПроигнорировано (не было изменений): %d",
                created, updated, ignored
        );
    }

}
