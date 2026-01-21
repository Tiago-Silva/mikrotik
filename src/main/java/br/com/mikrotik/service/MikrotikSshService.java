package br.com.mikrotik.service;

import br.com.mikrotik.exception.MikrotikConnectionException;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
        executeCommand(host, port, username, password, "/interface/pppoe-server/remote-access");
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
}
