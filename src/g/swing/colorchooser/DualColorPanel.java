package g.swing.colorchooser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import g.gf.ColorConstants;
import g.swing.colorchooser.ColorChooserPanel;
import g.swing.colorchooser.ColorSwatch;
import g.swing.colorchooser.DefaultColorSystems;
import g.swing.colorchooser.HexColorChooserField;

public class DualColorPanel extends JPanel implements ColorSelectionModel{
	public static void main(String[] atgs){
		JFrame frame = new JFrame(DualColorPanel.class.getName()); //$NON-NLS-1$
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Robot rob = null;
		try{
			rob = new Robot();
		}catch(Exception e){
			e.printStackTrace();
		}
		DualColorPanel pane = new DualColorPanel(rob);
		JCheckBox topC = new JCheckBox("Always on top");
		frame.add(topC,BorderLayout.PAGE_START);
		topC.addActionListener((ActionEvent e) -> frame.setAlwaysOnTop(topC.isSelected()));
		frame.add(pane,BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	private static final long serialVersionUID = 1L;
	private Consumer<ColorSelectionModel> pipette = null;
	private ColorSwatch lbutton,rbutton,mixButton,spMixButton;
	private final DefaultColorSelectionModel left,right;
	private Vector<ChangeListener> ls;
	private boolean isR;
	public Color getColor(boolean isR0){
		return isR0 ? right.getSelectedColor() : left.getSelectedColor();
	}
	public Color getPrimary(){
		return isR ? right.getSelectedColor() : left.getSelectedColor();
	}
	public Color getSecondary(){
		return isR ? left.getSelectedColor() : right.getSelectedColor();
	}
	public void setPrimary(boolean isR0){
		if(isR != isR0){
			isR = isR0;
			mixButton.setText(isR0 ? "→" : "←");
			spMixButton.setText(isR0 ? "⇒" :  "⇐");
			fireChangedEvent(null);
		}
	}
	public void setColor(boolean isR0,Color c){
		if(isR0){
			right.setSelectedColor(c);
		}else{
			left.setSelectedColor(c);
		}
	}
	public DualColorPanel(Robot rob) {
		this(rob == null ? null : (ColorSelectionModel mod) -> {
			pipetteTool(rob,mod);
		});
	}
	public DualColorPanel(){
		this((Consumer<ColorSelectionModel>)null);
	}
	public DualColorPanel(Consumer<ColorSelectionModel> pipe){
		super(new GridBagLayout());
		ls = new Vector<>(5);
		left = new DefaultColorSelectionModel(Color.BLACK);
		right = new DefaultColorSelectionModel(Color.WHITE);
		left.addChangeListener((ChangeEvent e) -> fireChangedEvent(e));
		right.addChangeListener((ChangeEvent e) -> fireChangedEvent(e));
		GridBagConstraints con = new GridBagConstraints();
		ButtonGroup group = new ButtonGroup();
		lbutton = new ColorSwatch(Color.BLACK);
		lbutton.setForeground(Color.WHITE);
		lbutton.setText("L");
		Dimension dim = new Dimension(48,48);
		lbutton.setMinimumSize(dim);
		lbutton.setPreferredSize(dim);
		rbutton = new ColorSwatch(Color.WHITE);
		rbutton.setForeground(Color.BLACK);
		rbutton.setText("R");
		rbutton.setMinimumSize(dim);
		rbutton.setPreferredSize(dim);
		mixButton = new ColorSwatch(Color.WHITE,new DefaultButtonModel());
		spMixButton = new ColorSwatch(Color.WHITE,new DefaultButtonModel());
		dim = new Dimension(24,24);
		mixButton.setMinimumSize(dim);
		mixButton.setPreferredSize(dim);
		spMixButton.setMinimumSize(dim);
		spMixButton.setPreferredSize(dim);
		
		JButton swapB = new JButton("↔");
		swapB.addActionListener((ActionEvent e) -> {
			Color l = left.getSelectedColor(),r = right.getSelectedColor();
			left.setSelectedColor(r);
			right.setSelectedColor(l);
		});
		
		group.add(lbutton);
		group.add(rbutton);
		lbutton.setSelected(true);
		mixButton.setText("←");
		spMixButton.setText("⇐");
		lbutton.addActionListener((ActionEvent e) -> {
			setPrimary(false);
		});
		rbutton.addActionListener((ActionEvent e) -> {
			setPrimary(true);
		});
		mixButton.addActionListener((ActionEvent e) -> {
			setSelectedColor(mixButton.getBackground());
		});
		spMixButton.addActionListener((ActionEvent e) -> {
			setSelectedColor(spMixButton.getBackground());
		});
		con.gridheight = 2;
		con.weightx = 1;
		super.add(lbutton,con);
		con.gridx = 2;
		super.add(rbutton,con);
		con.gridheight = 1;
		con.weightx = 0;
		con.gridx = 1;
		con.fill = GridBagConstraints.BOTH;
		super.add(mixButton,con);
		con.gridy = 1;
		con.gridx = 1;
		super.add(spMixButton, con);
		con.weightx = 1;
		con.gridy++;
		con.gridx = 0;
		super.add(new HexColorChooserField(left),con);
		con.gridx = 1;
		con.weightx = 0;
		super.add(swapB,con);
		con.weightx = 1;
		con.gridx = 2;
		super.add(new HexColorChooserField(right),con);
		con.gridwidth = GridBagConstraints.REMAINDER;
		con.gridx = 0;
		con.gridy++;
		if(pipe != null){
			pipette = pipe;
			JButton pip = new JButton("Pipette");
			super.add(pip,con);
			pip.addActionListener((ActionEvent e) -> {
				pipette.accept(this);
			});
			con.gridy++;
		}
		CardLayout lay = new CardLayout();
		JPanel pane = new JPanel(lay);
		for(DefaultColorSystems sys : DefaultColorSystems.values()){
			pane.add(new ColorChooserPanel(sys,this),sys.toString());
		}
		JComboBox<DefaultColorSystems> cb = new JComboBox<>(DefaultColorSystems.values());
		cb.setEditable(false);
		add(cb,con);
		con.gridy++;
		con.weighty = 1;
		add(pane,con);
		cb.addItemListener((ItemEvent e) -> {
			lay.show(pane, e.getItem().toString());
		});
		super.add(pane, con);
	}
	
	public Color getSelectedColor() {
		return isR ? right.getSelectedColor() : left.getSelectedColor();
	}
	public void setSelectedColor(Color c) {
		(isR ? right : left).setSelectedColor(c);
	}
	private void fireChangedEvent(ChangeEvent e){
		if(e != null){
			Color c = null;
			boolean isr = false;
			if(e.getSource() == right){
				c = right.getSelectedColor();
				isr = true;
			}
			if(e.getSource() == left){
				c = left.getSelectedColor();
			}
			if(c != null){
				if(isr){
					rbutton.setColor(c);
				}else{
					lbutton.setColor(c);
				}
			}
		}
		mixButton.setColor(new Color(
				ColorConstants.blend(
						(isR ? right : left).getSelectedColor().getRGB(),
						(isR ? left : right).getSelectedColor().getRGB()),true
				));
		spMixButton.setColor(new Color(
				ColorConstants.specialBlend(
						(isR ? right : left).getSelectedColor().getRGB(),
						(isR ? left : right).getSelectedColor().getRGB()),true
				));
		ChangeEvent ne = new ChangeEvent(this);
		for(ChangeListener lss : ls){
			lss.stateChanged(ne);
		}
	}
	public void addChangeListener(ChangeListener listener) {
		if(listener != null){
			ls.add(listener);
		}
	}
	public void removeChangeListener(ChangeListener listener) {
		ls.remove(listener);
	}
	public synchronized static void pipetteTool(Robot rob,ColorSelectionModel model){
		Objects.requireNonNull(rob);
		Objects.requireNonNull(model);
		Toolkit tk = Toolkit.getDefaultToolkit();
		JDialog dialog = new JDialog();
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.setBackground(new Color(0,0,0,0));
		BufferedImage cur = new BufferedImage(0x10,0x10,BufferedImage.TYPE_INT_ARGB);
		Point p = new Point(0x10,0x10);
		class Pipette extends JPanel implements MouseListener,MouseMotionListener{
			private static final long serialVersionUID = 1L;
			@Override
			public void mouseMoved(MouseEvent e) {
				Color c = rob.getPixelColor(e.getX(),e.getY());
				model.setSelectedColor(c);
				fillCur(c);
			}
			@Override
			public void mousePressed(MouseEvent e) {
				dialog.dispose();
				model.setSelectedColor(rob.getPixelColor(e.getX(), e.getY()));
			}
			private void fillCur(Color c){
				int i = 0,rgb = c.getRGB();
				while(i < 0xf){
					cur.setRGB(i,	0, rgb);
					cur.setRGB(i+1,	0xf, rgb);
					cur.setRGB(0, 	i+1, rgb);
					cur.setRGB(0xf,	i, rgb);
					i++;
				}
				rgb = (~rgb) | (0xff000000);
				cur.setRGB(0x6, 0x6, rgb);
				cur.setRGB(0x6, 0x9, rgb);
				cur.setRGB(0x9, 0x6, rgb);
				cur.setRGB(0x9, 0x9, rgb);
				setCursor(tk.createCustomCursor(cur, p, ""));
			}
			@Override
			public void mouseDragged(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			public void paintComponent(Graphics gr){
				super.paintComponent(gr);
			}
		}
		Pipette ppt = new Pipette();
		ppt.setFocusable(true);
		ppt.setBackground(new Color(0x0,0x0,0x0,1));
		ppt.addMouseListener(ppt);
		ppt.addMouseMotionListener(ppt);
		dialog.add(ppt);
		dialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		dialog.setVisible(true);
		dialog.dispose();
	}
}
