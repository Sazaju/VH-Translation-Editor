package fr.sazaju.vheditor.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.gui.MapContentPanelBuilder.JLineNumber;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap;
import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap.EmptyMapException;
import fr.sazaju.vheditor.translation.parsing.MapEntry;
import fr.vergne.logging.LoggerConfiguration;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JPanel mapContentArea;
	private final JPanel mapLoadingArea;
	private final JScrollPane mapContentScroll;
	private final JLabel mapTitleField;
	private final BackedTranslationMap map = new BackedTranslationMap();
	private final LoadingManager loading;
	private final JLabel loadingLabel;
	public Logger logger = LoggerConfiguration.getSimpleLogger();

	public MapContentPanel() {
		setBorder(new EtchedBorder());

		final CardLayout contentSwitcher = new CardLayout();
		setLayout(contentSwitcher);
		loading = new LoadingManager() {

			@Override
			public void start() {
				contentSwitcher.last(MapContentPanel.this);
			}

			@Override
			public void stop() {
				contentSwitcher.first(MapContentPanel.this);
			}
		};

		JPanel contentWrapper = new JPanel();
		contentWrapper.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.CENTER;
		mapTitleField = new JLabel(" ");
		contentWrapper.add(mapTitleField, constraints);

		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		mapContentArea = new JPanel();
		mapContentArea.setLayout(new GridLayout(1, 1));
		mapContentScroll = new JScrollPane(mapContentArea);
		mapContentScroll.getVerticalScrollBar().setUnitIncrement(15);
		contentWrapper.add(mapContentScroll, constraints);
		add(contentWrapper);

		mapLoadingArea = new JPanel();
		mapLoadingArea.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		loadingLabel = new JLabel("Loading...");
		mapLoadingArea.add(loadingLabel, constraints);
		add(mapLoadingArea);
	}

	public int getCurrentEntryIndex() {
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		Component focusOwner = frame.getFocusOwner();
		Iterator<EntryWrapper> iterator = getEntryIterator();
		if (focusOwner instanceof TranslationArea) {
			int index = 0;
			while (iterator.hasNext()) {
				EntryWrapper entry = iterator.next();
				if (entry.contains(focusOwner)) {
					return index;
				} else {
					index++;
				}
			}
			throw new RuntimeException(
					"Translation area not found in entries' components.");
		} else {
			int index = 0;
			Rectangle visible = mapContentArea.getVisibleRect();
			while (iterator.hasNext()) {
				EntryWrapper entry = iterator.next();
				for (Component component : entry) {
					Rectangle bounds = component.getBounds();
					if (visible.y < bounds.y + bounds.height) {
						return index;
					} else {
						// not yet the searched entry
					}
				}
				index++;
			}
		}

		throw new IllegalStateException("Impossible to find the current entry.");
	}

	public void goToEntry(final int entryIndex) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				Iterator<EntryWrapper> iterator = getEntryIterator();
				EntryWrapper entry = null;
				for (int i = 0; i <= entryIndex; i++) {
					entry = iterator.next();
				}

				if (entryIndex == 0) {
					mapContentScroll.getVerticalScrollBar().setValue(0);
				} else {
					Rectangle visible = mapContentArea.getVisibleRect();
					Component target = entry.getHeader();
					visible.y = 0;
					while (target != mapContentArea) {
						visible.y += target.getBounds().y;
						target = target.getParent();
					}
					mapContentArea.scrollRectToVisible(visible);
				}

				entry.getTranslationArea().requestFocusInWindow();
			}
		});
	}

	/**
	 * 
	 * @return an {@link Iterator} which provides the {@link List}s of
	 *         {@link Component}s corresponding to each {@link MapEntry}, in the
	 *         current order.
	 */
	private Iterator<EntryWrapper> getEntryIterator() {
		if (mapContentArea.getComponentCount() == 0) {
			return Collections.<EntryWrapper> emptyList().iterator();
		} else {
			return new Iterator<EntryWrapper>() {

				private final Component[] components = ((JPanel) mapContentArea
						.getComponent(0)).getComponents();
				private int nextIndex = 0;

				@Override
				public void remove() {
					throw new RuntimeException(
							"You cannot remove an entry from this iterator.");
				}

				@Override
				public EntryWrapper next() {
					searchNext();

					List<Component> list = new ArrayList<Component>();
					Component component;
					do {
						component = components[nextIndex];
						if (component instanceof JLineNumber) {
							// line display, not a map component
						} else {
							logger.fine("Store " + nextIndex + ": " + component);
							list.add(component);
						}
						nextIndex++;
					} while (!(component instanceof JTextArea && ((JTextArea) component)
							.getText().startsWith("# END STRING")));

					return new EntryWrapper(list);
				}

				private void searchNext() {
					Component component = null;
					while (nextIndex < components.length
							&& !(component instanceof JTextArea && ((JTextArea) component)
									.getText().startsWith("# TEXT STRING"))) {
						component = components[nextIndex];
						logger.fine("Check " + nextIndex + ": " + component);
						nextIndex++;
					}
					if (nextIndex == components.length) {
						// end reached
					} else {
						nextIndex--;
					}
				}

				@Override
				public boolean hasNext() {
					searchNext();
					return nextIndex < components.length;
				}
			};
		}
	}

	private static class EntryWrapper implements Iterable<Component> {
		private final List<Component> components;

		public EntryWrapper(List<Component> components) {
			if (components.size() == 1) {
				throw new IllegalArgumentException();
			}
			this.components = new ArrayList<Component>(components);
		}

		public Component getHeader() {
			return components.get(0);
		}

		public UntranslatedTag getUntranslatedTag() {
			return (UntranslatedTag) components.get(1);
		}

		public TranslationArea getTranslationArea() {
			return (TranslationArea) components.get(components.size() - 2);
		}

		public boolean contains(Component component) {
			return components.contains(component);
		}

		@Override
		public Iterator<Component> iterator() {
			return components.iterator();
		}

	}

	public void goToNextUntranslatedEntry(boolean relyOnTags) {
		TreeSet<Integer> untranslatedEntries = new TreeSet<Integer>(
				getUntranslatedEntryIndexes(relyOnTags));
		if (untranslatedEntries.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"All the entries are already translated.");
		} else {
			int currentEntry = getCurrentEntryIndex();
			Integer next = untranslatedEntries.ceiling(currentEntry + 1);
			if (next == null) {
				JOptionPane
						.showMessageDialog(this,
								"End of the entries reached. Search from the beginning.");
				goToEntry(untranslatedEntries.first());
			} else {
				goToEntry(next);
			}
		}
	}

	public void setMap(final File mapFile) {
		setMap(mapFile, 0);
	}

	public void setMap(final File mapFile, final int entryIndex) {
		if (mapFile.equals(map.getBaseFile())) {
			goToEntry(entryIndex);
		} else {
			loadingLabel.setText("Loading map " + mapFile.getName() + "...");
			loading.start();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						synchronized (map) {
							map.setBaseFile(mapFile);
							// TODO add map title (English label)
							mapTitleField.setText(mapFile.getName());
							mapContentArea.removeAll();
							MapContentPanelBuilder builder = new MapContentPanelBuilder();
							builder.setWithMap(map);
							mapContentArea.add(builder.instantiate());
							goToEntry(entryIndex);
						}
					} catch (EmptyMapException e) {
						JOptionPane.showMessageDialog(MapContentPanel.this,
								"The map " + mapFile + " is empty.",
								"Empty Map", JOptionPane.WARNING_MESSAGE);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MapContentPanel.this,
								e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							loading.stop();
						}
					});
				}
			});
		}
	}

	public TranslationMap getMap() {
		return map;
	}

	private static interface LoadingManager {
		public void start();

		public void stop();
	}

	public void save() {
		logger.info("Applying modifications...");
		Iterator<EntryWrapper> iterator = getEntryIterator();
		while (iterator.hasNext()) {
			EntryWrapper entry = iterator.next();
			entry.getUntranslatedTag().save();
			entry.getTranslationArea().save();
		}
		logger.info("Saving map to " + map.getBaseFile() + "...");
		map.save();
		logger.info("Map saved.");
		for (MapSavedListener listener : listeners) {
			listener.mapSaved(map.getBaseFile());
		}
	}

	public void reset() {
		if (map.getBaseFile() == null
				|| !isModified()
				|| JOptionPane
						.showConfirmDialog(
								this,
								"Are you sure you want to cancel *ALL* the modifications that you have not saved?",
								"Cancel Modifications",
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		} else {
			// we can go ahead
		}
		setVisible(false);
		Rectangle visible = mapContentArea.getVisibleRect();
		final JComponent reference = (JComponent) mapContentArea
				.getComponentAt(0, visible.y);
		final int offset = reference.getVisibleRect().y;
		for (Component component : mapContentArea.getComponents()) {
			if (component instanceof TranslationArea) {
				TranslationArea area = (TranslationArea) component;
				area.reset();
			} else {
				// irrelevant component
			}
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				Rectangle visible = mapContentArea.getVisibleRect();
				visible.y = reference.getBounds().y + offset;
				mapContentArea.scrollRectToVisible(visible);
				setVisible(true);
			}
		});
	}

	public Collection<Integer> getUntranslatedEntryIndexes(boolean relyOnTags) {
		Collection<Integer> untranslatedEntries = new LinkedList<Integer>();
		Iterator<? extends TranslationEntry> iterator = map.iteratorUsed();
		int count = 0;
		while (iterator.hasNext()) {
			TranslationEntry entry = iterator.next();
			if (relyOnTags && entry.isMarkedAsUntranslated() || !relyOnTags
					&& !entry.isActuallyTranslated()) {
				untranslatedEntries.add(count);
			} else {
				// already translated
			}
			count++;
		}
		return untranslatedEntries;
	}

	public boolean isModified() {
		Iterator<EntryWrapper> iterator = getEntryIterator();
		while (iterator.hasNext()) {
			EntryWrapper entry = iterator.next();
			if (entry.getTranslationArea().isModified()
					|| entry.getUntranslatedTag().isModified()) {
				return true;
			} else {
				// check others
			}
		}
		return false;
	}

	private final Collection<MapSavedListener> listeners = new HashSet<MapSavedListener>();

	public void addListener(MapSavedListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MapSavedListener listener) {
		listeners.remove(listener);
	}

	public interface MapSavedListener {

		void mapSaved(File mapFile);

	}

}
