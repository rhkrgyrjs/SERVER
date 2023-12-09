package elo;

public class EloCalculator 
{
	public static int calc(int myRating, int opRating, String win)
	{
		final double K = 32;
				
        double R1 = Math.pow(10, myRating / 400.0);
        double R2 = Math.pow(10, opRating / 400.0);

        double E1 = R1 / (R1 + R2);
        double E2 = R2 / (R1 + R2);
        
        double res1 = 1;
        double res2 = 0;
        
        if (win.equals("w"))
        {
        	res1 = 1;
        	res2 = 0;
        }
        else if (win.equals("l"))
        {
        	res1 = 0;
        	res2 = 1;
        }
        else if (win.equals("d"))
        {
        	res1 = 0.5;
        	res2 = 0.5;
        }
        
        int newRating = (int) (myRating + (K * (res1 - E1)));
        
        return newRating;
	}
}
