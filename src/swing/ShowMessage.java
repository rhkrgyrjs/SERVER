package swing;

import javax.swing.JOptionPane;

public class ShowMessage 
{
	private ShowMessage() {}
	public static void information(String title, String msg)
	{
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void warning(String title, String msg)
	{
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static boolean confirm(String title, String msg)
	{
		if (JOptionPane.showConfirmDialog(null, msg, title,JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION)
			return true;
		else
			return false;
	}
}
