package br.com.mikrotik.service;

import br.com.mikrotik.dto.MikrotikPppoeProfileDTO;
import br.com.mikrotik.dto.MikrotikPppoeUserDTO;
import br.com.mikrotik.exception.MikrotikConnectionException;
import br.com.mikrotik.model.PppoeProfile;
import br.com.mikrotik.model.PppoeUser;
import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.ApiConnection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serviço para comunicação com Mikrotik via API REST
 * Correção Final: Uso estrito de sintaxe CLI (numbers ao invés de .id)
 */
@Service
@Slf4j
public class MikrotikApiService {

    // ==================================================================================
    // MÉTODOS PÚBLICOS
    // ==================================================================================

    public boolean testConnection(String host, Integer apiPort, String username, String password) {
        ApiConnection connection = null;
        try {
            log.info("Testando conexão API com Mikrotik {}:{}", host, apiPort);
            connection = connect(host, username, password);
            connection.execute("/system/identity/print");
            log.info("✅ Conexão API com Mikrotik estabelecida com sucesso");
            return true;
        } catch (Exception e) {
            log.error("❌ Erro ao conectar com Mikrotik via API: {}", e.getMessage());
            throw new MikrotikConnectionException("Falha ao conectar com servidor Mikrotik via API: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    public void createPppoeUser(String host, Integer apiPort, String username, String password,
                                String pppoeUsername, String pppoePassword, String profileName) {
        createPppoeUserWithComment(host, apiPort, username, password, pppoeUsername, pppoePassword, profileName, null);
    }

    public void createPppoeUserWithComment(String host, Integer apiPort, String username, String password,
                                           String pppoeUsername, String pppoePassword, String profileName,
                                           String comment) {
        ApiConnection connection = null;
        try {
            String safeComment = sanitizeComment(comment);

            log.info("Criando usuário PPPoE: {} | Profile: {} | Comentário Safe: {}", pppoeUsername, profileName, safeComment);
            connection = connect(host, username, password);

            // Sintaxe CLI (create funcionou assim)
            String cmd = String.format("/ppp/secret/add name=%s password=%s profile=%s service=pppoe",
                    formatParam(pppoeUsername),
                    formatParam(pppoePassword),
                    formatParam(profileName));

            if (safeComment != null && !safeComment.isEmpty()) {
                cmd += " comment=" + formatParam(safeComment);
            }

            connection.execute(cmd);
            log.info("✅ Usuário PPPoE criado com sucesso: {}", pppoeUsername);

        } catch (Exception e) {
            handleException("Erro ao criar usuário PPPoE", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void deletePppoeUser(String host, Integer apiPort, String username, String password, String pppoeUsername) {
        ApiConnection connection = null;
        try {
            connection = connect(host, username, password);
            String id = findIdByNameListAll(connection, "/ppp/secret/print", pppoeUsername);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                connection.execute(String.format("/ppp/secret/remove numbers=%s", id));
                log.info("✅ Usuário PPPoE deletado: {}", pppoeUsername);
            } else {
                log.warn("Usuário PPPoE {} não encontrado para exclusão", pppoeUsername);
            }
        } catch (Exception e) {
            handleException("Erro ao deletar usuário PPPoE", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void disablePppoeUser(String host, Integer apiPort, String username, String password, String pppoeUsername) {
        executeToggleCommand(host, username, password, "/ppp/secret", pppoeUsername, "disable");
    }

    public void enablePppoeUser(String host, Integer apiPort, String username, String password, String pppoeUsername) {
        executeToggleCommand(host, username, password, "/ppp/secret", pppoeUsername, "enable");
    }

    public void changePppoeUserAll(String host, Integer apiPort, String username, String password,
                                   PppoeUser pppoeUser, PppoeProfile newProfile) {

        ApiConnection connection = null;
        try {
            log.info("Alterando dados completos do usuário: {}", pppoeUser.getUsername());
            connection = connect(host, username, password);

            String id = findIdByNameListAll(connection, "/ppp/secret/print", pppoeUser.getUsername());

            if (id != null) {
                StringBuilder cmd = new StringBuilder("/ppp/secret/set numbers=" + id);

                // Adiciona apenas os campos que foram informados no DTO
                if (pppoeUser.getUsername() != null && !pppoeUser.getUsername().isEmpty()) {
                    cmd.append(" name=").append(formatParam(pppoeUser.getUsername()));
                }
                if (pppoeUser.getPassword() != null && !pppoeUser.getPassword().isEmpty()) {
                    cmd.append(" password=").append(formatParam(pppoeUser.getPassword()));
                }
                if (newProfile != null) {
                    cmd.append(" profile=").append(formatParam(newProfile.getName()));
                }
                if (pppoeUser.getComment() != null) {
                    String safeComment = sanitizeComment(pppoeUser.getComment());
                    cmd.append(" comment=").append(formatParam(safeComment));
                }

                connection.execute(cmd.toString());
                log.info("✅ Dados do usuário alterados com sucesso em uma única operação");
                } else {
                throw new MikrotikConnectionException("Usuário PPPoE não encontrado: " + pppoeUser.getUsername());
            }
        } catch (Exception e) {
            handleException("Erro ao alterar dados do usuário PPPoE", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void changePppoeUserProfile(String host, Integer apiPort, String username, String password,
                                       String pppoeUsername, String newProfile) {
        ApiConnection connection = null;
        try {
            log.info("Alterando perfil do usuário {} para {}", pppoeUsername, newProfile);
            connection = connect(host, username, password);

            String id = findIdByNameListAll(connection, "/ppp/secret/print", pppoeUsername);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                String cmd = String.format("/ppp/secret/set numbers=%s profile=%s", id, formatParam(newProfile));
                connection.execute(cmd);
                log.info("✅ Perfil alterado com sucesso.");
            } else {
                throw new MikrotikConnectionException("Usuário PPPoE não encontrado: " + pppoeUsername);
            }
        } catch (Exception e) {
            handleException("Erro ao alterar perfil", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void updatePppoeUserPassword(String host, Integer apiPort, String username, String password,
                                        String pppoeUsername, String newPassword) {
        ApiConnection connection = null;
        try {
            log.info("Atualizando senha do usuário: {}", pppoeUsername);
            connection = connect(host, username, password);

            String id = findIdByNameListAll(connection, "/ppp/secret/print", pppoeUsername);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                String cmd = String.format("/ppp/secret/set numbers=%s password=%s", id, formatParam(newPassword));
                connection.execute(cmd);
                log.info("✅ Senha atualizada com sucesso.");
            }
        } catch (Exception e) {
            handleException("Erro ao atualizar senha", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void disconnectActivePppoeUser(String host, Integer apiPort, String username, String password,
                                          String pppoeUsername) {
        ApiConnection connection = null;
        try {
            connection = connect(host, username, password);
            String id = findIdByNameListAll(connection, "/ppp/active/print", pppoeUsername);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                connection.execute(String.format("/ppp/active/remove numbers=%s", id));
                log.info("✅ Usuário desconectado da sessão ativa: {}", pppoeUsername);
            } else {
                log.info("Usuário {} não está conectado no momento.", pppoeUsername);
            }
        } catch (Exception e) {
            handleException("Erro ao desconectar usuário", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void createPppoeProfile(String host, Integer apiPort, String username, String password,
                                   String profileName, Long maxBitrateDl, Long maxBitrateUl,
                                   Integer sessionTimeout, String description) {
        ApiConnection connection = null;
        try {
            log.info("Criando Profile: {}", profileName);
            connection = connect(host, username, password);

            StringBuilder cmd = new StringBuilder("/ppp/profile/add");
            cmd.append(" name=").append(formatParam(profileName));

            if (maxBitrateDl != null && maxBitrateUl != null && maxBitrateDl > 0 && maxBitrateUl > 0) {
                String rateLimit = formatBandwidth(maxBitrateUl) + "/" + formatBandwidth(maxBitrateDl);
                cmd.append(" rate-limit=").append(formatParam(rateLimit));
            }

            if (sessionTimeout != null && sessionTimeout > 0) {
                cmd.append(" session-timeout=").append(sessionTimeout);
            }

            if (description != null && !description.isEmpty()) {
                cmd.append(" comment=").append(formatParam(sanitizeComment(description)));
            }

            connection.execute(cmd.toString());
            log.info("✅ Perfil criado com sucesso.");

        } catch (Exception e) {
            handleException("Erro ao criar perfil", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void updatePppoeProfile(String host, Integer apiPort, String username, String password,
                                   String oldProfileName, String newProfileName,
                                   Long maxBitrateDl, Long maxBitrateUl,
                                   Integer sessionTimeout, String description) {
        ApiConnection connection = null;
        try {
            log.info("Atualizando Profile: {}", oldProfileName);
            connection = connect(host, username, password);

            String id = findIdByNameListAll(connection, "/ppp/profile/print", oldProfileName);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                StringBuilder cmd = new StringBuilder("/ppp/profile/set numbers=" + id);

                if (!oldProfileName.equals(newProfileName)) {
                    cmd.append(" name=").append(formatParam(newProfileName));
                }
                if (maxBitrateDl != null && maxBitrateUl != null) {
                    String rateLimit = formatBandwidth(maxBitrateUl) + "/" + formatBandwidth(maxBitrateDl);
                    cmd.append(" rate-limit=").append(formatParam(rateLimit));
                }
                if (sessionTimeout != null && sessionTimeout > 0) {
                    cmd.append(" session-timeout=").append(sessionTimeout);
                }
                if (description != null && !description.isEmpty()) {
                    cmd.append(" comment=").append(formatParam(sanitizeComment(description)));
                }

                connection.execute(cmd.toString());
                log.info("✅ Perfil atualizado com sucesso.");
            } else {
                log.warn("Perfil não encontrado: {}", oldProfileName);
            }
        } catch (Exception e) {
            handleException("Erro ao atualizar perfil", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void deletePppoeProfile(String host, Integer apiPort, String username, String password, String profileName) {
        ApiConnection connection = null;
        try {
            connection = connect(host, username, password);
            String id = findIdByNameListAll(connection, "/ppp/profile/print", profileName);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                connection.execute(String.format("/ppp/profile/remove numbers=%s", id));
                log.info("✅ Perfil deletado: {}", profileName);
            }
        } catch (Exception e) {
            handleException("Erro ao deletar perfil", e);
        } finally {
            closeConnection(connection);
        }
    }

    // LISTAGEM ESTRUTURADA
    public List<MikrotikPppoeUserDTO> getPppoeUsersStructured(String host, Integer apiPort, String username, String password) {
        ApiConnection connection = null;
        List<MikrotikPppoeUserDTO> users = new ArrayList<>();

        try {
            connection = connect(host, username, password);
            List<Map<String, String>> results = connection.execute("/ppp/secret/print");

            if (results != null) {
                for (Map<String, String> item : results) {
                    MikrotikPppoeUserDTO user = MikrotikPppoeUserDTO.builder()
                            .username(item.get("name"))
                            .password(item.get("password"))
                            .profile(item.get("profile"))
                            .service(item.get("service"))
                            .comment(item.get("comment"))
                            .disabled("true".equals(item.get("disabled")))
                            .build();
                    users.add(user);
                }
            }
            return users;
        } catch (Exception e) {
            handleException("Erro ao listar usuários", e);
            return new ArrayList<>();
        } finally {
            closeConnection(connection);
        }
    }

    public List<MikrotikPppoeProfileDTO> getPppoeProfilesStructured(String host, Integer apiPort, String username, String password) {
        ApiConnection connection = null;
        List<MikrotikPppoeProfileDTO> profiles = new ArrayList<>();

        try {
            connection = connect(host, username, password);
            List<Map<String, String>> results = connection.execute("/ppp/profile/print");

            if (results != null) {
                for (Map<String, String> item : results) {
                    MikrotikPppoeProfileDTO profile = MikrotikPppoeProfileDTO.builder()
                            .name(item.get("name"))
                            .localAddress(item.get("local-address"))
                            .remoteAddress(item.get("remote-address"))
                            .rateLimit(item.get("rate-limit"))
                            .sessionTimeout(item.get("session-timeout"))
                            .comment(item.get("comment"))
                            .disabled("true".equals(item.get("disabled")))
                            .build();
                    profiles.add(profile);
                }
            }
            return profiles;
        } catch (Exception e) {
            handleException("Erro ao listar perfis", e);
            return new ArrayList<>();
        } finally {
            closeConnection(connection);
        }
    }

    public List<Map<String, String>> listActivePppoeConnections(String host, Integer apiPort, String username, String password) {
        ApiConnection connection = null;
        try {
            connection = connect(host, username, password);
            return connection.execute("/ppp/active/print");
        } catch (Exception e) {
            handleException("Erro ao listar conexões ativas", e);
            return new ArrayList<>();
        } finally {
            closeConnection(connection);
        }
    }

    // ==================================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ==================================================================================

    private ApiConnection connect(String host, String username, String password) throws Exception {
        ApiConnection con = ApiConnection.connect(host);
        con.login(username, password);
        return con;
    }

    private String formatParam(String value) {
        if (value == null) return "\"\"";
        return "\"" + value + "\"";
    }

    private String sanitizeComment(String comment) {
        if (comment == null) return "";
        return comment
                .replace("#", "Nr")
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\"", "'")
                .trim();
    }

    private String findIdByName(ApiConnection connection, String printCommand, String name) throws Exception {
        String cmd = String.format("%s ?name=%s", printCommand, name);
        List<Map<String, String>> results = connection.execute(cmd);

        if (results != null && !results.isEmpty()) {
            return results.get(0).get(".id");
        }
        return null;
    }

    private String findIdByNameListAll(ApiConnection connection, String printCommand, String name) throws Exception {
        // ABORDAGEM SEGURA: Listar tudo e filtrar no Java.
        // Isso evita erros de sintaxe de query (where/?) que variam entre versões da lib.
        List<Map<String, String>> results = connection.execute(printCommand);

        if (results != null) {
            for (Map<String, String> item : results) {
                // Compara o nome que buscamos com o nome no Map
                // Usamos equalsIgnoreCase para ser mais robusto
                if (name.equalsIgnoreCase(item.get("name"))) {
                    return item.get(".id");
                }
            }
        }
        return null;
    }

    private void executeToggleCommand(String host, String username, String password, String basePath, String pppoeUsername, String action) {
        ApiConnection connection = null;
        try {
            connection = connect(host, username, password);
            String id = findIdByNameListAll(connection, basePath + "/print", pppoeUsername);

            if (id != null) {
                // CORREÇÃO: numbers ao invés de .id
                connection.execute(String.format("%s/%s numbers=%s", basePath, action, id));
                log.info("✅ Usuário {}: {}", action, pppoeUsername);
            }
        } catch (Exception e) {
            handleException("Erro ao " + action + " usuário", e);
        } finally {
            closeConnection(connection);
        }
    }

    private String formatBandwidth(Long bps) {
        if (bps == null || bps == 0) return "0";
        if (bps >= 1_000_000_000) return (bps / 1_000_000_000) + "G";
        if (bps >= 1_000_000) return (bps / 1_000_000) + "M";
        if (bps >= 1_000) return (bps / 1_000) + "k";
        return bps.toString();
    }

    private void closeConnection(ApiConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.warn("Erro ao fechar conexão (ignorado): {}", e.getMessage());
            }
        }
    }

    private void handleException(String msg, Exception e) {
        log.error("❌ {}: {}", msg, e.getMessage());
        throw new MikrotikConnectionException(msg + ": " + e.getMessage());
    }
}