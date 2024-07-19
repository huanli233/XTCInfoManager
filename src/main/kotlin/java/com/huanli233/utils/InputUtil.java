package com.huanli233.utils;


import java.util.Scanner;

public class InputUtil {
	private static Scanner scanner = new Scanner(System.in);

    public static String promptUser(String message) {
        System.out.print(message);
        String input = scanner.nextLine();
        if (input.startsWith("\"") || input.startsWith("'")) {
			input = input.substring(1);
		}
        if (input.endsWith("\"") || input.endsWith("'")) {
			input = input.substring(0,input.length() - 1);
		}
        return input;
    }
    public static String promptUser(String message,int mode) {
    	System.out.print(message);
        return scanner.nextLine();
    }
}
