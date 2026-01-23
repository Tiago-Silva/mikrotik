package br.com.mikrotik.util;

public class DocumentValidator {

    /**
     * Valida CPF (Pessoa Física)
     */
    public static boolean isValidCPF(String cpf) {
        if (cpf == null) {
            return false;
        }

        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^\\d]", "");

        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Calcula primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) {
            firstDigit = 0;
        }

        // Verifica primeiro dígito
        if (Character.getNumericValue(cpf.charAt(9)) != firstDigit) {
            return false;
        }

        // Calcula segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) {
            secondDigit = 0;
        }

        // Verifica segundo dígito
        return Character.getNumericValue(cpf.charAt(10)) == secondDigit;
    }

    /**
     * Valida CNPJ (Pessoa Jurídica)
     */
    public static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("[^\\d]", "");

        // Verifica se tem 14 dígitos
        if (cnpj.length() != 14) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        // Calcula primeiro dígito verificador
        int[] weight1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weight1[i];
        }
        int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        // Verifica primeiro dígito
        if (Character.getNumericValue(cnpj.charAt(12)) != firstDigit) {
            return false;
        }

        // Calcula segundo dígito verificador
        int[] weight2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weight2[i];
        }
        int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        // Verifica segundo dígito
        return Character.getNumericValue(cnpj.charAt(13)) == secondDigit;
    }

    /**
     * Valida documento baseado no tipo
     */
    public static boolean isValidDocument(String document, String type) {
        if (document == null || type == null) {
            return false;
        }

        if ("FISICA".equalsIgnoreCase(type)) {
            return isValidCPF(document);
        } else if ("JURIDICA".equalsIgnoreCase(type)) {
            return isValidCNPJ(document);
        }

        return false;
    }

    /**
     * Formata CPF
     */
    public static String formatCPF(String cpf) {
        if (cpf == null) {
            return null;
        }
        cpf = cpf.replaceAll("[^\\d]", "");
        if (cpf.length() != 11) {
            return cpf;
        }
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    /**
     * Formata CNPJ
     */
    public static String formatCNPJ(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        cnpj = cnpj.replaceAll("[^\\d]", "");
        if (cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    /**
     * Remove formatação de documento
     */
    public static String unformat(String document) {
        if (document == null) {
            return null;
        }
        return document.replaceAll("[^\\d]", "");
    }
}
