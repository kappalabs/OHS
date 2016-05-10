package com.kappa_labs.ohunter.server.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class providing utilities for hashing the password.
 */
public class PasswordUtils {

    /**
     * Transform array of char to array of Character.
     *
     * @param array Input array for transformation.
     * @return Array of Character with the same characters.
     */
    public static Character[] toCharacter(char[] array) {
        Character[] characters = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            characters[i] = array[i];
        }
        return characters;
    }

    /**
     * Compute SHA hash of given password.
     *
     * @param password Array to be hashed.
     * @return The SHA hash output string.
     */
    public static String getDigest(char[] password) {
        String digest = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
            digest = byteArray2Hex(md.digest(new String(password).getBytes()));
//            String encoding = "UTF-8";
//            digest = new String(md.digest(new String(password).getBytes()), encoding);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PasswordUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return digest;
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    
}
