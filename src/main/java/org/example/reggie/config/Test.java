package org.example.reggie.config;

import java.util.HashSet;

public class Test {

    public static boolean isValid(String s) {
        if (s.charAt(0) != 'a') return false;
        return helper(s, 1, 'a');
    }

    private static boolean helper(String s, int index, char pre) {
        if (index == s.length()) return true;

        char c = s.charAt(index);
        if (c != pre) {
            if (c == 'b') {
                return helper(s, index + 1, 'b');
            } else {
                return false;
            }
        }

        return helper(s, index + 1, pre);
    }

    public static void main(String[] args) {
        String s = "abab";
        System.out.println(isValid(s));
    }
}
