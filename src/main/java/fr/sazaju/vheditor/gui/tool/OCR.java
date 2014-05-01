package fr.sazaju.vheditor.gui.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class OCR extends JPanel implements Tool {

	public OCR() {
		File originalFile = new File(
				"/home/sazaju/Programing/Java/VH-Translation-Editor/VH/branches/working/Picture/文字/セリフ：アシュリー：ダメージ1.png");

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 0;

		ImagePanel originalImage = new ImagePanel(originalFile);
		add(originalImage, constraints);

		constraints.gridx++;
		ImagePanel translationImage = new ImagePanel(originalImage);
		add(translationImage, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		TextArea originalArea = new TextArea(originalImage);
		originalArea.setText("アシュリー\n『こ、これくらい…\n　まだまだへっちゃらです！』");
		originalArea.setEditable(false);
		add(originalArea, constraints);

		constraints.gridx++;
		TextArea translationArea = new TextArea(translationImage);
		translationArea.setText("translation");
		add(translationArea, constraints);
	}

	@Override
	public void setToolProvider(ToolProvider provider) {
		// no need for provider
	}

	@Override
	public String getTitle() {
		return "OCR";
	}

	@Override
	public JPanel instantiatePanel() {
		return this;
	}

	private static class ImagePanel extends JPanel {

		private final BufferedImage image;
		private final int background;
		private final Font font = new Font("MS Gothic", Font.PLAIN, 18);

		public ImagePanel(File file) {
			try {
				image = ImageIO.read(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			background = -1;
			fixSize(image);
		}

		public ImagePanel(ImagePanel originalImage) {
			BufferedImage original = originalImage.image;
			image = new BufferedImage(original.getWidth(),
					original.getHeight(), original.getType());
			background = original.getRGB(0, 0);
			fixSize(image);
		}

		private void fixSize(BufferedImage image) {
			Dimension size = new Dimension(image.getWidth(), image.getHeight());
			setMinimumSize(size);
			setMaximumSize(size);
			setPreferredSize(size);
			setSize(size);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, null);
		}

		public void setText(String text) {
			if (background == -1) {
				// fixed image
			} else {
				Graphics2D graphics = (Graphics2D) image.getGraphics();
				graphics.setColor(new Color(background));
				graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				graphics.setFont(font);

				String[] lines = text.split("[\n\r]+");
				int x = 0;
				int y = graphics.getFontMetrics().getAscent();
				for (String line : lines) {
					graphics.setColor(Color.BLACK);
					graphics.drawString(line, x + 1, y + 1);
					graphics.setColor(Color.WHITE);
					graphics.drawString(line, x, y);
					y += graphics.getFontMetrics().getHeight();
				}

				repaint();
				revalidate();
			}
		}

	}

	private static class TextArea extends JTextArea {

		public TextArea(final ImagePanel image) {
			setMinimumSize(image.getSize());
			setMaximumSize(image.getSize());
			setPreferredSize(image.getSize());
			setSize(image.getSize());
			setBorder(new EtchedBorder());
			getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					image.setText(getText());
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					image.setText(getText());
				}

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					// nothing to do
				}
			});
		}
	}
}
