package br.com.mikrotik.repository;

import br.com.mikrotik.model.CashFlowCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashFlowCategoryRepository extends JpaRepository<CashFlowCategory, Long> {

    // Buscar por empresa (paginado)
    Page<CashFlowCategory> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por empresa (lista)
    List<CashFlowCategory> findByCompanyId(Long companyId);

    // Buscar por ID e empresa
    Optional<CashFlowCategory> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar por tipo de categoria
    List<CashFlowCategory> findByCompanyIdAndCategoryType(Long companyId, CashFlowCategory.CategoryType categoryType);

    // Buscar categorias ativas
    List<CashFlowCategory> findByCompanyIdAndActive(Long companyId, Boolean active);

    // Buscar por tipo e ativo
    List<CashFlowCategory> findByCompanyIdAndCategoryTypeAndActive(Long companyId, CashFlowCategory.CategoryType categoryType, Boolean active);

    // Contar categorias por empresa
    long countByCompanyId(Long companyId);
}

