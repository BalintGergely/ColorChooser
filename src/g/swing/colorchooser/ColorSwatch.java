package g.swing.colorchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

public class ColorSwatch extends JToggleButton{
	private static final long serialVersionUID = 1L;
	public static void main(String[] atgs){
		JFrame frame = new JFrame();
		frame.setLayout(new GridLayout(3,3,12,12));
		Color[] colors = new Color[]{
				Color.BLUE,Color.MAGENTA,Color.RED,
				Color.CYAN,new Color(0,0xff,0,0),Color.ORANGE,
				Color.GREEN,Color.PINK,Color.YELLOW
		};
		ButtonGroup group = new ButtonGroup();
		for(Color c : colors){
			ColorSwatch butt = new ColorSwatch(c);
			group.add(butt);
			frame.add(butt);
		}
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	public ColorSwatch(){
		this(new Color(0,0,0,0));
	}
	public ColorSwatch(Color color) {
		super();
		super.setBackground(color);
		super.setContentAreaFilled(false);
	}
	public ColorSwatch(Color color,ButtonModel model0) {
		super();
		super.setBackground(color);
		super.setModel(model0);
		super.setContentAreaFilled(false);
	}
	@Override
	public Dimension getPreferredSize(){
		if(isPreferredSizeSet()){
			return super.getPreferredSize();
		}
		return new Dimension(12,12);
	}
	@Override
	public void paintComponent(Graphics gr){
		int w = getWidth(),h = getHeight();
		Color cc = getBackground();
		if(cc.getAlpha() != 0xff){
			gr.setColor(Color.GRAY);
			gr.fillRect(0, 0, w, h);
			gr.setColor(Color.BLACK);
			int w1 = w < 14 ? w/2 : w/7*3,h1 = h < 14 ? h/2 : h/7*3;
			gr.fillRect(0,0,w1,h1);
			gr.fillRect(w-w1,h-h1,w1,h1);
			gr.setColor(Color.WHITE);
			gr.fillRect(w-w1,0,w1,h1);
			gr.fillRect(0,h-h1,w1,h1);
			gr.setColor(new Color(cc.getRGB()));
			gr.fillRect(w/3, h/3, w-(w/3*2), h-(h/3*2));
		}
		gr.setColor(cc);
		gr.fillRect(0, 0, w, h);
		if(isSelected()){
			gr.setColor(new Color(~cc.getRGB()));
			gr.drawRect(1, 1, w-3, h-3);
		}
		super.paintComponent(gr);
	}
}
