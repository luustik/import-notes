package dev.newsystem.repository;

import dev.newsystem.entity.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {

    List<CompanyUser> findByLoginIn(Set<String> logins);
}
