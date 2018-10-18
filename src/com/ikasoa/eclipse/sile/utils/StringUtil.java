package com.ikasoa.eclipse.sile.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

@SuppressWarnings("deprecation")
public class StringUtil {

	private final static String encode = "UTF-8";

	public static String InputStream2String(InputStream in) {
		byte[] b = new byte[1024];
		int len = 0;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			while ((len = in.read(b)) > 0)
				out.write(b, 0, len);
			return out.toString(encode);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static InputStream String2InputStream(String str) {
		try {
			return new StringBufferInputStream(str);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
