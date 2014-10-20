package fr.sazaju.vheditor.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import fr.sazaju.vheditor.gui.UntranslatedTag.SwitchListener;
import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap;
import fr.sazaju.vheditor.translation.parsing.MapEntry;
import fr.vergne.parsing.layer.Layer;

public class MapContentPanelBuilder {

	private final LinkedHashMap<JComponent, LineManager> components = new LinkedHashMap<JComponent, LineManager>();

	public MapContentPanelBuilder clear() {
		components.clear();
		return this;
	}

	public JPanel instantiate() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraintsComponent = new GridBagConstraints();
		constraintsComponent.gridx = 1;
		constraintsComponent.anchor = GridBagConstraints.LINE_START;
		constraintsComponent.fill = GridBagConstraints.HORIZONTAL;
		constraintsComponent.weightx = 1;

		GridBagConstraints constraintsLines = new GridBagConstraints();
		constraintsLines.gridx = 0;
		constraintsLines.anchor = GridBagConstraints.LINE_START;
		constraintsLines.fill = GridBagConstraints.BOTH;
		constraintsLines.insets = new Insets(0, 0, 0, 10);

		JLineNumber previousLines = null;
		for (Entry<JComponent, LineManager> entry : components.entrySet()) {
			JComponent component = entry.getKey();
			panel.add(component, constraintsComponent);

			LineManager manager = entry.getValue();
			JLineNumber lines = new JLineNumber(manager, previousLines);
			panel.add(lines, constraintsLines);
			previousLines = lines;
		}
		return panel;
	}

	public MapContentPanelBuilder setWithMap(BackedTranslationMap map) {
		clear();

		addConstantText(removeLastNewLine(map.getHeader()));
		for (MapEntry entry : map.getUsedEntries()) {
			addCompleteEntry(entry);
		}

		if (map.hasUnusedEntries()) {
			Color oldBackground = getBackground();
			setBackground(Color.MAGENTA);

			addConstantText(removeLastNewLine(map.getUnusedEntriesSeparator()));
			for (MapEntry entry : map.getUnusedEntries()) {
				addCompleteEntry(entry);
			}

			setBackground(oldBackground);
		} else {
			// no unused part
		}

		return this;
	}

	public MapContentPanelBuilder addCompleteEntry(MapEntry mapEntry) {
		addConstantText(removeLastNewLine(mapEntry.getStartFlag()));
		addOptionalConstantText(new UntranslatedTag(mapEntry));
		addConstantText(removeLastNewLine(mapEntry.getContextLine()));
		if (mapEntry.hasAdvice()) {
			addConstantText(removeLastNewLine(mapEntry.getAdvice()));
		} else {
			// no advice to add
		}
		addConstantText(mapEntry.getOriginalVersion(), true);
		addConstantText(removeLastNewLine(mapEntry.getTranslationFlag()));
		addEditableText(new TranslationArea(mapEntry));
		addConstantText(removeLastNewLine(mapEntry.getEndFlag()));

		return this;
	}

	private String removeLastNewLine(Layer layer) {
		return layer.getContent().replaceFirst("\n$", "");
	}

	private Color background = null;

	public MapContentPanelBuilder setBackground(Color color) {
		background = color;
		return this;
	}

	public Color getBackground() {
		return background;
	}

	public MapContentPanelBuilder addConstantText(String content) {
		return addConstantText(content, false);
	}

	public MapContentPanelBuilder addConstantText(String content,
			boolean isHighlighted) {
		final JTextArea lines = new JTextArea(content);
		if (isHighlighted) {
			lines.setOpaque(true);
		} else {
			lines.setOpaque(false);
		}
		lines.setEditable(false);
		addAndSet(lines, new LineManager() {
			@Override
			public int getLineCount() {
				return lines.getText().replaceAll("[^\n]", "").length() + 1;
			}

			@Override
			public void addUpdateListener(final UpdateListener listener) {
				lines.getDocument().addDocumentListener(new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						listener.updated();
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						listener.updated();
					}

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						listener.updated();
					}
				});

			}
		});
		return this;
	}

	public MapContentPanelBuilder addEditableText(
			final TranslationArea translationArea) {
		// FIXME use (String content, XxxListener listener)
		addAndSet(translationArea, new LineManager() {
			@Override
			public int getLineCount() {
				return translationArea.getText().replaceAll("[^\n]", "")
						.length() + 1;
			}

			@Override
			public void addUpdateListener(final UpdateListener listener) {
				translationArea.getDocument().addDocumentListener(
						new DocumentListener() {

							@Override
							public void removeUpdate(DocumentEvent arg0) {
								listener.updated();
							}

							@Override
							public void insertUpdate(DocumentEvent arg0) {
								listener.updated();
							}

							@Override
							public void changedUpdate(DocumentEvent arg0) {
								listener.updated();
							}
						});
			}
		});
		return this;
	}

	public MapContentPanelBuilder addOptionalConstantText(
			final UntranslatedTag tag) {
		// FIXME use (String content, OptionListener listener)
		addAndSet(tag, new LineManager() {
			@Override
			public int getLineCount() {
				return tag.isMarked() ? 1 : 0;
			}

			@Override
			public void addUpdateListener(final UpdateListener listener) {
				tag.addSwitchListener(new SwitchListener() {

					@Override
					public void switched(boolean isNowActivated) {
						listener.updated();
					}
				});
			}
		});
		return this;
	}

	public static interface OptionListener {
		public void isActivated();

		public void isDeactivated();
	}

	private JComponent addAndSet(JComponent component, LineManager manager) {
		components.put(component, manager);
		if (background == null) {
			// keep default background
		} else {
			component.setOpaque(true);
			component.setBackground(background);
		}
		return component;
	}

	private static interface LineManager {

		int getLineCount();

		void addUpdateListener(UpdateListener listener);

	}

	private static interface UpdateListener {
		public void updated();
	}

	@SuppressWarnings("serial")
	public static class JLineNumber extends JLabel {

		private static final String UPDATE_PROPERTY = "lineUpdate";
		private final LineManager manager;
		private final JLineNumber previousLines;
		private int previousMax = 0;
		private int currentMax = 0;

		public JLineNumber(LineManager manager, JLineNumber previousLines) {
			super("", JLabel.RIGHT);
			this.previousLines = previousLines;
			if (previousLines != null) {
				previousLines.addPropertyChangeListener(UPDATE_PROPERTY,
						new PropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent arg0) {
								invalidate();
							}
						});
			} else {
				// no previous lines to take updates from
			}

			this.manager = manager;
			manager.addUpdateListener(new UpdateListener() {

				@Override
				public void updated() {
					invalidate();
				}
			});

			setOpaque(false);
			setFont(getFont().deriveFont(Font.PLAIN));
			setForeground(Color.GRAY);

			invalidate();
		}

		public int getCurrentMax() {
			return currentMax;
		}

		@Override
		public void invalidate() {
			boolean isUpdated = false;
			if (previousLines != null
					&& previousMax != previousLines.getCurrentMax()) {
				previousMax = previousLines.getCurrentMax();
				isUpdated = true;
			} else {
				// no update from previous lines
			}

			if (currentMax - previousMax != manager.getLineCount()) {
				currentMax = manager.getLineCount() + previousMax;
				isUpdated = true;
			} else {
				// no update from current lines
			}

			if (isUpdated) {
				setText(produceText(previousMax + 1, currentMax));
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						/*
						 * This late invocation allows to avoid stack overflow
						 * exceptions due to too many events fired for huge
						 * files. On the other hand, this way to do makes it
						 * long to update everything, like ~20s to update 3'000
						 * lines.
						 * 
						 * FIXME A better compromise should be found.
						 */
						firePropertyChange(UPDATE_PROPERTY, null, null);
					}
				});
				super.invalidate();
			} else {
				// still the same thing
			}
		}

		private static String produceText(int min, int max) {
			LinkedList<Integer> numbers = new LinkedList<Integer>();
			for (int i = min; i <= max; i++) {
				numbers.add(i);
			}
			String string = StringUtils.join(numbers, "<br/>");
			/*
			 * Sometimes, the alignment of the JLabel is missed by the HTML.
			 * Setting the alignment this way only do not solve the issue. Only
			 * the combination of both was able to maintain the alignment.
			 */
			string = "<div align=right>" + string + "</div>";
			string = "<html>" + string + "</html>";
			return string;
		}
	}

}
