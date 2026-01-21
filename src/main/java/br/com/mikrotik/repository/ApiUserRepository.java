package br.com.mikrotik.repository;

import br.com.mikrotik.model.ApiUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {
    Optional<ApiUser> findByUsername(String username);
    Optional<ApiUser> findByEmail(String email);
}
