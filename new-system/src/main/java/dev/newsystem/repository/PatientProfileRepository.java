package dev.newsystem.repository;

import dev.newsystem.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    List<PatientProfile> findByStatusIdIn(Collection<Short> statuses);

}
