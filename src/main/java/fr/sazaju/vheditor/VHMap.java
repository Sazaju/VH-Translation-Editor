package fr.sazaju.vheditor;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.impl.PatternFileMap;
import fr.vergne.translation.impl.PatternFileMap.PatternEntry;
import fr.vergne.translation.impl.PatternFileMap.PatternMetadata;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.SmartStringSwitcher;

public class VHMap implements TranslationMap<VHEntry> {

	public static final Field<Boolean> MARKED_AS_UNTRANSLATED = new Field<Boolean>(
			"Untranslated tag");
	public static final Field<Integer> CHAR_LIMIT_FACE = new Field<Integer>(
			"Char limit (face)");
	public static final Field<Integer> CHAR_LIMIT_NO_FACE = new Field<Integer>(
			"Char limit (no face)");
	public static final Field<String> CONTEXT = new Field<String>("Context");

	private static final String entryRegex = "(?s)# TEXT STRING\r?\n.*?\r?\n# END STRING";
	private static final String textLineRegex = "(|[^#\r\n][^\r\n]*)";
	private static final String textRegex = textLineRegex + "(\r?\n"
			+ textLineRegex + ")*";
	private static final String originalRegex = "(?s)(?<=\r?\n)" + textRegex
			+ "(?=\r?\n# TRANSLATION )";
	private static final String translationRegex = "(?s)(?<=# TRANSLATION \r?\n)"
			+ textRegex + "(?=\r?\n)";
	private final PatternFileMap submap;
	private final Collection<EntryFilter<VHEntry>> filters;
	private final int startUnusedIndex;

	static {
		// PatternFileMap.logger.setLevel(Level.ALL);
	}

	public VHMap(File file) {
		submap = new PatternFileMap(file, entryRegex, originalRegex,
				translationRegex);

		{
			Field<Boolean> field = MARKED_AS_UNTRANSLATED;
			String regex = "(?<=# TEXT STRING)\r?\n(# UNTRANSLATED\r?\n)?(?=# CONTEXT)";
			Switcher<String, Boolean> convertor = new Switcher<String, Boolean>() {

				private String newLine;

				@Override
				public Boolean switchForth(String value) {
					newLine = value.replaceAll("(?s)#.*$", "");
					return !value.trim().isEmpty();
				}

				@Override
				public String switchBack(Boolean value) {
					if (value) {
						return newLine + "# UNTRANSLATED" + newLine;
					} else {
						return newLine;
					}
				}
			};
			submap.addFieldRegex(field, regex, convertor, true);
		}

		{
			Field<Integer> field = CHAR_LIMIT_NO_FACE;
			String regex = "((?<=# ADVICE : \\??+)\\d+(?=\\D)|(?<=# TRANSLATION)(?=(?s:.)))";
			Switcher<String, Integer> convertor = new SmartStringSwitcher<>(
					Integer.class, "");
			submap.addFieldRegex(field, regex, convertor, false);
		}

		{
			Field<Integer> field = CHAR_LIMIT_FACE;
			String regex = "((?<=# ADVICE : \\??+\\d{1,4} char limi?+t \\()\\d+(?=\\D)|(?<=# END STRING)$)";
			Switcher<String, Integer> convertor = new SmartStringSwitcher<>(
					Integer.class, "");
			submap.addFieldRegex(field, regex, convertor, false);
		}

		{
			Field<String> field = CONTEXT;
			String regex = "(?<=# CONTEXT : )[^\r\n]+(?=\r?\n)";
			submap.addFieldRegex(field, regex, false);
		}

		startUnusedIndex = findUnusedIndex(submap);

		this.filters = new LinkedList<>();
		this.filters.add(new EntryFilter<VHEntry>() {

			@Override
			public String getName() {
				return MARKED_AS_UNTRANSLATED.getName();
			}

			@Override
			public String getDescription() {
				return "Search for entries marked with #UNTRANSLATED.";
			}

			@Override
			public boolean isRelevant(VHEntry entry) {
				return entry.getMetadata().get(MARKED_AS_UNTRANSLATED);
			}

		});
	}

	private int findUnusedIndex(PatternFileMap submap) {
		for (int index = 0; index < submap.size(); index++) {
			String beforeEntry = submap.getBeforeEntry(index);
			if (beforeEntry.contains("# UNUSED TRANSLATABLES")) {
				return index;
			} else {
				// not yet found;
			}
		}
		return submap.size();
	}

