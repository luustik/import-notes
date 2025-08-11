package dev.newsystem.repository;

import dev.newsystem.entity.PatientNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {

    List<PatientNote> findByOldNoteGuidIn(Set<String> oldNoteGuids);
}
