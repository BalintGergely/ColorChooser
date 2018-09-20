package g.gf;

import static java.lang.Math.*;

public final class ColorConstants {
	/**
	 * The maximum hue a color can have.
	 */
	public static int HUES = 0x5FA;
	private ColorConstants() {}
	public static int blend(int a, int b){
		int bA = (b >> 24) & 0xff;
		if(bA == 0){
			return a;
		}
		int aA = (a >> 24) & 0xff;
		if(aA == 0){
			return b;
		}
		int aR = (a >> 16) & 0xff;
		int aG = (a >> 8) & 0xff;
		int aB = a & 0xff;
		int bR = (b >> 16) & 0xff;
		int bG = (b >> 8) & 0xff;
		int bB = b & 0xff;
		if(aA+bA > 0xff){
			aA = 0xff-bA;
		}
		aR = ((aR * aA) + (bR * bA)) / (aA + bA);
		aG = ((aG * aA) + (bG * bA)) / (aA + bA);
		aB = ((aB * aA) + (bB * bA)) / (aA + bA);
		return (aA + bA) << 24 | aR << 16 | aG << 8 | aB;
	}
	public static int specialBlend(int a, int b){
		int bA = (b >> 24) & 0xff;
		if(bA == 0){
			return a;
		}
		int aA = (a >> 24) & 0xff;
		if(aA == 0){
			return b;
		}
		int aR = (a >> 16) & 0xff;
		int aG = (a >> 8) & 0xff;
		int aB = a & 0xff;
		int bR = (b >> 16) & 0xff;
		int bG = (b >> 8) & 0xff;
		int bB = b & 0xff;
		if(aA+bA > 0xff){
			aA = 0xff-bA;
		}
		aR = (int) floor(sqrt(((aR*aR * aA) + (bR*bR * bA)) / (double)(aA + bA)));
		aG = (int) floor(sqrt(((aG*aG * aA) + (bG*bG * bA)) / (double)(aA + bA)));
		aB = (int) floor(sqrt(((aB*aB * aA) + (bB*bB * bA)) / (double)(aA + bA)));
		return (aA + bA) << 24 | aR << 16 | aG << 8 | aB;
	}
	public static int oSub(int a,int b){
	    byte aA = (byte)((a >> 24) & 0xff);
	    byte aR = (byte)((a >> 16) & 0xff);
	    byte aG = (byte)((a >> 8) & 0xff);
	    byte aB = (byte)(a & 0xff);
	    
	    byte bA = (byte)((b >> 24) & 0xff);
	    byte bR = (byte)((b >> 16) & 0xff);
	    byte bG = (byte)((b >> 8) & 0xff);
	    byte bB = (byte)(b & 0xff);
	    
	    byte A = (byte)(bA-aA);
	    byte R = (byte)(bR-aR);
	    byte G = (byte)(bG-aG);
	    byte B = (byte)(bB-aB);
	    
	    return A << 24 | R << 16 | G << 8 | B;
	}
	public static int oAdd(int a,int b){
	    byte aA = (byte)((a >> 24) & 0xff);
	    byte aR = (byte)((a >> 16) & 0xff);
	    byte aG = (byte)((a >> 8) & 0xff);
	    byte aB = (byte)(a & 0xff);
	    
	    byte bA = (byte)((b >> 24) & 0xff);
	    byte bR = (byte)((b >> 16) & 0xff);
	    byte bG = (byte)((b >> 8) & 0xff);
	    byte bB = (byte)(b & 0xff);
	    
	    byte A = (byte)(bA+aA);
	    byte R = (byte)(bR+aR);
	    byte G = (byte)(bG+aG);
	    byte B = (byte)(bB+aB);
	    
	    return A << 24 | R << 16 | G << 8 | B;
	}
	public static int setAlpha(int color,int value){
		return (color & 0x00ffffff) | ((value & 0xff) << 24);
	}
	public static int getAlpha(int color){
		return (color >>> 24) & 0xff;
	}
	public static int setRed(int color,int value){
		return (color & 0xff00ffff) | ((value & 0xff) << 16);
	}
	public static int getRed(int color){
		return (color >>> 16) & 0xff;
	}
	public static int setGreen(int color,int value){
		return (color & 0xffff00ff) | ((value & 0xff) << 8);
	}
	public static int getGreen(int color){
		return (color >>> 8) & 0xff;
	}
	public static int setBlue(int color,int value){
		return (color & 0xffffff00) | (value & 0xff);
	}
	public static int getBlue(int color){
		return color & 0xff;
	}
	public static int combine(int r,int g,int b,int a){
		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}
}
