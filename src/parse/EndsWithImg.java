package parse;

public class EndsWithImg 
{
	private EndsWithImg() {}
	
	public static boolean isJpg(String filePath) 
	{
        String lowerCaseFileName = filePath.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg");
	}

}
