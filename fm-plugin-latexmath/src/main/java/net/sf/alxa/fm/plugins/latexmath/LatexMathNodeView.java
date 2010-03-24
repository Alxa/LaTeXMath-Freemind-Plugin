package net.sf.alxa.fm.plugins.latexmath;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import freemind.view.mindmapview.MapView;

/**
 * A port of JLatexViewer from freeplane
 * 
 */
public class LatexMathNodeView extends JComponent {
	// constants
	private static final long serialVersionUID = 1L;
	private static final int FONT_SIZE_DEFAULT = 16;
	private static final int FONT_SIZE_MIN = 4;
	private static final int FONT_SIZE_MAX = 50;
	
	
	private int paddingTop = 5; // TODO configurable insets
	static String editorTitle = "Latex Equation (via JLaTeXMath)";
	private float zoom = 1f;
	// model
	private int fontsize = FONT_SIZE_DEFAULT;
	private LatexMathNodeHook model;
	private TeXFormula teXFormula;
	private Icon latexIcon;
	// editor
	private final LatexMathEditor latexEditor;

	//
	LatexMathNodeView(LatexMathNodeHook model) {
		setModel(model);
		this.latexEditor = new LatexMathEditor(this);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (latexEditor != null) {
						latexEditor.edit(LatexMathNodeView.this.model);
					}
					e.consume();
					return;
				}
			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		calculateSize();
		return super.getPreferredSize();
	}

	private void calculateSize() {
		MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(
				MapView.class, this);
		final float mapZoom = mapView.getZoom();
		if (mapZoom != zoom) {
			updateTexIcon();
		}
		this.zoom = mapZoom;
		final Insets insets = getInsets();
		insets.top = paddingTop;
		final Dimension dimension = new Dimension(latexIcon.getIconWidth()
				+ insets.left + insets.right, latexIcon.getIconHeight()
				+ insets.top + insets.bottom);
		setPreferredSize(dimension);
	}

	private void updateTexIcon() {
		// System.out.println("update tex display");
		latexIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY,
				fontsize * zoom);
	}

	@Override
	public void paint(final Graphics g) {
		final Insets insets = getInsets();
		insets.top = paddingTop;
		latexIcon.paintIcon(this, g, insets.left, insets.top);
		super.paint(g);
	}

	public void setBounds(int x, int y, int w, int h) {
		if (zoom < 1f) {
			super.setBounds(x, y, (int) (w / zoom), (int) (h / zoom));
		} else {
			super.setBounds(x, y, (int) (w), (int) (h));
		}
	}

	public void setModel(final LatexMathNodeHook model) {
		this.model = model;
		try {
			teXFormula = new TeXFormula(model.getContent());
			teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY,
					fontsize);
			updateTexIcon();
		} catch (Exception e) {
			try {
				teXFormula = new TeXFormula("\\mbox{" + e.getMessage() + "}");
				teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY,
						fontsize);
			} catch (Exception e1) {
				teXFormula = new TeXFormula(
						"\\mbox{Can not parse given equation}");
			}
		}
		// zoom = 0;
		revalidate();
		repaint();
	}

	/**
	 * Latex Equation Editor
	 * 
	 */
	private static class LatexMathEditor {
		// model
		private Component parent;
		private final JTextArea textArea = new JTextArea();
		final JOptionPane editPane;
		final JDialog dialog;

		//
		public LatexMathEditor(Component parent) {
			this.parent = parent;
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			final JScrollPane editorScrollPane = new JScrollPane(textArea);
			editorScrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			editorScrollPane.setPreferredSize(new Dimension(500, 160));
			
			// Edit pane
			editPane = new JOptionPane(editorScrollPane,
					JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	
			// Dialog
			dialog = editPane.createDialog(JOptionPane
					.getFrameForComponent(parent), editorTitle);
			dialog
					.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		}

		public void edit(LatexMathNodeHook model) {
			textArea.setText(model.getContent());
			dialog.setLocationRelativeTo(parent);
			dialog.setVisible(true);

			if (editPane.getValue().equals(JOptionPane.OK_OPTION)) {
				final String eq = textArea.getText();
				model.setContentUndoable(eq);
			}

		}

	}

}
