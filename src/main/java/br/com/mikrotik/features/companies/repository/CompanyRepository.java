
package br.com.mikrotik.features.companies.repository;

import br.com.mikrotik.features.companies.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Busca uma empresa pelo CNPJ
     */
    Optional<Company> findByCnpj(String cnpj);

    /**
     * Verifica se existe uma empresa com o CNPJ informado
     */
    boolean existsByCnpj(String cnpj);

    /**
     * Busca empresas ativas
     */
    Page<Company> findByActive(Boolean active, Pageable pageable);

    /**
     * Busca empresas por nome (parcial, case-insensitive)
     */
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
