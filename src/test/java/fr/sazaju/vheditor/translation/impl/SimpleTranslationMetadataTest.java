package fr.sazaju.vheditor.translation.impl;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationMetadataTest;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldWriter;

public class SimpleTranslationMetadataTest extends TranslationMetadataTest {

	private final Field<Integer> nonEditable0 = new Field<>("Field 0");
	private final Field<Integer> editable0 = new Field<>("Field 1");
	private final Field<Integer> nonEditable1 = new Field<>("Field 2");
	private final Field<Integer> editable1 = new Field<>("Field 3");

	@Override
	protected TranslationMetadata createTranslationMetadata() {
		SimpleTranslationMetadata metadata = new SimpleTranslationMetadata();
		final Integer[] datastore = { getInitialReference(nonEditable0),
				getInitialReference(nonEditable1),
				getInitialReference(editable0), getInitialReference(editable1) };
		metadata.configureField(nonEditable0,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[0];
					}
				});
		metadata.configureField(nonEditable1,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[2];
					}
				});
		metadata.configureField(editable0,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[1];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[1] = value;
					}
				});
		metadata.configureField(editable1,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[3];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[3] = value;
					}
				});
		return metadata;
	}

	@Override
	protected Collection<Field<?>> getEditableFields() {
		Collection<Field<?>> fields = new LinkedList<Field<?>>();
		fields.add(editable0);
		fields.add(editable1);
		return fields;
	}

	@Override
	protected Collection<Field<?>> getNonEditableFields() {
		Collection<Field<?>> fields = new LinkedList<Field<?>>();
		fields.add(nonEditable0);
		fields.add(nonEditable1);
		return fields;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getInitialReference(Field<T> field) {
		return (T) (Integer) 10;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}

	@Test
	public void testEditableFieldIdentifiedThroughWriter() {
		SimpleTranslationMetadata metadata = new SimpleTranslationMetadata();
		final Integer[] datastore = { 0, 1, 2, 3 };
		metadata.configureField(nonEditable0,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[0];
					}
				});
		metadata.configureField(editable0,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[1];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[1] = value;
					}
				});
		metadata.configureField(nonEditable1,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[2];
					}
				});
		metadata.configureField(editable1,
				new SimpleTranslationMetadata.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[3];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[3] = value;
					}
				});

		assertEquals(false, metadata.isEditable(nonEditable0));
		assertEquals(false, metadata.isEditable(nonEditable1));
		assertEquals(true, metadata.isEditable(editable0));
		assertEquals(true, metadata.isEditable(editable1));
	}

}