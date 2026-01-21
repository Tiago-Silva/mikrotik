package br.com.mikrotik.constant;

public class ApiConstants {

    // HTTP Status Messages
    public static final String SUCCESS = "Operação realizada com sucesso";
    public static final String ERROR = "Erro ao processar solicitação";

    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_OPERATOR = "OPERATOR";
    public static final String ROLE_VIEWER = "VIEWER";

    // API Endpoints
    public static final String API_PREFIX = "/api";
    public static final String AUTH_PREFIX = API_PREFIX + "/auth";
    public static final String SERVERS_PREFIX = API_PREFIX + "/mikrotik-servers";
    public static final String PROFILES_PREFIX = API_PREFIX + "/profiles";
    public static final String USERS_PREFIX = API_PREFIX + "/users";
    public static final String CONNECTIONS_PREFIX = API_PREFIX + "/connections";

    // Error Messages
    public static final String SERVER_NOT_FOUND = "Servidor Mikrotik não encontrado";
    public static final String PROFILE_NOT_FOUND = "Perfil PPPoE não encontrado";
    public static final String USER_NOT_FOUND = "Usuário PPPoE não encontrado";
    public static final String CONNECTION_NOT_FOUND = "Conexão não encontrada";
    public static final String USERNAME_ALREADY_EXISTS = "Username já existe neste servidor";
    public static final String INVALID_CREDENTIALS = "Credenciais inválidas";
    public static final String UNAUTHORIZED = "Não autorizado";
    public static final String CONNECTION_FAILED = "Falha ao conectar com servidor Mikrotik";

    // Mikrotik Commands
    public static final String CMD_PPP_SECRET_ADD = "/ppp secret add";
    public static final String CMD_PPP_SECRET_REMOVE = "/ppp secret remove";
    public static final String CMD_PPP_SECRET_DISABLE = "/ppp secret disable";
    public static final String CMD_PPP_SECRET_ENABLE = "/ppp secret enable";
    public static final String CMD_PPP_ACTIVE_PRINT = "/ppp active print";
    public static final String CMD_PPP_SECRET_PRINT = "/ppp secret print";

    // Default Values
    public static final int DEFAULT_SSH_PORT = 22;
    public static final int DEFAULT_SESSION_TIMEOUT = 3600; // 1 hour
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Validation
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 255;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_PROFILE_NAME_LENGTH = 2;

    private ApiConstants() {
        // Utility class
    }
}
