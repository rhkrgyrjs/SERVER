package hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 
{
	private SHA256() {}
	
	public static String toString(String text)
	{
		String result = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(text.getBytes());
			result = bytesToHex(md.digest());
		}
		catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		return result;
	}
	
	private static String bytesToHex(byte[] bytes)
	{
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {builder.append(String.format("%02x", b));}
		return builder.toString();
	}

}
