package org.jwat.warc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.jwat.warc.WarcValidationError;

@RunWith(Parameterized.class)
public class TestMissingHeadersAll {

	private int expected_records;
	private String warcFile;
	private Set<String> fieldNamesSet;

	@Parameters
	public static Collection<Object[]> configs() {
		return Arrays.asList(new Object[][] {
				{2, "test-lonely-warcinfo-metadata.warc", "WARC-Record-ID,WARC-Date,Content-Length"},
				{4, "test-lonely-request-response-resource-conversion.warc", "WARC-Record-ID,WARC-Date,Content-Length,WARC-Target-URI"},
				{1, "test-lonely-continuation.warc", "WARC-Record-ID,WARC-Date,Content-Length,WARC-Target-URI,WARC-Segment-Number,WARC-Segment-Origin-ID"},
				{1, "test-lonely-revisit.warc", "WARC-Record-ID,WARC-Date,Content-Length,WARC-Target-URI,WARC-Profile"},
				{1, "test-lonely-monkeys.warc", "WARC-Type,WARC-Record-ID,WARC-Date,Content-Length,WARC-Type"}
		});
	}

	public TestMissingHeadersAll(int records, String warcFile, String fieldNames) {
		this.expected_records = records;
		this.warcFile = warcFile;
		this.fieldNamesSet = new HashSet<String>(Arrays.asList((fieldNames.split(",", -1))));
	}

	@Test
	public void test() {
		boolean bDebugOutput = System.getProperty("jwat.debug.output") != null;

		InputStream in;

		int records = 0;
		int errors = 0;

		try {
			in = this.getClass().getClassLoader().getResourceAsStream(warcFile);

			WarcReader reader = WarcReaderFactory.getReader(in);
			WarcRecord record;

			while ((record = reader.getNextRecord()) != null) {
				if (bDebugOutput) {
					RecordDebugBase.printRecord(record);
					RecordDebugBase.printRecordErrors(record);
				}

				record.close();

				++records;

				if (record.hasErrors()) {
					errors += record.getValidationErrors().size();

					Assert.assertTrue(fieldNamesSet.containsAll(
							filter(record.getValidationErrors())
							));
				}
				else {
					Assert.fail("There must be errors.");
				}
			}

			reader.close();
			in.close();

			if (bDebugOutput) {
				RecordDebugBase.printStatus(records, errors);
			}
		}
		catch (FileNotFoundException e) {
			Assert.fail("Input file missing");
		}
		catch (IOException e) {
			Assert.fail("Unexpected io exception");
		}

		Assert.assertEquals(expected_records, records);
		//Assert.assertEquals(0, errors);
	}

	public List<String> filter(Collection<WarcValidationError> errors) {
		List<String> fields = new ArrayList<String>();
		for (WarcValidationError error : errors) {
			fields.add(error.field);
		}
		return fields;
	}

}