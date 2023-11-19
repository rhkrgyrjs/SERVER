package image;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Blob 
{
	private Blob() {}
	public static byte[] toByteArray(BufferedImage bi, String format)
	{
		byte[] bytes = null;
		try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ImageIO.write(bi, format, baos);
	        bytes = baos.toByteArray();} catch (IOException e) {e.printStackTrace();}
		return bytes;
	}

	    // convert byte[] to BufferedImage
	    public static BufferedImage toBufferedImage(byte[] bytes)
	    {
	    	BufferedImage bi = null;
	    	try {
	        InputStream is = new ByteArrayInputStream(bytes);
	        bi = ImageIO.read(is);
	        } catch (IOException e) {e.printStackTrace();}
	    	return bi;
	    }


}
