package com.eric.store.common.util;

public class HashUtils {


    public static String sha256(String s){
        try {
            var d = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e){ throw new RuntimeException(e);
        }
    }

}
