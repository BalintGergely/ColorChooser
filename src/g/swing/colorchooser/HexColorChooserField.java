package g.swing.colorchooser;

import java.awt.Color;
import java.awt.Font;
import java.util.Objects;

import javax.swing.JTextPane;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.StringContent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
/**
 * A color chooser that updates it's color model based on the hex color code written into it.
 * @author balintgergely
 *
 */
@SuppressWarnings("exports")
public class HexColorChooserField extends JTextPane implements ChangeListener{
	private static final long serialVersionUID = 1L;
	private ColorSelectionModel model;
	public HexColorChooserField(ColorSelectionModel model0){
		super();
		super.setDocument(new ListenerDoc());
		model = model0 == null ? new DefaultColorSelectionModel() : model0;
		model.addChangeListener(this);
		stateChanged(null);
	}
	public void setColorSelectionModel(ColorSelectionModel model0){
		Objects.requireNonNull(model0);
		model.removeChangeListener(this);
		model = model0;
		model.addChangeListener(this);
	}
	public ColorSelectionModel getModel(){
		return model;
	}
	@Override
	public void setDocument(Document doc){
		if(model != null){
			throw new UnsupportedOperationException();
		}
		super.setDocument(doc);
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		String str = Integer.toHexString(model.getSelectedColor().getRGB());
		while(str.length() < 7){
			str = "0"+str;
		}
		ListenerDoc doc = (ListenerDoc)super.getDocument();
		try {
			doc.replaceWhole(str.toUpperCase());
		} catch (BadLocationException e1) {
			throw new RuntimeException(e1);
		}
	}
	void docUpdate(ListenerDoc doc) throws BadLocationException{
		if(doc.getLength() > 8){
			doc.remove(8, doc.getLength()-8);
		}
		String str = doc.getText(0, doc.getLength());
		doc.setCharacterAttributes(0, str.length(), doc.getStyle("k"), true);
		if(str.length() >= 6 && str.length() <= 8){
			try{
				int rgb = Integer.parseUnsignedInt(str, 16);
				Color c = new Color(rgb,str.length() > 6);
				StyleConstants.setForeground(doc.getStyle("r"),new Color(c.getRed(),0,0));
				StyleConstants.setForeground(doc.getStyle("g"),new Color(0,c.getGreen(),0));
				StyleConstants.setForeground(doc.getStyle("b"),new Color(0,0,c.getBlue()));
				doc.setCharacterAttributes(str.length()-6, 2, doc.getStyle("r"), true);
				doc.setCharacterAttributes(str.length()-4, 2, doc.getStyle("g"), true);
				doc.setCharacterAttributes(str.length()-2, 2, doc.getStyle("b"), true);
				model.setSelectedColor(c);
			}catch(Exception e){
				//
			}
		}
	}
	private class ListenerDoc extends DefaultStyledDocument{
		private static final long serialVersionUID = 1L;
		ListenerDoc(){
			super(new StringContent(),new StyleContext());
			Style k = super.addStyle("k", null);
			StyleConstants.setFontFamily(k, Font.MONOSPACED);
			StyleConstants.setBackground(k, Color.WHITE);
			StyleConstants.setForeground(k, Color.BLACK);
			Style r = super.addStyle("r", k),g = super.addStyle("g", k),b = super.addStyle("b", k);
			StyleConstants.setForeground(r, Color.RED);
			StyleConstants.setForeground(g, Color.GREEN);
			StyleConstants.setForeground(b, Color.BLUE);
		}
		@Override
		public Style addStyle(String name,Style parent){
			throw new UnsupportedOperationException();
		}
		@Override
		public void removeStyle(String name){
			throw new UnsupportedOperationException();
		}
		void replaceWhole(String str) throws BadLocationException{
			if(getCurrentWriter() != Thread.currentThread()){
				writeLock();
				try{
					super.remove(0, super.getLength());
					super.insertString(0, str, getStyle("k"));
					docUpdate(this);
				}finally{
					writeUnlock();
				}
			}
		}
		@Override
		public void remove(int off,int len) throws BadLocationException{
			writeLock();
			try{
				super.remove(off, len);
				docUpdate(this);
			}finally{
				writeUnlock();
			}
		}
		@Override
		public void insertString(int off,String str,AttributeSet set) throws BadLocationException{
			try{
				Integer.parseUnsignedInt(str,16);
			}catch(Exception e){
				return;
			}
			if(str.startsWith("-")){
				return;
			}
			writeLock();
			try{
				super.insertString(off, str.toUpperCase(), getStyle("k"));
				docUpdate(this);
			}finally{
				writeUnlock();
			}
		}
	}
}
