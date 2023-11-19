package image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class PicResize 
{
	private PicResize() {}
	
	public static BufferedImage getProfilePic(String filePath)
	{
		BufferedImage resizedImg = null;
		try
		{
			File file = new File(filePath);
			InputStream is = new FileInputStream(file);
			Image img = new ImageIcon(file.toString()).getImage();
			
			int resizeWidth = 80;
			int resizeHeight = 92;
			
			resizedImg = resize(is, resizeWidth, resizeHeight);
		}
		catch (IOException e) {e.printStackTrace();}
		
		return resizedImg;
	}
	
	private static BufferedImage resize(InputStream is, int width, int height) throws IOException
	{
		BufferedImage inputImage = ImageIO.read(is);
		BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
		Graphics2D gp2d = outputImage.createGraphics();
		gp2d.drawImage(inputImage, 0, 0, width, height, null);
		gp2d.dispose();
		
		return outputImage;
	}
}
