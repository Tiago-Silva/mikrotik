package br.com.mikrotik.repository;

import br.com.mikrotik.model.PppoeCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PppoeCredentialRepository extends JpaRepository<PppoeCredential, Long> {

    // Buscar por empresa (multi-tenant)
    Page<PppoeCredential> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por empresa e servidor
    Page<PppoeCredential> findByCompanyIdAndMikrotikServerId(Long companyId, Long mikrotikServerId, Pageable pageable);

    // Buscar por status
    Page<PppoeCredential> findByCompanyIdAndStatus(Long companyId, PppoeCredential.CredentialStatus status, Pageable pageable);

    // Buscar por servidor e status
    Page<PppoeCredential> findByMikrotikServerIdAndStatus(Long mikrotikServerId, PppoeCredential.CredentialStatus status, Pageable pageable);

    // Buscar por username
    Optional<PppoeCredential> findByUsernameAndMikrotikServerId(String username, Long mikrotikServerId);

    // Buscar por ID e empresa (segurança multi-tenant)
    Optional<PppoeCredential> findByIdAndCompanyId(Long id, Long companyId);

    // Verificar existência por username e servidor
    boolean existsByUsernameAndMikrotikServerId(String username, Long mikrotikServerId);

    // Listar credenciais online
    List<PppoeCredential> findByCompanyIdAndStatus(Long companyId, PppoeCredential.CredentialStatus status);

    // Buscar por MAC address
    Optional<PppoeCredential> findByMacAddressAndMikrotikServerId(String macAddress, Long mikrotikServerId);

    // Contar credenciais por empresa
    long countByCompanyId(Long companyId);

    // Contar credenciais por status
    long countByCompanyIdAndStatus(Long companyId, PppoeCredential.CredentialStatus status);

    // Contar credenciais por servidor
    long countByMikrotikServerId(Long mikrotikServerId);

    // Buscar credenciais não vinculadas a contrato (paginado)
    @Query("SELECT pc FROM PppoeCredential pc WHERE pc.companyId = :companyId " +
           "AND pc.id NOT IN (SELECT c.pppoeCredentialId FROM Contract c WHERE c.pppoeCredentialId IS NOT NULL)")
    Page<PppoeCredential> findUnassignedCredentials(@Param("companyId") Long companyId, Pageable pageable);

    // Buscar com filtros múltiplos
    @Query("SELECT pc FROM PppoeCredential pc WHERE pc.companyId = :companyId " +
           "AND (:username IS NULL OR LOWER(pc.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:serverId IS NULL OR pc.mikrotikServerId = :serverId) " +
           "AND (:status IS NULL OR pc.status = :status)")
    Page<PppoeCredential> findByFilters(@Param("companyId") Long companyId,
                                        @Param("username") String username,
                                        @Param("serverId") Long serverId,
                                        @Param("status") PppoeCredential.CredentialStatus status,
                                        Pageable pageable);
}
