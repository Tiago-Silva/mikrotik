package br.com.mikrotik.features.financial.service;

import br.com.mikrotik.features.financial.dto.ChartOfAccountsDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.features.financial.model.ChartOfAccounts;
import br.com.mikrotik.features.financial.repository.ChartOfAccountsRepository;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartOfAccountsService {

    private final ChartOfAccountsRepository chartOfAccountsRepository;

    /**
     * Criar nova conta do plano de contas
     */
    @Transactional
    public ChartOfAccountsDTO create(ChartOfAccountsDTO dto) {
        log.info("Criando nova conta do plano de contas: {} ({})", dto.getName(), dto.getCode());

        Long companyId = CompanyContextHolder.getCompanyId();
        dto.setCompanyId(companyId);

        ChartOfAccounts account = dto.toEntity();
        account = chartOfAccountsRepository.save(account);

        log.info("Conta criada: ID={}, Código={}, Nome={}", account.getId(), account.getCode(), account.getName());
        return ChartOfAccountsDTO.fromEntity(account);
    }

    /**
     * Buscar conta por ID
     */
    @Transactional(readOnly = true)
    public ChartOfAccountsDTO findById(Long id) {
        Long companyId = CompanyContextHolder.getCompanyId();
        ChartOfAccounts account = chartOfAccountsRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta do plano de contas não encontrada: " + id));
        return ChartOfAccountsDTO.fromEntity(account);
    }

    /**
     * Buscar conta por código
     */
    @Transactional(readOnly = true)
    public ChartOfAccountsDTO findByCode(String code) {
        Long companyId = CompanyContextHolder.getCompanyId();
        ChartOfAccounts account = chartOfAccountsRepository.findByCodeAndCompanyId(code, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com código: " + code));
        return ChartOfAccountsDTO.fromEntity(account);
    }

    /**
     * Listar todas as contas (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ChartOfAccountsDTO> findAll(Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findByCompanyId(companyId, pageable)
                .map(ChartOfAccountsDTO::fromEntity);
    }

    /**
     * Listar todas as contas (lista)
     */
    @Transactional(readOnly = true)
    public List<ChartOfAccountsDTO> findAll() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findByCompanyId(companyId).stream()
                .map(ChartOfAccountsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Listar contas ativas
     */
    @Transactional(readOnly = true)
    public List<ChartOfAccountsDTO> findActive() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findByCompanyIdAndActive(companyId, true).stream()
                .map(ChartOfAccountsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Listar por tipo de conta
     */
    @Transactional(readOnly = true)
    public Page<ChartOfAccountsDTO> findByType(ChartOfAccounts.AccountType accountType, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findByCompanyIdAndAccountType(companyId, accountType, pageable)
                .map(ChartOfAccountsDTO::fromEntity);
    }

    /**
     * Listar por categoria
     */
    @Transactional(readOnly = true)
    public Page<ChartOfAccountsDTO> findByCategory(ChartOfAccounts.Category category, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findByCompanyIdAndCategory(companyId, category, pageable)
                .map(ChartOfAccountsDTO::fromEntity);
    }

    /**
     * Listar contas pai (sem parent)
     */
    @Transactional(readOnly = true)
    public List<ChartOfAccountsDTO> findParentAccounts() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findParentAccounts(companyId).stream()
                .map(ChartOfAccountsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Listar contas filhas
     */
    @Transactional(readOnly = true)
    public List<ChartOfAccountsDTO> findChildAccounts(Long parentId) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.findByCompanyIdAndParentId(companyId, parentId).stream()
                .map(ChartOfAccountsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Atualizar conta
     */
    @Transactional
    public ChartOfAccountsDTO update(Long id, ChartOfAccountsDTO dto) {
        log.info("Atualizando conta do plano de contas: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();

        ChartOfAccounts account = chartOfAccountsRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta do plano de contas não encontrada: " + id));

        // Atualizar campos
        if (dto.getCode() != null) {
            account.setCode(dto.getCode());
        }
        if (dto.getName() != null) {
            account.setName(dto.getName());
        }
        if (dto.getAccountType() != null) {
            account.setAccountType(dto.getAccountType());
        }
        if (dto.getCategory() != null) {
            account.setCategory(dto.getCategory());
        }
        if (dto.getParentId() != null) {
            account.setParentId(dto.getParentId());
        }
        if (dto.getActive() != null) {
            account.setActive(dto.getActive());
        }

        account = chartOfAccountsRepository.save(account);
        log.info("Conta atualizada: ID={}, Código={}", account.getId(), account.getCode());

        return ChartOfAccountsDTO.fromEntity(account);
    }

    /**
     * Deletar conta (soft delete - apenas inativar)
     */
    @Transactional
    public void delete(Long id) {
        log.info("Inativando conta do plano de contas: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();

        ChartOfAccounts account = chartOfAccountsRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta do plano de contas não encontrada: " + id));

        // Soft delete - apenas inativar
        account.setActive(false);
        chartOfAccountsRepository.save(account);

        log.info("Conta inativada: ID={}", id);
    }

    /**
     * Contar contas
     */
    @Transactional(readOnly = true)
    public long count() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return chartOfAccountsRepository.countByCompanyId(companyId);
    }
}

