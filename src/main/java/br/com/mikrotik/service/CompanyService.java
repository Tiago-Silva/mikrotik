package br.com.mikrotik.service;

import br.com.mikrotik.dto.CompanyDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.Company;
import br.com.mikrotik.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * Cria uma nova empresa
     */
    @Transactional
    public CompanyDTO create(CompanyDTO dto) {
        log.info("Criando nova empresa: {}", dto.getName());

        // Validar se CNPJ já existe
        if (companyRepository.existsByCnpj(dto.getCnpj())) {
            throw new ValidationException("CNPJ já cadastrado: " + dto.getCnpj());
        }

        Company company = new Company();
        company.setName(dto.getName());
        company.setTradeName(dto.getTradeName());
        company.setCnpj(dto.getCnpj());
        company.setEmail(dto.getEmail());
        company.setSupportPhone(dto.getSupportPhone());
        company.setActive(dto.getActive() != null ? dto.getActive() : true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        Company saved = companyRepository.save(company);
        log.info("Empresa criada com sucesso. ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    /**
     * Busca uma empresa por ID
     */
    @Transactional(readOnly = true)
    public CompanyDTO findById(Long id) {
        log.info("Buscando empresa por ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + id));

        return mapToDTO(company);
    }

    /**
     * Busca uma empresa por CNPJ
     */
    @Transactional(readOnly = true)
    public CompanyDTO findByCnpj(String cnpj) {
        log.info("Buscando empresa por CNPJ: {}", cnpj);

        Company company = companyRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. CNPJ: " + cnpj));

        return mapToDTO(company);
    }

    /**
     * Lista todas as empresas com paginação
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> findAll(Pageable pageable) {
        log.info("Listando todas as empresas. Página: {}", pageable.getPageNumber());

        return companyRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Lista empresas ativas
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> findByActive(Boolean active, Pageable pageable) {
        log.info("Listando empresas ativas: {}. Página: {}", active, pageable.getPageNumber());

        return companyRepository.findByActive(active, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Busca empresas por nome
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> findByName(String name, Pageable pageable) {
        log.info("Buscando empresas por nome: {}", name);

        return companyRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Atualiza uma empresa
     */
    @Transactional
    public CompanyDTO update(Long id, CompanyDTO dto) {
        log.info("Atualizando empresa ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + id));

        // Validar se CNPJ foi alterado e já existe
        if (!company.getCnpj().equals(dto.getCnpj())) {
            if (companyRepository.existsByCnpj(dto.getCnpj())) {
                throw new ValidationException("CNPJ já cadastrado: " + dto.getCnpj());
            }
        }

        company.setName(dto.getName());
        company.setTradeName(dto.getTradeName());
        company.setCnpj(dto.getCnpj());
        company.setEmail(dto.getEmail());
        company.setSupportPhone(dto.getSupportPhone());
        company.setActive(dto.getActive());
        company.setUpdatedAt(LocalDateTime.now());

        Company updated = companyRepository.save(company);
        log.info("Empresa atualizada com sucesso. ID: {}", updated.getId());

        return mapToDTO(updated);
    }

    /**
     * Desativa uma empresa (soft delete)
     */
    @Transactional
    public void deactivate(Long id) {
        log.info("Desativando empresa ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + id));

        company.setActive(false);
        company.setUpdatedAt(LocalDateTime.now());
        companyRepository.save(company);

        log.info("Empresa desativada com sucesso. ID: {}", id);
    }

    /**
     * Ativa uma empresa
     */
    @Transactional
    public void activate(Long id) {
        log.info("Ativando empresa ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + id));

        company.setActive(true);
        company.setUpdatedAt(LocalDateTime.now());
        companyRepository.save(company);

        log.info("Empresa ativada com sucesso. ID: {}", id);
    }

    /**
     * Deleta uma empresa (apenas se não houver dependências)
     */
    @Transactional
    public void delete(Long id) {
        log.warn("Tentando deletar empresa ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada. ID: " + id));

        // TODO: Verificar se existem dependências (servidores, clientes, etc.)
        // Por enquanto, apenas deletar

        companyRepository.delete(company);
        log.info("Empresa deletada com sucesso. ID: {}", id);
    }

    /**
     * Mapeia entidade para DTO
     */
    private CompanyDTO mapToDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .tradeName(company.getTradeName())
                .cnpj(company.getCnpj())
                .email(company.getEmail())
                .supportPhone(company.getSupportPhone())
                .active(company.getActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
