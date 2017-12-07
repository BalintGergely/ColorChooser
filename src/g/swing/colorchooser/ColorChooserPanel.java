package g.swing.colorchooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import g.gf.ColorConstants;
import g.swing.colorchooser.ColorSystem;

public class ColorChooserPanel extends JPanel{
	public static void main(String[] atgs) throws InterruptedException{
		JFrame frame = new JFrame("JColorChooserPane"); //$NON-NLS-1$
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DefaultColorSelectionModel model = new DefaultColorSelectionModel(Color.BLACK);
		JTabbedPane pane = new JTabbedPane();
		frame.add(pane,BorderLayout.CENTER);
		frame.add(new HexColorChooserField(model), BorderLayout.PAGE_START);
		for(DefaultColorSystems sys : DefaultColorSystems.values()){
			pane.add(sys.name(), new ColorChooserPanel(sys,model));
		}
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		model.addChangeListener((ChangeEvent e) -> {
			synchronized(model){
				model.notifyAll();
			}
		});
		Color c = null;
		synchronized(model){
			while(true){
				model.wait(c == null ? 0 : 1000);
				if(c != null && c.equals(model.getSelectedColor())){
					System.out.println("Color set to rgba "+c.getRed()+" "+c.getGreen()+" "+c.getBlue()+" "+c.getAlpha());
					c = null;
				}else{
					c = model.getSelectedColor();
				}
			}
		}
	}
	private static final long serialVersionUID = 1L;
	ColorSelectionModel model;
	public final ColorSystem system;
	private ChangeListener listener;
	private ColorBox box;
	float[] values;
	ColorSlider[] sliders;
	public ColorChooserPanel(ColorSystem system0,ColorSelectionModel model0){
		super(new GridBagLayout());
		if(!(system0 instanceof Enum)){
			throw new IllegalArgumentException();
		}
		system = system0;
		model = model0 == null ? new DefaultColorSelectionModel() : model0;
		sliders = new ColorSlider[system.getLength()];
		values = new float[sliders.length];
		int i = 0,dim = 0;
		while(i < sliders.length){
			sliders[i] = new ColorSlider(i);
			dim = 
			i++;
		}
		for(ColorSlider sl : sliders){
			i = sl.init2();
			if(i > dim){
				dim = i;
			}
		}
		listener = (ChangeEvent e) -> {
			if(!Thread.holdsLock(values)){
				synchronized(values){
					Color col = model.getSelectedColor();
					int rgb = system.toRGB(values);
					if(rgb != col.getRGB()){
						system.fromRGB(col.getRGB(), values);
						importValues();
					}
				}
			}
		};
		GridBagConstraints con = new GridBagConstraints();
		con.weightx = 1;
		con.gridx = 0;
		con.fill = GridBagConstraints.BOTH;
		con.gridwidth = GridBagConstraints.REMAINDER;
		if(sliders.length > 2){
			con.gridy = 0;
			con.weighty = 1;
			add(box = new ColorBox(sliders[0],sliders[1],dim),con);
			con.gridwidth = 1;
			con.weighty = 0;
			con.gridwidth = 1;
			con.gridy = 1;
			int index = 0;
			if(sliders.length > 3){
				JRadioButton
							a = new JRadioButton(system.getLabel(0)),
							b = new JRadioButton(system.getLabel(1)),
							c = new JRadioButton(system.getLabel(2));
				index = 3;
				ButtonGroup group = new ButtonGroup();
				group.add(a);
				a.addActionListener((ActionEvent e) -> box.setZ(sliders[0]));
				group.add(b);
				b.addActionListener((ActionEvent e) -> box.setZ(sliders[1]));
				group.add(c);
				c.addActionListener((ActionEvent e) -> box.setZ(sliders[2]));
				add(a,con);
				con.gridy += 2;
				add(b,con);
				con.gridy += 2;
				add(c,con);
				con.gridy += 2;
				c.setSelected(true);
			}
			while(index < sliders.length){
				add(new JLabel(system.getLabel(index)),con);
				con.gridy += 2;
				index++;
			}
			con.gridwidth = GridBagConstraints.REMAINDER;
		}
		con.gridy = 2;
		for(ColorSlider sl : sliders){
			add(sl,con);
			con.gridy += 2;
		}
		con.gridy = 1;
		con.gridx = 1;
		con.gridwidth = 1;
		for(ColorSlider sl : sliders){
			add(new JSpinner(sl.spinner),con);
			con.gridy += 2;
		}
		model.addChangeListener(listener);
		listener.stateChanged(null);
	}
	void importValues(){
		int i = 0;
		while(i < values.length){
			sliders[i].spinner.setValue(Float.valueOf(values[i]));
			i++;
		}
	}
	void exportValues(){
		model.setSelectedColor(new Color(system.toRGB(values),true));
	}
	public void setColorSelectionModel(ColorSelectionModel model0){
		model.removeChangeListener(listener);
		model = model0 == null ? new DefaultColorSelectionModel() : model0;
		model.addChangeListener(listener);
		listener.stateChanged(null);
	}
	public ColorSelectionModel getColorSelectionModel(){
		return model;
	}
	private class ColorSlider extends JPanel implements MouseListener,MouseMotionListener{
		private static final long serialVersionUID = 1L;
		SpinnerNumberModel spinner;
		BufferedImage buffer;
		final int index;
		int v = -1;
		ColorSlider(int index0){
			spinner = new SpinnerNumberModel(
					Float.valueOf(system.defaultValue(index0)),
					Float.valueOf(system.min(index0)),
					Float.valueOf(system.max(index0)),
					Float.valueOf(1));
			index = index0;
			boolean isAlpha = system.getAlphaIndex() == index;
			buffer = new BufferedImage(system.max(index0)-system.min(index0),
					isAlpha ? 3 : 1,BufferedImage.TYPE_3BYTE_BGR);
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		int init2(){
			ChangeListener ls = (ChangeEvent e) -> update();
			for(ColorSlider sl : sliders){
				if(sl != this && system.getAlphaIndex() != sl.index){
					sl.spinner.addChangeListener(ls);
				}
			}
			spinner.addChangeListener((ChangeEvent e) -> {
				boolean bol = Thread.holdsLock(values);
				synchronized(values){
					float a = spinner.getNumber().floatValue(),f = system.round(index, a);
					if(f != a){
						spinner.setValue(Float.valueOf(f));
					}
					values[index] = f;
					if(!bol){
						exportValues();
					}
				}
				repaint();
			});
			return buffer.getWidth();
		}
		void update(){
			v = -1;
			repaint();
		}
		@Override
		public Dimension getMinimumSize(){
			return new Dimension(16,16);
		}
		@Override
		public Dimension getMaximumSize(){
			return new Dimension(buffer.getWidth()+2,16);
		}
		@Override
		public Dimension getPreferredSize(){
			return new Dimension(system.max(index)+2,16);
		}
		private void repaintBuffer(int v0,float... values0){
			int i = 0,min = system.min(index),max = system.max(index),v1 = Math.min(buffer.getWidth(), v0);
			while(i < v1){
				values0[index] = valForPos(i,min,max,v1);
				int rgb = system.toRGB(values0);
				if(system.getAlphaIndex() == index){
					buffer.setRGB(i, 1, ColorConstants.blend(0xff777777, rgb));
					buffer.setRGB(i, 2, ColorConstants.blend(0xff000000, rgb));
					rgb = ColorConstants.blend(0xffffffff, rgb);
				}
				buffer.setRGB(i, 0, rgb);
				i++;
			}
			v = v0;
		}
		@Override
		public void paintComponent(Graphics gr){
			int w = getWidth()-2,h = getHeight()-2;
			float[] val = values.clone();
			if(w != v && (w < buffer.getWidth() || v < buffer.getWidth())){
				repaintBuffer(w,val);
			}
			gr.drawImage(buffer,1,1,Math.max(buffer.getWidth(),w),h,null);
			int x = posForVal(spinner.getNumber().floatValue(),system.min(index),system.max(index),w)+1;
			gr.setXORMode(Color.WHITE);
			gr.setColor(Color.BLACK);
			gr.drawLine(x, 1, x, (int)Math.floor(h*0.17)+1);
			if(h >= 3){
				gr.drawLine(x, (int)Math.floor(h*0.83)+1, x, h);
			}
			gr.setPaintMode();
			gr.drawRect(0, 0, w+1, h+1);
		}
		void event(float val){
			spinner.setValue(Float.valueOf(system.round(index,val)));
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			event(valForPos(e.getX()-1,system.min(index),system.max(index),getWidth()-2));
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			//
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			//
		}
		@Override
		public void mousePressed(MouseEvent e) {
			mouseDragged(e);
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			//
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			//
		}
		@Override
		public void mouseExited(MouseEvent e) {
			//
		}
	}
	private class ColorBox extends JPanel implements MouseListener, MouseMotionListener, ChangeListener{
		private static final long serialVersionUID = 1L;
		private ColorSlider x,y;
		private BufferedImage buffer;
		private int vw = -1,vh = -1;
		public ColorBox(ColorSlider x0,ColorSlider y0,int dim){
			x = x0;
			y = y0;
			for(ColorSlider sl : sliders){
				if(system.getAlphaIndex() != sl.index){
					sl.spinner.addChangeListener(this);
				}
			}
			buffer = new BufferedImage(dim,dim,BufferedImage.TYPE_3BYTE_BGR);
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		void setZ(ColorSlider z){
			if(x == z){
				x = null;
			}else if(y == z){
				y = null;
			}else{
				z = x;
				x = y;
				y = z;
				stateChanged(null);
				return;
			}
			for(ColorSlider sl : sliders){
				if(sl != x && sl != y && sl != z){
					if(x == null){
						x = sl;
					}
					if(y == null){
						y = sl;
					}
					break;
				}
			}
			if(system.max(x.index)-system.min(x.index) > system.max(y.index)-system.min(y.index)){
				z = x;
				x = y;
				y = z;
			}
			stateChanged(null);
		}
		@Override
		public Dimension getMinimumSize(){
			return new Dimension(16,16);
		}
		@Override
		public Dimension getMaximumSize(){
			int i = buffer.getWidth()+2;
			return new Dimension(i,i);
		}
		@Override
		public Dimension getPreferredSize(){
			return new Dimension(system.max(x.index)+2,system.max(y.index)+2);
		}
		public void stateChanged(ChangeEvent e) {
			Object so = e != null ? e.getSource() : null;
			if(so != x.spinner && so != y.spinner){
				vw = -1;
			}
			repaint();
		}
		private void repaintBuffer(int vw0,int vh0,float... values0){
			int x0 = 0,y0,
				xmin = system.min(x.index),
				xmax = system.max(x.index),
				ymin = system.min(y.index),
				ymax = system.max(y.index),
				vw1 = Math.min(buffer.getWidth(), vw0),
				vh1 = Math.min(buffer.getHeight(), vh0);
			while(x0 < vw1){
				values0[x.index] = valForPos(x0,xmin,xmax,vw1);
				y0 = 0;
				while(y0 < vh1){
					values0[y.index] = valForPos(y0,ymin,ymax,vh1);
					buffer.setRGB(x0, y0, system.toRGB(values0));
					y0++;
				}
				x0++;
			}
			vw = vw0;
			vh = vh0;
		}
		@Override
		public void paintComponent(Graphics gr){
			int w = getWidth()-2,h = getHeight()-2;
			float[] val = values.clone();
			if(	(w != vw && (w < buffer.getWidth() || vw < buffer.getWidth())) ||
				(h != vh && (h < buffer.getWidth() || vh < buffer.getWidth()))){
				repaintBuffer(w,h,val);
			}
			gr.drawImage(buffer, 1, 1, Math.max(w, buffer.getWidth()), Math.max(h, buffer.getHeight()), null);
			int x0 = posForVal(x.spinner.getNumber().floatValue(),system.min(x.index),system.max(x.index),w),
				y0 = posForVal(y.spinner.getNumber().floatValue(),system.min(y.index),system.max(y.index),h);
			gr.setColor(Color.BLACK);
			gr.setXORMode(Color.WHITE);
			gr.drawLine(x0+1,	y0-1,	x0+1,	y0-5);
			gr.drawLine(x0+1,	y0+3,	x0+1,	y0+7);
			gr.drawLine(x0-1,	y0+1,	x0-5,	y0+1);
			gr.drawLine(x0+3,	y0+1,	x0+7,	y0+1);
			gr.setPaintMode();
			gr.setColor(new Color(~model.getSelectedColor().getRGB()));
			gr.drawRect(0, 0, w+1, h+1);
		}
		public void mouseDragged(MouseEvent e) {
			x.event(valForPos(e.getX()-1,system.min(x.index),system.max(x.index),getWidth()-2));
			y.event(valForPos(e.getY()-1,system.min(y.index),system.max(y.index),getHeight()-2));
		}
		public void mouseMoved(MouseEvent e) {
			//
		}
		public void mouseClicked(MouseEvent e) {
			//
		}
		public void mousePressed(MouseEvent e) {
			mouseDragged(e);
		}
		public void mouseReleased(MouseEvent e) {
			//
		}
		public void mouseEntered(MouseEvent e) {
			//
		}
		public void mouseExited(MouseEvent e) {
			//
		}
	}
	static float valForPos(int x,int min,int max,int v){
		if(v <= max){
			min = 0;
		}else if(v < max-min){
			min = max-v;
		}
		int range = max-min;
		float val = (x * (range/(float)v))+min;
		if(val > max){
			return max;
		}
		if(val < min){
			return min;
		}
		return val;
	}
	static int posForVal(float val,int min,int max,int v){
		if(v <= max){
			min = 0;
		}else if(v < max-min){
			min = max-v;
		}
		int range = max-min;
		int x = (int)Math.floor(((val-min) / (range/(float)v)));
		if(x >= v){
			return v-1;
		}
		return x;
	}
}
