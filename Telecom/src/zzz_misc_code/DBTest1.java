package zzz_misc_code;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import utils.CopyAndSerializationUtils;


public class DBTest1 {
	private static final String PW_FILE = "C:/Users/marco/gmailpassword.ser";
	public static void main(String[] args) throws Exception {
		
		String s = "00012230005500";
		s = s.replaceFirst("^0+(?!$)", "");
		System.out.println(s);
	}
	
}
