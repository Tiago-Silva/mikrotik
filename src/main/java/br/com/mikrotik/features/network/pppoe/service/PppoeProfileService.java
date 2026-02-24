package br.com.mikrotik.features.network.pppoe.service;

import br.com.mikrotik.features.network.pppoe.dto.MikrotikPppoeProfileDTO;
import br.com.mikrotik.features.network.pppoe.dto.PppoeProfileDTO;
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.features.sync.dto.SyncResultDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PppoeProfileService {

    private final PppoeProfileRepository repository;
    private final MikrotikServerRepository serverRepository;
    private final MikrotikApiService apiService; // API service for better performance

    @Transactional
    public PppoeProfileDTO create(PppoeProfileDTO dto) {
        MikrotikServer server = serverRepository.findById(dto.getMikrotikServerId())
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));

        // 1. SALVAR NO BANCO PRIMEIRO (flush imediato para expor erros de constraint)
        // Se o banco falhar (ex.: nome duplicado), o Mikrotik nunca é chamado — sem órfãos.
        log.info("Salvando perfil PPPoE no banco antes de enviar ao Mikrotik");
        PppoeProfile profile = new PppoeProfile();
        profile.setName(dto.getName());
        profile.setDescription(dto.getDescription());
        profile.setMaxBitrateDl(dto.getMaxBitrateDl());
        profile.setMaxBitrateUl(dto.getMaxBitrateUl());
        profile.setSessionTimeout(dto.getSessionTimeout());
        profile.setActive(dto.getActive() != null ? dto.getActive() : true);
        profile.setMikrotikServer(server);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        PppoeProfile saved = repository.saveAndFlush(profile);

        // 2. CRIAR NO MIKROTIK VIA API
        // Se o Mikrotik falhar, @Transactional reverte o banco automaticamente.
        log.info("Criando perfil PPPoE no Mikrotik via API: {}", saved.getId());
        apiService.createPppoeProfile(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                dto.getName(),
                dto.getMaxBitrateDl(),
                dto.getMaxBitrateUl(),
                dto.getSessionTimeout(),
                dto.getDescription()
        );

        log.info("Perfil PPPoE criado com sucesso: {} (Banco + Mikrotik)", saved.getId());
        return mapToDTO(saved);
    }

    public PppoeProfileDTO getById(Long id) {
        PppoeProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + id));
        return mapToDTO(profile);
    }

    public Page<PppoeProfileDTO> getByServer(Long serverId, Pageable pageable) {
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));
        return repository.findByMikrotikServer(server, pageable)
                .map(this::mapToDTO);
    }

    public Page<PppoeProfileDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public PppoeProfileDTO update(Long id, PppoeProfileDTO dto) {
        PppoeProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + id));

        // Capturar nome antigo ANTES de mutar a entidade — será usado na chamada ao Mikrotik
        final String oldName = profile.getName();
        final MikrotikServer server = profile.getMikrotikServer();

        // 1. PERSISTIR NO BANCO PRIMEIRO (flush para expor erros de constraint antes de chamar o Mikrotik)
        // Se o save falhar, o Mikrotik não é tocado — sem divergência de nomes.
        profile.setName(dto.getName());
        profile.setDescription(dto.getDescription());
        profile.setMaxBitrateDl(dto.getMaxBitrateDl());
        profile.setMaxBitrateUl(dto.getMaxBitrateUl());
        profile.setSessionTimeout(dto.getSessionTimeout());
        profile.setActive(dto.getActive());
        profile.setUpdatedAt(LocalDateTime.now());

        PppoeProfile updated = repository.saveAndFlush(profile);

        // 2. ATUALIZAR NO MIKROTIK APÓS O COMMIT
        // REGRA DE OURO: nunca segurar connection pool esperando resposta de hardware.
        final String newName        = dto.getName();
        final Long   bitrateDl      = dto.getMaxBitrateDl();
        final Long   bitrateUl      = dto.getMaxBitrateUl();
        final Integer sessionTimeout = dto.getSessionTimeout();
        final String description     = dto.getDescription();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Atualizando perfil PPPoE no Mikrotik após commit: {} → {}", oldName, newName);
                try {
                    apiService.updatePppoeProfile(
                            server.getIpAddress(),
                            server.getApiPort(),
                            server.getUsername(),
                            server.getPassword(),
                            oldName,
                            newName,
                            bitrateDl,
                            bitrateUl,
                            sessionTimeout,
                            description
                    );
                    log.info("✅ Perfil PPPoE atualizado no Mikrotik: {}", newName);
                } catch (Exception e) {
                    log.error("❌ Falha ao atualizar perfil PPPoE no Mikrotik: {} — {}", newName, e.getMessage());
                    // Banco já atualizado. Operador pode re-sincronizar manualmente.
                }
            }
        });

        log.info("Perfil PPPoE atualizado no banco: {} (Mikrotik será atualizado após commit)", updated.getId());
        return mapToDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        PppoeProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + id));

        MikrotikServer server = profile.getMikrotikServer();
        String profileName = profile.getName();

        // 1. DELETAR DO BANCO PRIMEIRO (flush para expor FK constraints)
        // Se houver usuários PPPoE vinculados, o banco rejeita e o Mikrotik não é tocado.
        log.info("Deletando perfil PPPoE do banco: {}", profileName);
        repository.delete(profile);
        repository.flush();

        // 2. DELETAR DO MIKROTIK VIA API
        // Se o Mikrotik falhar, @Transactional reverte a deleção do banco automaticamente.
        log.info("Deletando perfil PPPoE do Mikrotik via API: {}", profileName);
        apiService.deletePppoeProfile(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                profileName
        );

        log.info("Perfil PPPoE deletado com sucesso: {} (Banco + Mikrotik)", id);
    }

    @Transactional
    public SyncResultDTO syncProfilesFromMikrotik(Long serverId) {
        SyncResultDTO result = new SyncResultDTO();

        // Buscar servidor
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + serverId));

        log.info("Iniciando sincronização de profiles do servidor {} (ID: {})", server.getName(), serverId);

        try {
            // Buscar profiles do Mikrotik via API (✅ Comentários completos!)
            log.info("Usando MikrotikApiService para sincronização via porta API: {}", server.getApiPort());
            List<MikrotikPppoeProfileDTO> mikrotikProfiles = apiService.getPppoeProfilesStructured(
                    server.getIpAddress(),
                    server.getApiPort(),
                    server.getUsername(),
                    server.getPassword()
            );

            result.setTotalMikrotikUsers(mikrotikProfiles.size()); // Reutilizando campo do DTO

            for (MikrotikPppoeProfileDTO mikrotikProfile : mikrotikProfiles) {
                try {
                    // Verificar se profile já existe no banco
                    Optional<PppoeProfile> existingProfile = repository.findByNameAndMikrotikServer(
                            mikrotikProfile.getName(),
                            server
                    );

                    if (existingProfile.isPresent()) {
                        // Profile já existe, pular
                        result.setSkippedUsers(result.getSkippedUsers() + 1);
                        result.getSkippedUsernames().add(mikrotikProfile.getName());
                        log.debug("Profile {} já existe no banco, pulando", mikrotikProfile.getName());
                        continue;
                    }

                    // Criar novo profile no banco
                    PppoeProfile newProfile = new PppoeProfile();
                    newProfile.setName(mikrotikProfile.getName());

                    // Descrição/comentário
                    String description = mikrotikProfile.getComment() != null ?
                            mikrotikProfile.getComment() : "Sincronizado do Mikrotik";
                    newProfile.setDescription(description);

                    // Parsear rate-limit para extrair download e upload
                    if (mikrotikProfile.getRateLimit() != null && !mikrotikProfile.getRateLimit().isEmpty()) {
                        Long[] rates = parseRateLimit(mikrotikProfile.getRateLimit());
                        Long uploadRate = rates[0];
                        Long downloadRate = rates[1];
                        newProfile.setMaxBitrateUl(uploadRate); // Upload
                        newProfile.setMaxBitrateDl(downloadRate); // Download
                    } else {
                        newProfile.setMaxBitrateUl(0L);
                        newProfile.setMaxBitrateDl(0L);
                    }

                    // Session timeout - converter de string para segundos
                    if (mikrotikProfile.getSessionTimeout() != null && !mikrotikProfile.getSessionTimeout().isEmpty()) {
                        newProfile.setSessionTimeout(parseSessionTimeout(mikrotikProfile.getSessionTimeout()));
                    } else {
                        newProfile.setSessionTimeout(0);
                    }

                    // Status ativo
                    newProfile.setActive(mikrotikProfile.getDisabled() == null || !mikrotikProfile.getDisabled());

                    newProfile.setMikrotikServer(server);
                    newProfile.setCreatedAt(LocalDateTime.now());
                    newProfile.setUpdatedAt(LocalDateTime.now());

                    repository.save(newProfile);

                    result.setSyncedUsers(result.getSyncedUsers() + 1);
                    result.getSyncedUsernames().add(mikrotikProfile.getName());
                    log.info("Profile {} sincronizado com sucesso", mikrotikProfile.getName());

                } catch (Exception e) {
                    result.setFailedUsers(result.getFailedUsers() + 1);
                    String errorMsg = String.format("Erro ao sincronizar profile %s: %s",
                            mikrotikProfile.getName(), e.getMessage());
                    result.getErrorMessages().add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            log.info("Sincronização de profiles concluída. Total: {}, Sincronizados: {}, Pulados: {}, Falhas: {}",
                    result.getTotalMikrotikUsers(), result.getSyncedUsers(),
                    result.getSkippedUsers(), result.getFailedUsers());

        } catch (Exception e) {
            log.error("Erro ao buscar profiles do Mikrotik: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro ao buscar profiles do Mikrotik: " + e.getMessage());
        }

        return result;
    }

    /**
     * Parse rate-limit string from Mikrotik format (upload/download or upload/download burst-upload/burst-download)
     * Returns [upload, download] in bps
     */
    private Long[] parseRateLimit(String rateLimit) {
        try {
            // Formato típico: "10M/20M" ou "10M/20M 15M/25M"
            String[] parts = rateLimit.split("\\s+");
            String mainRate = parts[0]; // Pegar apenas a taxa principal, ignorar burst

            String[] rates = mainRate.split("/");
            if (rates.length >= 2) {
                Long upload = parseBandwidth(rates[0].trim());
                Long download = parseBandwidth(rates[1].trim());
                return new Long[]{upload, download};
            }
        } catch (Exception e) {
            log.warn("Erro ao parsear rate-limit '{}': {}", rateLimit, e.getMessage());
        }
        return new Long[]{0L, 0L};
    }

    /**
     * Convert bandwidth string (e.g., "10M", "1G", "512k") to bps
     */
    private Long parseBandwidth(String bandwidth) {
        try {
            bandwidth = bandwidth.toUpperCase().trim();
            long multiplier = 1;

            if (bandwidth.endsWith("G")) {
                multiplier = 1_000_000_000L;
                bandwidth = bandwidth.substring(0, bandwidth.length() - 1);
            } else if (bandwidth.endsWith("M")) {
                multiplier = 1_000_000L;
                bandwidth = bandwidth.substring(0, bandwidth.length() - 1);
            } else if (bandwidth.endsWith("K")) {
                multiplier = 1_000L;
                bandwidth = bandwidth.substring(0, bandwidth.length() - 1);
            }

            return Long.parseLong(bandwidth) * multiplier;
        } catch (Exception e) {
            log.warn("Erro ao parsear bandwidth '{}': {}", bandwidth, e.getMessage());
            return 0L;
        }
    }

    /**
     * Parse session timeout from Mikrotik format
     * Format can be: "1d 2h 30m", "2h", "30m", etc.
     * Returns seconds
     */
    private Integer parseSessionTimeout(String timeout) {
        try {
            timeout = timeout.toLowerCase().trim();
            int totalSeconds = 0;

            // Parse days
            if (timeout.contains("d")) {
                String[] parts = timeout.split("d");
                totalSeconds += Integer.parseInt(parts[0].trim()) * 86400;
                timeout = parts.length > 1 ? parts[1].trim() : "";
            }

            // Parse hours
            if (timeout.contains("h")) {
                String[] parts = timeout.split("h");
                totalSeconds += Integer.parseInt(parts[0].trim()) * 3600;
                timeout = parts.length > 1 ? parts[1].trim() : "";
            }

            // Parse minutes
            if (timeout.contains("m")) {
                String[] parts = timeout.split("m");
                totalSeconds += Integer.parseInt(parts[0].trim()) * 60;
                timeout = parts.length > 1 ? parts[1].trim() : "";
            }

            // Parse seconds
            if (timeout.contains("s")) {
                String[] parts = timeout.split("s");
                totalSeconds += Integer.parseInt(parts[0].trim());
            } else if (!timeout.isEmpty() && timeout.matches("\\d+")) {
                // Se for apenas número, assumir segundos
                totalSeconds += Integer.parseInt(timeout);
            }

            return totalSeconds;
        } catch (Exception e) {
            log.warn("Erro ao parsear session timeout '{}': {}", timeout, e.getMessage());
            return 0;
        }
    }

    private PppoeProfileDTO mapToDTO(PppoeProfile profile) {
        return new PppoeProfileDTO(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getMaxBitrateDl(),
                profile.getMaxBitrateUl(),
                profile.getSessionTimeout(),
                profile.getActive(),
                profile.getMikrotikServer().getId(),
                profile.getMikrotikServer().getName()
        );
    }
}
