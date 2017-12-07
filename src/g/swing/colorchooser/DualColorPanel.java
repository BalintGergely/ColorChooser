package g.swing.colorchooser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
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
	private ColorSwatch lbutton,rbutton,mixButton;
	private final DefaultColorSelectionModel left,right;
	private Vector<ChangeListener> ls;
	private boolean isR;
	public Color getColor(boolean isR0){
		return isR0 ? right.getSelectedColor() : left.getSelectedColor();
	}
	public DualColorPanel(Robot rob) {
		this(rob == null ? null : (ColorSelectionModel mod) -> {
			choose(rob,mod);
		});
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
		dim = new Dimension(24,24);
		mixButton.setMinimumSize(dim);
		mixButton.setPreferredSize(dim);
		
		JButton swapB = new JButton("<>");
		swapB.addActionListener((ActionEvent e) -> {
			Color l = left.getSelectedColor(),r = right.getSelectedColor();
			left.setSelectedColor(r);
			right.setSelectedColor(l);
		});
		
		group.add(lbutton);
		group.add(rbutton);
		lbutton.setSelected(true);
		lbutton.addActionListener((ActionEvent e) -> {
			isR = false;
			fireChangedEvent(null);
		});
		rbutton.addActionListener((ActionEvent e) -> {
			isR = true;
			fireChangedEvent(null);
		});
		mixButton.addActionListener((ActionEvent e) -> {
			setSelectedColor(mixButton.getBackground());
		});
		con.weightx = 1;
		super.add(lbutton,con);
		con.gridx = 1;
		super.add(mixButton,con);
		con.gridx = 2;
		super.add(rbutton,con);
		con.fill = GridBagConstraints.BOTH;
		con.gridy = 1;
		con.gridx = 0;
		super.add(new HexColorChooserField(left),con);
		con.gridx = 1;
		super.add(swapB,con);
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
	public synchronized static void choose(Robot rob,ColorSelectionModel model){
		Objects.requireNonNull(rob);
		Objects.requireNonNull(model);
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.setBackground(new Color(0,0,0,0));
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0x80,0x80,0x80,1));
		MouseAdapter ad = new MouseAdapter(){
			@Override
			public void mouseMoved(MouseEvent e) {
				model.setSelectedColor(rob.getPixelColor(e.getX(), e.getY()));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				dialog.dispose();
				model.setSelectedColor(rob.getPixelColor(e.getX(), e.getY()));
			}
		};
		panel.addMouseListener(ad);
		panel.addMouseMotionListener(ad);
		dialog.add(panel);
		dialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		dialog.setVisible(true);
		dialog.dispose();
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
				boolean sw = c.getBlue()+c.getGreen()+c.getRed() > 448;
				if(isr){
					rbutton.setBackground(c);
					rbutton.setForeground(sw ? Color.BLACK : Color.WHITE);
				}else{
					lbutton.setBackground(c);
					lbutton.setForeground(sw ? Color.BLACK : Color.WHITE);
				}
			}
		}
		mixButton.setBackground(new Color(
				ColorConstants.blend(
						(isR ? right : left).getSelectedColor().getRGB(),
						(isR ? left : right).getSelectedColor().getRGB())
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
}
