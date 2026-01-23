package br.com.mikrotik.repository;

import br.com.mikrotik.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Buscar por empresa (multi-tenant)
    Page<Customer> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por empresa e status
    Page<Customer> findByCompanyIdAndStatus(Long companyId, Customer.CustomerStatus status, Pageable pageable);

    // Buscar por empresa e nome (like)
    Page<Customer> findByCompanyIdAndNameContainingIgnoreCase(Long companyId, String name, Pageable pageable);

    // Buscar por documento e empresa
    Optional<Customer> findByDocumentAndCompanyId(String document, Long companyId);

    // Verificar se documento já existe na empresa
    boolean existsByDocumentAndCompanyId(String document, Long companyId);

    // Buscar por email e empresa
    Optional<Customer> findByEmailAndCompanyId(String email, Long companyId);

    // Buscar por tipo de cliente
    Page<Customer> findByCompanyIdAndType(Long companyId, Customer.CustomerType type, Pageable pageable);

    // Contar clientes por status
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.companyId = :companyId AND c.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") Customer.CustomerStatus status);

    // Buscar clientes ativos
    List<Customer> findByCompanyIdAndStatus(Long companyId, Customer.CustomerStatus status);

    // Buscar por ID e empresa (segurança multi-tenant)
    Optional<Customer> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar clientes com endereços na cidade
    @Query("SELECT DISTINCT c FROM Customer c " +
           "JOIN c.addresses a " +
           "WHERE c.companyId = :companyId AND LOWER(a.city) = LOWER(:city)")
    Page<Customer> findByCompanyIdAndCity(@Param("companyId") Long companyId,
                                          @Param("city") String city,
                                          Pageable pageable);

    // Buscar clientes com múltiplos filtros
    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId " +
           "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:type IS NULL OR c.type = :type)")
    Page<Customer> findByFilters(@Param("companyId") Long companyId,
                                  @Param("name") String name,
                                  @Param("status") Customer.CustomerStatus status,
                                  @Param("type") Customer.CustomerType type,
                                  Pageable pageable);
}
