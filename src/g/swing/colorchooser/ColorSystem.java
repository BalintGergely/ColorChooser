package g.swing.colorchooser;

public interface ColorSystem {
	public abstract int getLength();
	public abstract float round(int index,float value);
	public abstract String getLabel(int index);
	public abstract int min(int index);
	public abstract int max(int index);
	public abstract int toRGB(float... array);
	public abstract float[] fromRGB(int rgb,float[] array);
	public abstract int getAlphaIndex();
	public abstract int defaultValue(int index);
}