	@Override
	public Iterator<VHEntry> iterator() {
		return new Iterator<VHEntry>() {

			private int nextIndex = 0;

			@Override
			public boolean hasNext() {
				return nextIndex < startUnusedIndex;
			}

			@Override
			public VHEntry next() {
				if (hasNext()) {
					VHEntry entry = getEntry(nextIndex);
					nextIndex++;
					return entry;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"You cannot remove entries from this map.");
			}
		};
	}

	public Iterator<VHEntry> unusedIterator() {
		return new Iterator<VHEntry>() {

			private int nextIndex = startUnusedIndex;

			@Override
			public boolean hasNext() {
				return nextIndex < submap.size();
			}

			@Override
			public VHEntry next() {
				if (hasNext()) {
					VHEntry entry = getEntry(nextIndex);
					nextIndex++;
					return entry;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"You cannot remove entries from this map.");
			}
		};
	}

	@Override
	public VHEntry getEntry(int index) {
		PatternEntry subentry = submap.getEntry(index);
		boolean isUsed = index < startUnusedIndex;
		return new VHEntry(subentry, isUsed);
	}

	@Override
	public int size() {
		return startUnusedIndex;
	}

	public int unusedSize() {
		return submap.size() - startUnusedIndex;
	}

	@Override
	public void saveAll() {
		submap.saveAll();
	}

	@Override
	public void resetAll() {
		submap.resetAll();
	}

	@Override
	public Collection<EntryFilter<VHEntry>> getEntryFilters() {
		return filters;
	}

	public String getBeforeEntry(int index) {
		return submap.getBeforeEntry(index);
	}

	public String getAfterEntry(int index) {
		return submap.getAfterEntry(index);
	}

	public static class VHEntry implements TranslationEntry<VHMetadata> {

		private final PatternEntry subentry;
		private final boolean isUsed;

		public VHEntry(PatternEntry subentry, boolean isUsed) {
			this.subentry = subentry;
			this.isUsed = isUsed;
		}

		public boolean isUsed() {
			return isUsed;
		}

		@Override
		public String getOriginalContent() {
			return subentry.getOriginalContent();
		}

		@Override
		public String getStoredTranslation() {
			return subentry.getStoredTranslation();
		}

		@Override
		public String getCurrentTranslation() {
			return subentry.getCurrentTranslation();
		}

		@Override
		public void setCurrentTranslation(String translation) {
			subentry.setCurrentTranslation(translation);
		}

		@Override
		public void saveTranslation() {
			subentry.saveTranslation();
		}

		@Override
		public void resetTranslation() {
			subentry.resetTranslation();
		}

		@Override
		public void saveAll() {
			subentry.saveAll();
		}

		@Override
		public void resetAll() {
			subentry.resetAll();
		}

		@Override
		public VHMetadata getMetadata() {
			return new VHMetadata(subentry.getMetadata());
		}

		@Override
		public void addTranslationListener(TranslationListener listener) {
			subentry.addTranslationListener(listener);
		}

		@Override
		public void removeTranslationListener(TranslationListener listener) {
			subentry.removeTranslationListener(listener);
		}

		@Override
		public String toString() {
			return getOriginalContent() + " = " + getCurrentTranslation() + " "
					+ getMetadata();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof VHEntry) {
				VHEntry e = (VHEntry) obj;
				return subentry.equals(e.subentry);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return subentry.hashCode();
		}
	}

	public static class VHMetadata implements TranslationMetadata {

		private final PatternMetadata submetadata;

		public VHMetadata(PatternMetadata submetadata) {
			this.submetadata = submetadata;
		}

		@Override
		public Iterator<Field<?>> iterator() {
			return submetadata.iterator();
		}

		@Override
		public <T> T getStored(Field<T> field) {
			return submetadata.getStored(field);
		}

		@Override
		public <T> T get(Field<T> field) {
			return submetadata.get(field);
		}

		@Override
		public <T> boolean isEditable(Field<T> field) {
			return submetadata.isEditable(field);
		}

		@Override
		public <T> void set(Field<T> field, T value)
				throws UneditableFieldException {
			submetadata.set(field, value);
		}

		@Override
		public void addFieldListener(FieldListener listener) {
			submetadata.addFieldListener(listener);
		}

		@Override
		public void removeFieldListener(FieldListener listener) {
			submetadata.removeFieldListener(listener);
		}

		@Override
		public <T> void save(Field<T> field) {
			submetadata.save(field);
		}

		@Override
		public <T> void reset(Field<T> field) {
			submetadata.reset(field);
		}

		@Override
		public void saveAll() {
			submetadata.saveAll();
		}

		@Override
		public void resetAll() {
			submetadata.resetAll();
		}

		@Override
		public String toString() {
			return submetadata.toString();
		}
	}
}
