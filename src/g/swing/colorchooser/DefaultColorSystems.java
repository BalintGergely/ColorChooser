package g.swing.colorchooser;

import g.gf.ColorConstants;

public enum DefaultColorSystems implements ColorSystem{
	RGBA("Red","Green","Blue","Alpha"){
		@Override
		public int max(int index) {
			return 0xff;
		}
		@Override
		public int toRGB(float... array) {
			int r = Math.round(array[0]),g = Math.round(array[1]),b = Math.round(array[2]),a = Math.round(array[3]);
			if(r < 0 || r > 0xff || g < 0 || g > 0xff || b < 0 || b > 0xff || a < 0 || a > 0xff){
				throw new IllegalArgumentException(r+" "+g+" "+b+" "+a);
			}
			return a << 24 | r << 16 | g << 8 | b;
		}
		@Override
		public float[] fromRGB(int rgb, float[] array) {
			if(array == null || array.length < 4){
				array = new float[4];
			}
			array[0] = ColorConstants.getRed(rgb);
			array[1] = ColorConstants.getGreen(rgb);
			array[2] = ColorConstants.getBlue(rgb);
			array[3] = ColorConstants.getAlpha(rgb);
			return array;
		}
		@Override
		public int getAlphaIndex(){
			return 3;
		}
		@Override
		public int defaultValue(int index) {
			return index == 3 ? 0xff : 0;
		}
		public float round(int index,float value){
			return Math.round(value);
		}
	},
	CMYK("Cyan","Magenta","Yellow","Black","Alpha"){

		@Override
		public int max(int index) {
			return 0xff;
		}
		@Override
		public int toRGB(float... array) {
			float c = array[0]/255,
				m = array[1]/255,
				y = array[2]/255,
				k = array[3]-254;
			int a = Math.round(array[4]),
				r = Math.round(1.0f + c * k - k - c),
				g = Math.round(1.0f + m * k - k - m),
	        	b = Math.round(1.0f + y * k - k - y);
			if(r < 0 || r > 0xff || g < 0 || g > 0xff || b < 0 || b > 0xff || a < 0 || a > 0xff){
				throw new IllegalArgumentException();
			}
			return a << 24 | r << 16 | g << 8 | b;
		}

		@Override
		public float[] fromRGB(int rgb, float[] array) {
			if(array == null || array.length < 5){
				array = new float[5];
			}
			int r = ColorConstants.getRed(rgb),
				g = ColorConstants.getGreen(rgb),
				b = ColorConstants.getBlue(rgb),
				a = ColorConstants.getAlpha(rgb);
			float max = max(r, g, b);
			if(max > 0f){
				array[0] = (1.0f - r / max)*255;
				array[1] = (1.0f - g / max)*255;
				array[2] = (1.0f - b / max)*255;
			}else{
				array[0] = 0.0f;
				array[1] = 0.0f;
				array[2] = 0.0f;
			}
			array[3] = 255 - max;
			array[4] = a;
			return array;
		}
		@Override
		public int getAlphaIndex(){
			return 4;
		}
		@Override
		public int defaultValue(int index) {
			return index == 3 ? 0xff : 0;
		}
		public float round(int index,float value){
			return Math.round(value);
		}
	},
	HSV("Hue","Saturation","Brightness","Transparency"){
		@Override
		public int min(int index){
			return (index == 1) ? -100 : 0;
		}
		@Override
		public int max(int index) {
			return (index == 0) ? 360 : 100;
		}
		@Override
		public int toRGB(float... array) {
			float hu = array[0]/360,
				sa = array[1]/100f,//Difference between max and min
				br = array[2]*2.55f,//The max of r g b
				al = array[3]*2.55f;
			if(sa < 0){
				sa = -sa;
				hu += 0.5f;
			}
			int r,g,b;
			if(sa != 0){
				hu = (hu - (float)Math.floor(hu)) * 6.0f;
				int fh = (int)Math.floor(hu);
				float
					f = hu - (float)java.lang.Math.floor(hu),
					p = br * (1.0f - sa) + 0.5f,
					md = br * (1.0f - sa * (fh%2 == 0 ? (1.0f - f) : f)) + 0.5f;
				br += 0.5f;
				switch(fh){
				case 0:
	                r = (int)br;
	                g = (int)md;
	                b = (int)p;
	                break;
	            case 1:
	                r = (int)md;
	                g = (int)br;
	                b = (int)p;
	                break;
	            case 2:
	                r = (int)p;
	                g = (int)br;
	                b = (int)md;
	                break;
	            case 3:
	                r = (int)p;
	                g = (int)md;
	                b = (int)br;
	                break;
	            case 4:
	                r = (int)md;
	                g = (int)p;
	                b = (int)br;
	                break;
	            case 5:
	                r = (int)br;
	                g = (int)p;
	                b = (int)md;
	                break;
				default:
					throw new IllegalArgumentException();
				}
			}else{
				r = g = b = (int)(br + 0.5f);
			}
			return ((~((int)al) & 0xff) << 24) | (r << 16) | (g << 8) | b;
		}
		@Override
		public float[] fromRGB(int rgb, float[] array) {
			if(array == null || array.length < 4){
				array = new float[4];
			}
			int r = ColorConstants.getRed(rgb),
				g = ColorConstants.getGreen(rgb),
				b = ColorConstants.getBlue(rgb),
				cmax = r > g ? r : g,
				cmin = b < g ? b : g;
			if(cmax < b){
				cmax = b;
			}
			if(cmin > r){
				cmin = r;
			}
			float sat = cmax > 0 ? (cmax-cmin)/(float)cmax : 0,br = cmax/2.55f;
			if((Float.floatToIntBits(array[1]) >> 31) != 0){
				sat = -sat;
			}
			if(sat != 0){
				float	rc = (cmax - r)/(float)(cmax - cmin),
						gc = (cmax - g)/(float)(cmax - cmin),
						bc = (cmax - b)/(float)(cmax - cmin),
						hue;
				if(r == cmax){
					hue = bc - gc;
					if(hue < 0){
						hue += 6.0f;
					}
				}else if(g == cmax){
					hue = 2.0f + rc - bc;
				}else{
					hue = 4.0f + gc - rc;
				}
				if(sat < 0f){
					hue += 3f;
					if(hue > 6f){
						hue -= 6f;
					}
				}
				array[0] = hue * 60f;
			}
			array[1] = sat*100;
			array[2] = br;
			array[3] = ColorConstants.getAlpha(~rgb)/2.55f;
			return array;
		}
		@Override
		public int getAlphaIndex(){
			return 3;
		}
		@Override
		public int defaultValue(int index) {
			return 0;
		}
		public float round(int index,float value){
			return value;
		}
	},
	HSL("Hue","Saturation","Lightness","Transparency"){
		@Override
		public int min(int index){
			return (index == 1) ? -100 : 0;
		}
		@Override
		public int max(int index) {
			if(index == 0){
				return 360;
			}
			if(index == 2){
				return 200;
			}
			return 100;
		}
		@Override
		public int toRGB(float... array) {
			float hu = array[0]/360,
				sa = array[1]/100f,
				li = array[2]/200f,
				al = array[3]*2.55f;
			int r,g,b;
			if(sa < 0){
				sa = -sa;
				hu += 0.5f;
			}
			if(sa != 0){
				hu = (hu - (float)Math.floor(hu)) * 6.0f;
				int fh = (int)Math.floor(hu);
				float	f = hu - (float)java.lang.Math.floor(hu),
						q = li + sa * ((li > 0.5f) ? 1.0f - li : li),
						p = 2.0f * li - q,
						md = p + (q - p) * (fh%2 == 1 ? (1.0f - f) : f);
				q = q * 255f + 0.5f;
				p = p * 255f + 0.5f;
				md = md*255f + 0.5f;
				switch(fh){
				case 0:
	                r = (int)q;
	                g = (int)md;
	                b = (int)p;
	                break;
	            case 1:
	                r = (int)md;
	                g = (int)q;
	                b = (int)p;
	                break;
	            case 2:
	                r = (int)p;
	                g = (int)q;
	                b = (int)md;
	                break;
	            case 3:
	                r = (int)p;
	                g = (int)md;
	                b = (int)q;
	                break;
	            case 4:
	                r = (int)md;
	                g = (int)p;
	                b = (int)q;
	                break;
	            case 5:
	                r = (int)q;
	                g = (int)p;
	                b = (int)md;
	                break;
				default:
					throw new IllegalArgumentException();
				}
			}else{
				r = g = b = (int)(li * 255f + 0.5f);
			}
			return ((~((int)al) & 0xff) << 24) | (r << 16) | (g << 8) | b;
		}
		@Override
		public float[] fromRGB(int rgb, float[] array) {
			if(array == null || array.length < 4){
				array = new float[4];
			}
			int r = ColorConstants.getRed(rgb),
				g = ColorConstants.getGreen(rgb),
				b = ColorConstants.getBlue(rgb),
				cmax = r > g ? r : g,
				cmin = b < g ? b : g;
			if(cmax < b){
				cmax = b;
			}
			if(cmin > r){
				cmin = r;
			}
			float summa = cmax + cmin,
					sat = summa == 0 ? 0 : (cmax - cmin)/(summa > 256f ? 512 - summa : summa);
			if((Float.floatToIntBits(array[1]) >> 31) != 0){
				sat = -sat;
			}
			if(sat != 0){
				float	rc = (cmax - r)/(float)(cmax - cmin),
						gc = (cmax - g)/(float)(cmax - cmin),
						bc = (cmax - b)/(float)(cmax - cmin),
						hue;
				if(r == cmax){
					hue = bc - gc;
					if(hue < 0){
						hue += 6.0f;
					}
				}else if(g == cmax){
					hue = 2.0f + rc - bc;
				}else{
					hue = 4.0f + gc - rc;
				}
				if(sat < 0){
					hue += 3f;
					if(hue > 6f){
						hue -= 6f;
					}
				}
				array[0] = hue*60f;
			}
			array[1] = sat * 100;
			array[2] = summa / 2.55f;
			array[3] = ColorConstants.getAlpha(~rgb)/2.55f;
			
			return array;
		}
		@Override
		public int getAlphaIndex(){
			return 3;
		}
		@Override
		public int defaultValue(int index) {
			return 0;
		}
		public float round(int index,float value){
			return value;
		}
	};
	private DefaultColorSystems(String... labels0){
		labels = labels0;
	}
	private final String[] labels;
	public int getLength(){
		return labels.length;
	}
	public String getLabel(int index){
		return labels[index];
	}
	public int min(int index){
		return 0;
	}
	public static float max(float a,float b,float c){
		if(a > b){
			b = a;
		}
		if(b > c){
			return b;
		}
		return c;
	}
}
