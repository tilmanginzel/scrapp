package de.medieninf.mobcomp.scrapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class to generate a secure hash for a given string.
 */
public class HashGenerator {

    /**
     * Creates a hash with SHA-1 algorithm for a given string.
     *
     * @param content to hash
     * @return SHA-1 hash for string
     */
    public String getSha1Hash(String content){
        return generateHash(content, "SHA-1");
    }

    /**
     * Generates a hash value for a given string and a given algorithm as string.
     *
     * @param content to hash
     * @param algorithm to use
     * @return generated hash for string
     */
    private String generateHash(String content, String algorithm){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte byteData [] = new byte[0];
        if (md != null) {
            md.update(content.getBytes());
            byteData = md.digest();
        }

        StringBuilder sb = new StringBuilder();
        for (byte aByteData : byteData) {
            sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
