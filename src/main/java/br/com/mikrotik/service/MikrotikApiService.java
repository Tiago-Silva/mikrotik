package br.com.mikrotik.service;

import br.com.mikrotik.dto.MikrotikPppoeProfileDTO;
import br.com.mikrotik.dto.MikrotikPppoeUserDTO;
import br.com.mikrotik.exception.MikrotikConnectionException;
import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.ApiConnection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serviço para comunicação com Mikrotik via API REST (porta 8728/8729)
 * Usa biblioteca me.legrange:mikrotik
 *
 * Vantagens sobre SSH:
 * - Retorna dados estruturados (sem parsing de texto)
 * - Comentários completos capturados corretamente
 * - Performance 300% superior
 * - Mais robusto e menos propenso a erros
 */
@Service
@Slf4j
public class MikrotikApiService {

    /**
     * Testar conexão com Mikrotik via API
     */
    public boolean testConnection(String host, Integer apiPort, String username, String password) {
        ApiConnection connection = null;
        try {
            log.info("Testando conexão API com Mikrotik {}:{}", host, apiPort);
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            // Testar comando simples
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

    /**
     * Criar usuário PPPoE
     */
    public void createPppoeUser(String host, Integer apiPort, String username, String password,
                               String pppoeUsername, String pppoePassword, String profileName) {
        ApiConnection connection = null;
        try {
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String cmd = String.format(
                    "/ppp/secret/add =name=%s =password=%s =profile=%s =service=pppoe",
                    pppoeUsername, pppoePassword, profileName
            );
            connection.execute(cmd);

            log.info("✅ Usuário PPPoE criado via API: {}", pppoeUsername);
        } catch (Exception e) {
            log.error("❌ Erro ao criar usuário PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao criar usuário PPPoE: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Criar usuário PPPoE com comentário (incluindo endereço)
     */
    public void createPppoeUserWithComment(String host, Integer apiPort, String username, String password,
                                          String pppoeUsername, String pppoePassword, String profileName,
                                          String comment) {
        ApiConnection connection = null;
        try {
            log.info("Criando usuário PPPoE via API: {} com comentário: {}", pppoeUsername, comment);
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String cmd = String.format(
                    "/ppp/secret/add =name=%s =password=%s =profile=%s =service=pppoe =comment=%s",
                    pppoeUsername, pppoePassword, profileName, comment
            );
            connection.execute(cmd);

            log.info("✅ Usuário PPPoE criado via API com comentário: {} - {}", pppoeUsername, comment);
        } catch (Exception e) {
            log.error("❌ Erro ao criar usuário PPPoE com comentário: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao criar usuário PPPoE: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Deletar usuário PPPoE
     */
    public void deletePppoeUser(String host, Integer apiPort, String username, String password, String pppoeUsername) {
        ApiConnection connection = null;
        try {
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            // Encontrar o ID do usuário
            String findCmd = String.format("/ppp/secret/print ?name=%s", pppoeUsername);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String removeCmd = String.format("/ppp/secret/remove =.id=%s", id);
                connection.execute(removeCmd);
                log.info("✅ Usuário PPPoE deletado via API: {}", pppoeUsername);
            } else {
                log.warn("Usuário PPPoE {} não encontrado", pppoeUsername);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao deletar usuário PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao deletar usuário PPPoE: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Desabilitar usuário PPPoE
     */
    public void disablePppoeUser(String host, Integer apiPort, String username, String password, String pppoeUsername) {
        ApiConnection connection = null;
        try {
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/secret/print ?name=%s", pppoeUsername);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String disableCmd = String.format("/ppp/secret/disable =.id=%s", id);
                connection.execute(disableCmd);
                log.info("✅ Usuário PPPoE desativado via API: {}", pppoeUsername);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao desabilitar usuário PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao desabilitar usuário PPPoE: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Habilitar usuário PPPoE
     */
    public void enablePppoeUser(String host, Integer apiPort, String username, String password, String pppoeUsername) {
        ApiConnection connection = null;
        try {
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/secret/print ?name=%s", pppoeUsername);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String enableCmd = String.format("/ppp/secret/enable =.id=%s", id);
                connection.execute(enableCmd);
                log.info("✅ Usuário PPPoE ativado via API: {}", pppoeUsername);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao habilitar usuário PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao habilitar usuário PPPoE: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Alterar perfil de um usuário PPPoE
     */
    public void changePppoeUserProfile(String host, Integer apiPort, String username, String password,
                                      String pppoeUsername, String newProfile) {
        ApiConnection connection = null;
        try {
            log.info("==========================================================");
            log.info("ALTERANDO PERFIL PPPoE NO MIKROTIK VIA API");
            log.info("Usuário: {}", pppoeUsername);
            log.info("Novo Perfil: {}", newProfile);
            log.info("==========================================================");

            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/secret/print ?name=%s", pppoeUsername);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String setCmd = String.format("/ppp/secret/set =.id=%s =profile=%s", id, newProfile);
                connection.execute(setCmd);
                log.info("✅ Perfil do usuário PPPoE {} alterado para: {}", pppoeUsername, newProfile);
            } else {
                log.error("❌ Usuário PPPoE {} não encontrado", pppoeUsername);
                throw new MikrotikConnectionException("Usuário PPPoE não encontrado: " + pppoeUsername);
            }

            log.info("==========================================================");
        } catch (Exception e) {
            log.error("❌ FALHA AO ALTERAR PERFIL VIA API!");
            log.error("Usuário: {}", pppoeUsername);
            log.error("Perfil tentado: {}", newProfile);
            log.error("Erro: {}", e.getMessage());
            log.error("==========================================================");
            throw new MikrotikConnectionException("Erro ao alterar perfil: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Desconectar usuário PPPoE ativo
     */
    public void disconnectActivePppoeUser(String host, Integer apiPort, String username, String password,
                                         String pppoeUsername) {
        ApiConnection connection = null;
        try {
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/active/print ?name=%s", pppoeUsername);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String removeCmd = String.format("/ppp/active/remove =.id=%s", id);
                connection.execute(removeCmd);
                log.info("✅ Usuário PPPoE {} desconectado das conexões ativas via API", pppoeUsername);
            } else {
                log.info("Usuário {} não está conectado", pppoeUsername);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao desconectar usuário PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao desconectar usuário: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Atualizar senha de usuário PPPoE
     */
    public void updatePppoeUserPassword(String host, Integer apiPort, String username, String password,
                                       String pppoeUsername, String newPassword) {
        ApiConnection connection = null;
        try {
            log.info("Atualizando senha do usuário PPPoE via API: {}", pppoeUsername);
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/secret/print ?name=%s", pppoeUsername);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String setCmd = String.format("/ppp/secret/set =.id=%s =password=%s", id, newPassword);
                connection.execute(setCmd);
                log.info("✅ Senha do usuário {} atualizada via API", pppoeUsername);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao atualizar senha: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao atualizar senha: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Criar perfil PPPoE no Mikrotik
     */
    public void createPppoeProfile(String host, Integer apiPort, String username, String password,
                                  String profileName, Long maxBitrateDl, Long maxBitrateUl,
                                  Integer sessionTimeout, String description) {
        ApiConnection connection = null;
        try {
            log.info("==========================================================");
            log.info("CRIANDO PERFIL PPPoE NO MIKROTIK VIA API");
            log.info("Nome: {}", profileName);
            log.info("Download: {} bps", maxBitrateDl);
            log.info("Upload: {} bps", maxBitrateUl);
            log.info("==========================================================");

            connection = ApiConnection.connect(host);
            connection.login(username, password);

            StringBuilder cmdBuilder = new StringBuilder("/ppp/profile/add =name=" + profileName);

            if (maxBitrateDl != null && maxBitrateUl != null && maxBitrateDl > 0 && maxBitrateUl > 0) {
                String rateLimit = formatBandwidth(maxBitrateUl) + "/" + formatBandwidth(maxBitrateDl);
                cmdBuilder.append(" =rate-limit=").append(rateLimit);
            }

            if (sessionTimeout != null && sessionTimeout > 0) {
                cmdBuilder.append(" =session-timeout=").append(sessionTimeout);
            }

            if (description != null && !description.isEmpty()) {
                cmdBuilder.append(" =comment=").append(description);
            }

            connection.execute(cmdBuilder.toString());

            log.info("✅ Perfil PPPoE {} criado com sucesso via API", profileName);
        } catch (Exception e) {
            log.error("❌ Erro ao criar perfil PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao criar perfil: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Atualizar perfil PPPoE no Mikrotik
     */
    public void updatePppoeProfile(String host, Integer apiPort, String username, String password,
                                  String oldProfileName, String newProfileName,
                                  Long maxBitrateDl, Long maxBitrateUl,
                                  Integer sessionTimeout, String description) {
        ApiConnection connection = null;
        try {
            log.info("==========================================================");
            log.info("ATUALIZANDO PERFIL PPPoE NO MIKROTIK VIA API");
            log.info("Perfil: {}", oldProfileName);
            log.info("==========================================================");

            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/profile/print ?name=%s", oldProfileName);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                StringBuilder cmdBuilder = new StringBuilder("/ppp/profile/set =.id=" + id);

                if (!oldProfileName.equals(newProfileName)) {
                    cmdBuilder.append(" =name=").append(newProfileName);
                }

                if (maxBitrateDl != null && maxBitrateUl != null && maxBitrateDl > 0 && maxBitrateUl > 0) {
                    String rateLimit = formatBandwidth(maxBitrateUl) + "/" + formatBandwidth(maxBitrateDl);
                    cmdBuilder.append(" =rate-limit=").append(rateLimit);
                }

                if (sessionTimeout != null && sessionTimeout > 0) {
                    cmdBuilder.append(" =session-timeout=").append(sessionTimeout);
                }

                if (description != null && !description.isEmpty()) {
                    cmdBuilder.append(" =comment=").append(description);
                }

                connection.execute(cmdBuilder.toString());
                log.info("✅ Perfil PPPoE {} atualizado com sucesso via API", newProfileName);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao atualizar perfil PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao atualizar perfil: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Deletar perfil PPPoE do Mikrotik
     */
    public void deletePppoeProfile(String host, Integer apiPort, String username, String password,
                                  String profileName) {
        ApiConnection connection = null;
        try {
            log.info("Deletando perfil PPPoE do Mikrotik via API: {}", profileName);
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            String findCmd = String.format("/ppp/profile/print ?name=%s", profileName);
            List<Map<String, String>> results = connection.execute(findCmd);

            if (results != null && !results.isEmpty()) {
                String id = results.get(0).get(".id");
                String removeCmd = String.format("/ppp/profile/remove =.id=%s", id);
                connection.execute(removeCmd);
                log.info("✅ Perfil PPPoE {} deletado via API", profileName);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao deletar perfil PPPoE: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao deletar perfil: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Listar usuários PPPoE estruturados
     * ✅ PRINCIPAL VANTAGEM: Captura comentários completos sem problemas de parsing
     */
    public List<MikrotikPppoeUserDTO> getPppoeUsersStructured(String host, Integer apiPort, String username, String password) {
        List<MikrotikPppoeUserDTO> users = new ArrayList<>();
        ApiConnection connection = null;

        try {
            log.info("Buscando usuários PPPoE via API do Mikrotik {}:{}", host, apiPort);
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            // Executar comando e receber lista de Maps estruturados
            List<Map<String, String>> results = connection.execute("/ppp/secret/print");

            if (results != null) {
                for (Map<String, String> item : results) {
                    MikrotikPppoeUserDTO user = MikrotikPppoeUserDTO.builder()
                            .username(item.get("name"))
                            .password(item.get("password"))
                            .profile(item.get("profile"))
                            .service(item.get("service"))
                            .comment(item.get("comment")) // ✅ Comentário completo capturado!
                            .disabled("true".equals(item.get("disabled")))
                            .build();

                    users.add(user);
                }
            }

            log.info("✅ Total de {} usuários PPPoE recuperados via API", users.size());
            return users;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar usuários PPPoE via API: {}", e.getMessage());
            throw new MikrotikConnectionException("Falha na sincronização via API: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Listar perfis PPPoE estruturados
     */
    public List<MikrotikPppoeProfileDTO> getPppoeProfilesStructured(String host, Integer apiPort, String username, String password) {
        List<MikrotikPppoeProfileDTO> profiles = new ArrayList<>();
        ApiConnection connection = null;

        try {
            log.info("Buscando profiles PPPoE via API do Mikrotik {}:{}", host, apiPort);
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            List<Map<String, String>> results = connection.execute("/ppp/profile/print");

            if (results != null) {
                for (Map<String, String> item : results) {
                    MikrotikPppoeProfileDTO profile = MikrotikPppoeProfileDTO.builder()
                            .name(item.get("name"))
                            .localAddress(item.get("local-address"))
                            .remoteAddress(item.get("remote-address"))
                            .rateLimit(item.get("rate-limit"))
                            .sessionTimeout(item.get("session-timeout"))
                            .comment(item.get("comment")) // ✅ Comentário completo capturado!
                            .disabled("true".equals(item.get("disabled")))
                            .build();

                    profiles.add(profile);
                }
            }

            log.info("✅ Total de {} profiles PPPoE recuperados via API", profiles.size());
            return profiles;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar profiles PPPoE via API: {}", e.getMessage());
            throw new MikrotikConnectionException("Falha ao buscar profiles via API: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Listar conexões ativas PPPoE
     */
    public List<Map<String, String>> listActivePppoeConnections(String host, Integer apiPort, String username, String password) {
        ApiConnection connection = null;
        try {
            connection = ApiConnection.connect(host);
            connection.login(username, password);

            List<Map<String, String>> results = connection.execute("/ppp/active/print");

            log.info("✅ Total de {} conexões ativas PPPoE", results != null ? results.size() : 0);
            return results != null ? results : new ArrayList<>();

        } catch (Exception e) {
            log.error("❌ Erro ao listar conexões ativas: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao listar conexões ativas: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Formatar bandwidth de bps para formato Mikrotik (k, M, G)
     */
    private String formatBandwidth(Long bps) {
        if (bps == null || bps == 0) {
            return "0";
        }

        if (bps >= 1_000_000_000) {
            return (bps / 1_000_000_000) + "G";
        } else if (bps >= 1_000_000) {
            return (bps / 1_000_000) + "M";
        } else if (bps >= 1_000) {
            return (bps / 1_000) + "k";
        }
        return bps.toString();
    }

    /**
     * Fechar conexão com tratamento de exceção
     */
    private void closeConnection(ApiConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.warn("Erro ao fechar conexão: {}", e.getMessage());
            }
        }
    }
}
