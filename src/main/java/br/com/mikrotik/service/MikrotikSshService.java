package br.com.mikrotik.service;

import br.com.mikrotik.dto.MikrotikPppoeProfileDTO;
import br.com.mikrotik.dto.MikrotikPppoeUserDTO;
import br.com.mikrotik.exception.MikrotikConnectionException;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MikrotikSshService {

    public boolean testConnection(String host, Integer port, String username, String password) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);
            session.disconnect();
            return true;
        } catch (JSchException e) {
            log.error("Erro ao conectar com Mikrotik: {}", e.getMessage());
            throw new MikrotikConnectionException("Falha ao conectar com servidor Mikrotik: " + e.getMessage());
        }
    }

    public List<String> executeCommand(String host, Integer port, String username, String password, String command) {
        List<String> result = new ArrayList<>();
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(channelExec.getInputStream())
            );
            channelExec.connect();

            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }

            channelExec.disconnect();
            session.disconnect();

            return result;
        } catch (JSchException | java.io.IOException e) {
            log.error("Erro ao executar comando no Mikrotik: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao executar comando: " + e.getMessage());
        }
    }

    public void createPppoeUser(String host, Integer port, String username, String password,
                               String pppoeUsername, String pppoePassword, String profileName) {
        String command = String.format(
                "/ppp secret add name=%s password=%s profile=%s service=pppoe",
                pppoeUsername, pppoePassword, profileName
        );
        executeCommand(host, port, username, password, command);
        log.info("Usuário PPPoE criado: {}", pppoeUsername);
    }

    public void deletePppoeUser(String host, Integer port, String username, String password, String pppoeUsername) {
        String command = String.format("/ppp secret remove [find name=%s]", pppoeUsername);
        executeCommand(host, port, username, password, command);
        log.info("Usuário PPPoE deletado: {}", pppoeUsername);
    }

    public void disablePppoeUser(String host, Integer port, String username, String password, String pppoeUsername) {
        String command = String.format("/ppp secret disable [find name=%s]", pppoeUsername);
        executeCommand(host, port, username, password, command);
        log.info("Usuário PPPoE desativado: {}", pppoeUsername);
    }

    public void enablePppoeUser(String host, Integer port, String username, String password, String pppoeUsername) {
        String command = String.format("/ppp secret enable [find name=%s]", pppoeUsername);
        executeCommand(host, port, username, password, command);
        log.info("Usuário PPPoE ativado: {}", pppoeUsername);
    }

    public List<String> listActivePppoeConnections(String host, Integer port, String username, String password) {
        String command = "/ppp active print";
        return executeCommand(host, port, username, password, command);
    }

    public List<String> listPppoeUsers(String host, Integer port, String username, String password) {
        String command = "/ppp secret print";
        return executeCommand(host, port, username, password, command);
    }

    public List<MikrotikPppoeUserDTO> getPppoeUsersStructured(String host, Integer port, String username, String password) {
        List<MikrotikPppoeUserDTO> users = new ArrayList<>();

        try {
            // Comando para listar secrets PPPoE com detalhes
            String command = "/ppp secret print detail";
            List<String> output = executeCommand(host, port, username, password, command);

            MikrotikPppoeUserDTO currentUser = null;

            for (String line : output) {
                line = line.trim();

                // Nova entrada de usuário
                if (line.startsWith("Flags:") || line.matches("^\\d+.*")) {
                    if (currentUser != null && currentUser.getUsername() != null) {
                        users.add(currentUser);
                    }
                    currentUser = new MikrotikPppoeUserDTO();
                    currentUser.setDisabled(line.contains("X") || line.contains("D"));
                }

                if (currentUser != null) {
                    // Parsear campos
                    if (line.contains("name=")) {
                        currentUser.setUsername(extractValue(line, "name"));
                    }
                    if (line.contains("password=")) {
                        currentUser.setPassword(extractValue(line, "password"));
                    }
                    if (line.contains("profile=")) {
                        currentUser.setProfile(extractValue(line, "profile"));
                    }
                    if (line.contains("service=")) {
                        currentUser.setService(extractValue(line, "service"));
                    }
                    if (line.contains("comment=")) {
                        currentUser.setComment(extractValue(line, "comment"));
                    }
                }
            }

            // Adicionar último usuário
            if (currentUser != null && currentUser.getUsername() != null) {
                users.add(currentUser);
            }

            log.info("Total de {} usuários PPPoE encontrados no Mikrotik", users.size());
            return users;

        } catch (Exception e) {
            log.error("Erro ao buscar usuários PPPoE do Mikrotik: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao buscar usuários PPPoE: " + e.getMessage());
        }
    }

    private String extractValue(String line, String key) {
        try {
            // Padrão: key="value" ou key=value
            Pattern pattern = Pattern.compile(key + "=\"?([^\"\\s]+)\"?");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("Erro ao extrair valor de '{}': {}", key, e.getMessage());
        }
        return null;
    }

    public List<MikrotikPppoeProfileDTO> getPppoeProfilesStructured(String host, Integer port, String username, String password) {
        List<MikrotikPppoeProfileDTO> profiles = new ArrayList<>();

        try {
            // Comando para listar profiles PPPoE com detalhes
            String command = "/ppp profile print detail";
            List<String> output = executeCommand(host, port, username, password, command);

            MikrotikPppoeProfileDTO currentProfile = null;

            for (String line : output) {
                line = line.trim();

                // Nova entrada de profile
                if (line.startsWith("Flags:") || line.matches("^\\d+.*")) {
                    if (currentProfile != null && currentProfile.getName() != null) {
                        profiles.add(currentProfile);
                    }
                    currentProfile = new MikrotikPppoeProfileDTO();
                    currentProfile.setDisabled(line.contains("X") || line.contains("D"));
                }

                if (currentProfile != null) {
                    // Parsear campos
                    if (line.contains("name=")) {
                        currentProfile.setName(extractValue(line, "name"));
                    }
                    if (line.contains("local-address=")) {
                        currentProfile.setLocalAddress(extractValue(line, "local-address"));
                    }
                    if (line.contains("remote-address=")) {
                        currentProfile.setRemoteAddress(extractValue(line, "remote-address"));
                    }
                    if (line.contains("rate-limit=")) {
                        currentProfile.setRateLimit(extractValue(line, "rate-limit"));
                    }
                    if (line.contains("session-timeout=")) {
                        currentProfile.setSessionTimeout(extractValue(line, "session-timeout"));
                    }
                    if (line.contains("comment=")) {
                        currentProfile.setComment(extractValue(line, "comment"));
                    }
                }
            }

            // Adicionar último profile
            if (currentProfile != null && currentProfile.getName() != null) {
                profiles.add(currentProfile);
            }

            log.info("Total de {} profiles PPPoE encontrados no Mikrotik", profiles.size());
            return profiles;

        } catch (Exception e) {
            log.error("Erro ao buscar profiles PPPoE do Mikrotik: {}", e.getMessage());
            throw new MikrotikConnectionException("Erro ao buscar profiles PPPoE: " + e.getMessage());
        }
    }
}
