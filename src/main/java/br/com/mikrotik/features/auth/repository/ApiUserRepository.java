package br.com.mikrotik.features.auth.repository;

import br.com.mikrotik.features.auth.model.ApiUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {

    // Buscar por username (qualquer company)
    Optional<ApiUser> findByUsername(String username);


    Optional<ApiUser> findByEmail(String email);

    // Novos métodos com suporte multi-tenant
    Optional<ApiUser> findByUsernameAndCompanyId(String username, Long companyId);

    List<ApiUser> findByCompanyId(Long companyId);

    Page<ApiUser> findByCompanyId(Long companyId, Pageable pageable);

    Page<ApiUser> findByCompanyIdAndActive(Long companyId, Boolean active, Pageable pageable);

    boolean existsByUsernameAndCompanyId(String username, Long companyId);
}
