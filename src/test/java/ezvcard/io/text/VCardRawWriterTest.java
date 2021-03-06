package ezvcard.io.text;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.parameter.Encoding;
import ezvcard.parameter.VCardParameters;
import ezvcard.util.org.apache.commons.codec.net.QuotedPrintableCodec;

/*
 Copyright (c) 2012-2016, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
@SuppressWarnings("resource")
public class VCardRawWriterTest {
	@Test
	public void writeBeginComponent() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeBeginComponent("COMP");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"BEGIN:COMP\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void writeEndComponent() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeEndComponent("COMP");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"END:COMP\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void writeVersion() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeVersion();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"VERSION:2.1\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	/*
	 * TYPE parameters for 2.1 vCards should look like this:
	 * 
	 * ADR;WORK;DOM:
	 * 
	 * TYPE parameters for 3.0 vCards should look like this:
	 * 
	 * ADR;TYPE=work,dom:
	 */
	@Test
	public void type_parameter() throws Throwable {
		//@formatter:off
		assertTypeParameter(VCardVersion.V2_1,
		"PROP;ONE:\r\n" +
		"PROP;ONE;TWO:\r\n" +
		"PROP;ONE;TWO;THREE:\r\n"
		);
		
		assertTypeParameter(VCardVersion.V3_0,
		"PROP;TYPE=one:\r\n" +
		"PROP;TYPE=one,two:\r\n" +
		"PROP;TYPE=one,two,three:\r\n"
		);
		
		assertTypeParameter(VCardVersion.V4_0,
		"PROP;TYPE=one:\r\n" +
		"PROP;TYPE=one,two:\r\n" +
		"PROP;TYPE=one,two,three:\r\n"
		);
		//@formatter:on
	}

	private void assertTypeParameter(VCardVersion version, String expected) throws IOException {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, version);

		VCardParameters parameters = new VCardParameters();
		parameters.put(VCardParameters.TYPE, "one");
		writer.writeProperty(null, "PROP", parameters, "");

		parameters = new VCardParameters();
		parameters.putAll(VCardParameters.TYPE, Arrays.asList("one", "two"));
		writer.writeProperty(null, "PROP", parameters, "");

		parameters = new VCardParameters();
		parameters.putAll(VCardParameters.TYPE, Arrays.asList("one", "two", "three"));
		writer.writeProperty(null, "PROP", parameters, "");

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void parameters() throws Throwable {
		//@formatter:off
		assertParameters(VCardVersion.V2_1,
		"PROP;SINGLE=one:\r\n" +
		"PROP;MULTIPLE=one;MULTIPLE=two:\r\n" +
		"PROP;SINGLE=one;MULTIPLE=one;MULTIPLE=two:\r\n"
		);
		
		assertParameters(VCardVersion.V3_0,
		"PROP;SINGLE=one:\r\n" +
		"PROP;MULTIPLE=one,two:\r\n" +
		"PROP;SINGLE=one;MULTIPLE=one,two:\r\n"
		);
		
		assertParameters(VCardVersion.V4_0,
		"PROP;SINGLE=one:\r\n" +
		"PROP;MULTIPLE=one,two:\r\n" +
		"PROP;SINGLE=one;MULTIPLE=one,two:\r\n"
		);
		//@formatter:on
	}

	private void assertParameters(VCardVersion version, String expected) throws IOException {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, version);

		VCardParameters parameters = new VCardParameters();
		parameters.put("SINGLE", "one");
		writer.writeProperty(null, "PROP", parameters, "");

		parameters = new VCardParameters();
		parameters.put("MULTIPLE", "one");
		parameters.put("MULTIPLE", "two");
		writer.writeProperty(null, "PROP", parameters, "");

		parameters = new VCardParameters();
		parameters.put("SINGLE", "one");
		parameters.put("MULTIPLE", "one");
		parameters.put("MULTIPLE", "two");
		writer.writeProperty(null, "PROP", parameters, "");

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void foldingScheme() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);
		writer.getFoldedLineWriter().setLineLength(50);
		writer.getFoldedLineWriter().setIndent("  ");

		writer.writeProperty("PROP", "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:The vCard MIME Directory Profile also provide\r\n" +
		"  s support for representing other important infor\r\n" +
		"  mation about the person associated with the dire\r\n" +
		"  ctory entry. For instance, the date of birth of \r\n" +
		"  the person; an audio clip describing the pronunc\r\n" +
		"  iation of the name associated with the directory \r\n" +
		"  entry, or some other application of the digital \r\n" +
		"  sound; longitude and latitude geo-positioning in\r\n" +
		"  formation related to the person associated with \r\n" +
		"  the directory entry; date and time that the dire\r\n" +
		"  ctory information was last updated; annotations \r\n" +
		"  often written on a business card; Uniform Resour\r\n" +
		"  ce Locators (URL) for a website; public key info\r\n" +
		"  rmation.\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void no_foldingScheme() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);
		writer.getFoldedLineWriter().setLineLength(null);

		writer.writeProperty("PROP", "The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:The vCard MIME Directory Profile also provides support for representing other important information about the person associated with the directory entry. For instance, the date of birth of the person; an audio clip describing the pronunciation of the name associated with the directory entry, or some other application of the digital sound; longitude and latitude geo-positioning information related to the person associated with the directory entry; date and time that the directory information was last updated; annotations often written on a business card; Uniform Resource Locators (URL) for a website; public key information.\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void newline() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);
		writer.getFoldedLineWriter().setNewline("*");

		writer.writeProperty("PROP", "one");
		writer.writeProperty("PROP", "two");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:one*" +
		"PROP:two*";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void groups() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

		writer.writeProperty("group1", "PROP", new VCardParameters(), "");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"group1.PROP:\r\n";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void invalid_group_name_characters() throws Throwable {
		for (VCardVersion version : VCardVersion.values()) {
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			for (char c : ".;:\n\r".toCharArray()) {
				try {
					writer.writeProperty("GROUP" + c, "PROP", new VCardParameters(), "");
					fail("IllegalArgumentException expected with character '" + c + "' and version " + version + ".");
				} catch (IllegalArgumentException e) {
					//expected
				}
			}
		}
	}

	@Test
	public void invalid_group_name_whitespace() throws Throwable {
		for (VCardVersion version : VCardVersion.values()) {
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			for (char c : " \t".toCharArray()) {
				try {
					writer.writeProperty(c + "GROUP", "PROP", new VCardParameters(), "");
					fail("IllegalArgumentException expected with character '" + c + "' and version " + version + ".");
				} catch (IllegalArgumentException e) {
					//expected
				}
			}
		}
	}

	@Test
	public void invalid_property_name_characters() throws Throwable {
		for (VCardVersion version : VCardVersion.values()) {
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			for (char c : ".;:\n\r".toCharArray()) {
				try {
					writer.writeProperty("PROP" + c, "");
					fail("IllegalArgumentException expected with character '" + c + "' and version " + version + ".");
				} catch (IllegalArgumentException e) {
					//expected
				}
			}
		}
	}

	@Test
	public void invalid_property_name_whitespace() throws Throwable {
		for (VCardVersion version : VCardVersion.values()) {
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			for (char c : " \t".toCharArray()) {
				try {
					writer.writeProperty(c + "PROP", "");
					fail("IllegalArgumentException expected with character '" + c + "' and version " + version + ".");
				} catch (IllegalArgumentException e) {
					//expected
				}
			}
		}
	}

	@Test
	public void invalid_parameter_name_characters() throws Throwable {
		for (VCardVersion version : VCardVersion.values()) {
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			for (char c : ";:=\n\r".toCharArray()) {
				VCardParameters parameters = new VCardParameters();
				parameters.put("NAME" + c, "value");
				try {
					writer.writeProperty(null, "PROP", parameters, "");
					fail("IllegalArgumentException expected with character '" + c + "' and version " + version + ".");
				} catch (IllegalArgumentException e) {
					//expected
				}
			}
		}
	}

	@Test
	public void invalid_parameter_value_characters() throws Throwable {
		assert_invalid_parameter_value_characters(V2_1, ",:\r\n", false, true);
		assert_invalid_parameter_value_characters(V2_1, ",:\r\n", true, true);

		assert_invalid_parameter_value_characters(V3_0, "\"\r\n", false, true);
		assert_invalid_parameter_value_characters(V3_0, "\"\r\n", true, false);

		assert_invalid_parameter_value_characters(V4_0, "\"", false, true);
		assert_invalid_parameter_value_characters(V4_0, "\"", true, false);
		assert_invalid_parameter_value_characters(V4_0, "\r\n", false, false);
		assert_invalid_parameter_value_characters(V4_0, "\r\n", true, false);
	}

	private void assert_invalid_parameter_value_characters(VCardVersion version, String characters, boolean caretEncoding, boolean exceptionExpected) throws IOException {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, version);
		writer.setCaretEncodingEnabled(caretEncoding);
		for (char c : characters.toCharArray()) {
			VCardParameters parameters = new VCardParameters();
			parameters.put("NAME", "value" + c);
			try {
				writer.writeProperty(null, "PROP", parameters, "");
				if (exceptionExpected) {
					fail("IllegalArgumentException expected with character '" + c + "'.");
				}
			} catch (IllegalArgumentException e) {
				if (!exceptionExpected) {
					fail("IllegalArgumentException not expected with character '" + c + "'.");
				}
			}
		}
	}

	@Test
	public void parameters_special_chars() throws Throwable {
		//2.1 without caret escaping
		//replaces \ with \\
		//replaces ; with \;
		assertParametersSpecialChars("^\\;=[]\"\t" + ((char) 28), V2_1, false, "PROP;X-TEST=^\\\\\\;=[]\"\t" + ((char) 28) + ";X-TEST=normal:\r\n");

		//2.1 with caret escaping (ignored)
		//replaces \ with \\
		//replaces ; with \;
		assertParametersSpecialChars("^\\;=[]\"\t" + ((char) 28), V2_1, true, "PROP;X-TEST=^\\\\\\;=[]\"\t" + ((char) 28) + ";X-TEST=normal:\r\n");

		//3.0 without caret escaping
		//surrounds in double quotes, since it contains , ; or :
		assertParametersSpecialChars("^\\,;:=[]\t" + ((char) 28), V3_0, false, "PROP;X-TEST=\"^\\,;:=[]\t" + ((char) 28) + "\",normal:\r\n");

		//3.0 with caret escaping (same as 4.0)
		//replaces ^ with ^^
		//replaces newline with ^n
		//replaces " with ^'
		//surrounds in double quotes, since it contains , ; or :
		assertParametersSpecialChars("^\\,;:=[]\"\t\n" + ((char) 28), V3_0, true, "PROP;X-TEST=\"^^\\,;:=[]^'\t^n" + ((char) 28) + "\",normal:\r\n");

		//4.0 without caret escaping
		//replaces newline with \n
		//surrounds in double quotes, since it contains , ; or :
		assertParametersSpecialChars("^\\,;:=[]\t\n" + ((char) 28), V4_0, false, "PROP;X-TEST=\"^\\,;:=[]\t\\n" + ((char) 28) + "\",normal:\r\n");

		//4.0 with caret escaping
		//replaces ^ with ^^
		//replaces newline with ^n
		//replaces " with ^'
		//surrounds in double quotes, since it contains , ; or :
		assertParametersSpecialChars("^\\,;:=[]\"\t\n" + ((char) 28), V4_0, true, "PROP;X-TEST=\"^^\\,;:=[]^'\t^n" + ((char) 28) + "\",normal:\r\n");
	}

	private void assertParametersSpecialChars(String paramValue, VCardVersion version, boolean caretEncodingEnabled, String expected) throws IOException {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, version);
		writer.setCaretEncodingEnabled(caretEncodingEnabled);

		VCardParameters parameters = new VCardParameters();
		parameters.put("X-TEST", paramValue);
		parameters.put("X-TEST", "normal");
		writer.writeProperty(null, "PROP", parameters, "");

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void null_property_value() throws Throwable {
		for (VCardVersion version : VCardVersion.values()) {
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, version);
			writer.writeProperty("PROP", null);

			String actual = sw.toString();
			String expected = "PROP:\r\n";
			assertEquals(actual, expected);
		}
	}

	/*
	 * If newline characters exist in a property value in 2.1, then that
	 * property value should be "quoted-printable" encoded. The escape sequence
	 * "\n" should ONLY be used for 3.0 and 4.0. See the "Delimiters" subsection
	 * in section 2 of the 2.1 specs.
	 */
	@Test
	public void newlines_in_property_values() throws Throwable {
		assertNewlinesInPropertyValues(VCardVersion.V2_1, "PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n");
		assertNewlinesInPropertyValues(VCardVersion.V3_0, "PROP:one\\ntwo\r\n");
		assertNewlinesInPropertyValues(VCardVersion.V4_0, "PROP:one\\ntwo\r\n");
	}

	private void assertNewlinesInPropertyValues(VCardVersion version, String expected) throws IOException {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, version);

		writer.writeProperty("PROP", "one\r\ntwo");

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	/*
	 * Property values that use "quoted-printable" encoding must include a "="
	 * at the end of the line if the next line is folded.
	 */
	@Test
	public void quoted_printable_line() throws Throwable {
		StringWriter sw = new StringWriter();
		VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);
		writer.getFoldedLineWriter().setLineLength(60);

		VCardParameters parameters = new VCardParameters();
		parameters.setEncoding(Encoding.QUOTED_PRINTABLE);

		writer.writeProperty(null, "PROP", parameters, "quoted-printable \r\nline");
		writer.writeProperty(null, "PROP", parameters, "short");
		writer.close();

		//must construct the first line differently, since the length of the CHARSET parameter will vary depending on the local machine
		String firstLine = "PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:quoted-printable =0D=0Aline";
		firstLine = firstLine.substring(0, 59) + "=\r\n " + firstLine.substring(59);

		//@formatter:off
		String expected = firstLine + "\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:short\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void quoted_printable_line_encoding() throws Throwable {
		final String propValue = "\u00e4\u00f6\u00fc\u00df";

		//UTF-8
		{
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

			VCardParameters parameters = new VCardParameters();
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			parameters.setCharset("UTF-8");

			writer.writeProperty(null, "PROP", parameters, propValue);
			writer.close();

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C3=A4=C3=B6=C3=BC=C3=9F\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}

		//ISO-8859-1
		{
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

			VCardParameters parameters = new VCardParameters();
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			parameters.setCharset("ISO-8859-1");

			writer.writeProperty(null, "PROP", parameters, propValue);
			writer.close();

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=ISO-8859-1:=E4=F6=FC=DF\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}

		//invalid
		{
			StringWriter sw = new StringWriter();
			VCardRawWriter writer = new VCardRawWriter(sw, VCardVersion.V2_1);

			VCardParameters parameters = new VCardParameters();
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			parameters.setCharset("invalid");

			writer.writeProperty(null, "PROP", parameters, propValue);
			writer.close();

			QuotedPrintableCodec codec = new QuotedPrintableCodec("UTF-8");
			String encoded = codec.encode(propValue);

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:" + encoded + "\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}
}
