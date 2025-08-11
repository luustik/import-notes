package dev.newsystem.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "patient_profile")
@Getter
@Setter
@NoArgsConstructor
public class PatientProfile {

    @Id
    @SequenceGenerator(name = "patient_profile_seq", sequenceName = "patient_profile_id_seq", allocationSize = 500)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_profile_seq")
    @Column(name = "id")
    private long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ElementCollection
    @CollectionTable(name = "patient_old_client_guids", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "old_client_guid", length = 36)
    private Set<String> oldClientGuids = new HashSet<>();

    @Column(name = "status_id", nullable = false)
    private short statusId;

    @OneToMany(
            mappedBy = "patientProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PatientNote> notes = new ArrayList<>();

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "created_date_time", nullable = false)
    private LocalDateTime createdDateTime;
}
