package dam.dad.app.util;

public class ValidationUtils {
    
    // Longitud mínima y máxima para el nombre de usuario
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    
    // Longitud mínima y máxima para la contraseña
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 20;
    
    // Expresión regular para validar el nombre de usuario
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    public static String validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "El nombre de usuario no puede estar vacío";
        }
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "El nombre de usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres";
        }
        
        if (username.length() > MAX_USERNAME_LENGTH) {
            return "El nombre de usuario no puede tener más de " + MAX_USERNAME_LENGTH + " caracteres";
        }
        
        if (!username.matches(USERNAME_PATTERN)) {
            return "El nombre de usuario solo puede contener letras, números y guiones bajos";
        }
        
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres";
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "La contraseña no puede tener más de " + MAX_PASSWORD_LENGTH + " caracteres";
        }
        
        return null;
    }
} 