package br.com.mikrotik.service;

import br.com.mikrotik.dto.BankAccountDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.BankAccount;
import br.com.mikrotik.repository.BankAccountRepository;
import br.com.mikrotik.util.CompanyContextHolder;
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
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    /**
     * Criar nova conta bancária
     */
    @Transactional
    public BankAccountDTO create(BankAccountDTO dto) {
        log.info("Criando nova conta bancária: {}", dto.getName());

        Long companyId = CompanyContextHolder.getCompanyId();
        dto.setCompanyId(companyId);

        // Validar se já existe conta com mesmo número
        if (dto.getAccountNumber() != null && dto.getBankCode() != null) {
            // A constraint UNIQUE no banco já impede duplicação
        }

        // Definir saldo atual igual ao inicial
        if (dto.getCurrentBalance() == null && dto.getInitialBalance() != null) {
            dto.setCurrentBalance(dto.getInitialBalance());
        }

        BankAccount account = dto.toEntity();
        account = bankAccountRepository.save(account);

        log.info("Conta bancária criada: ID={}, Nome={}", account.getId(), account.getName());
        return BankAccountDTO.fromEntity(account);
    }

    /**
     * Buscar conta por ID
     */
    @Transactional(readOnly = true)
    public BankAccountDTO findById(Long id) {
        Long companyId = CompanyContextHolder.getCompanyId();
        BankAccount account = bankAccountRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada: " + id));
        return BankAccountDTO.fromEntity(account);
    }

    /**
     * Listar todas as contas (paginado)
     */
    @Transactional(readOnly = true)
    public Page<BankAccountDTO> findAll(Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return bankAccountRepository.findByCompanyId(companyId, pageable)
                .map(BankAccountDTO::fromEntity);
    }

    /**
     * Listar todas as contas (lista)
     */
    @Transactional(readOnly = true)
    public List<BankAccountDTO> findAll() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return bankAccountRepository.findByCompanyId(companyId).stream()
                .map(BankAccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Listar contas ativas
     */
    @Transactional(readOnly = true)
    public Page<BankAccountDTO> findActive(Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return bankAccountRepository.findByCompanyIdAndActive(companyId, true, pageable)
                .map(BankAccountDTO::fromEntity);
    }

    /**
     * Listar por tipo de conta
     */
    @Transactional(readOnly = true)
    public Page<BankAccountDTO> findByType(BankAccount.AccountType accountType, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return bankAccountRepository.findByCompanyIdAndAccountType(companyId, accountType, pageable)
                .map(BankAccountDTO::fromEntity);
    }

    /**
     * Atualizar conta
     */
    @Transactional
    public BankAccountDTO update(Long id, BankAccountDTO dto) {
        log.info("Atualizando conta bancária: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();

        BankAccount account = bankAccountRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada: " + id));

        // Atualizar campos
        if (dto.getName() != null) {
            account.setName(dto.getName());
        }
        if (dto.getAccountType() != null) {
            account.setAccountType(dto.getAccountType());
        }
        if (dto.getBankCode() != null) {
            account.setBankCode(dto.getBankCode());
        }
        if (dto.getAgency() != null) {
            account.setAgency(dto.getAgency());
        }
        if (dto.getAccountNumber() != null) {
            account.setAccountNumber(dto.getAccountNumber());
        }
        if (dto.getActive() != null) {
            account.setActive(dto.getActive());
        }
        if (dto.getNotes() != null) {
            account.setNotes(dto.getNotes());
        }

        // NÃO permitir alterar saldo atual diretamente - usar lançamentos financeiros
        if (dto.getCurrentBalance() != null && !dto.getCurrentBalance().equals(account.getCurrentBalance())) {
            log.warn("⚠️  Tentativa de alterar saldo atual diretamente ignorada. Use lançamentos financeiros.");
        }

        account = bankAccountRepository.save(account);
        log.info("Conta bancária atualizada: ID={}", account.getId());

        return BankAccountDTO.fromEntity(account);
    }

    /**
     * Deletar conta (soft delete - apenas inativar)
     */
    @Transactional
    public void delete(Long id) {
        log.info("Inativando conta bancária: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();

        BankAccount account = bankAccountRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada: " + id));

        // Soft delete - apenas inativar
        account.setActive(false);
        bankAccountRepository.save(account);

        log.info("Conta bancária inativada: ID={}", id);
    }

    /**
     * Contar contas
     */
    @Transactional(readOnly = true)
    public long count() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return bankAccountRepository.countByCompanyId(companyId);
    }
}

