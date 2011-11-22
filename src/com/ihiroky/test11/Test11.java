package com.ihiroky.test11;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Hiroki Itoh
 */
public class Test11 {

	public static void run(Class c) {
		run(new Class[]{c});
	}

	public static void run(Class[] classes) {
		
		try {
			int total = 0;
			Hashtable failures = new Hashtable();
			Hashtable errors = new Hashtable();
			for (int i=0; i<classes.length; i++) {
				Class c = classes[i];
				invoke(c, null, "beforeClass");
				Method[] methods = c.getMethods();
				for (int j=0; j<methods.length; j++) {
					Method method = methods[j];
					if (method.getName().startsWith("test")) {
						String testName = c.getName() + "#" + method.getName();
						Object instance = c.newInstance();
						try {
							invoke(c, instance, "before");
							try { method.invoke(instance, null); }
							finally { invoke(c, instance, "after"); }
						} catch (InvocationTargetException ite) {
							Throwable t = ite.getTargetException();
							if (t instanceof AssertFailException) {
								System.err.println(" [run] failure : " + testName);
								failures.put(testName, t);
							} else {
								System.err.println(" [run] error   : " + testName);
								errors.put(testName, new TestErrorException(t));
							}
						}
						total++;
					}
				}
				invoke(c, null, "afterClass");
			}

			Thread.sleep(100);
			printSummary(total, failures.size(), errors.size());
			printNotOK(failures, "failures", "FAILED");
			printNotOK(errors, "errors", "ERROR");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void printSummary(int total, int failure, int error) {
		String msg = "| Test11 summary - tests:" + total + ", failures:" + failure + ", error:" + error + " |";
		String bar = repeat('-', msg.length() - 2);
		System.out.println('+' + bar + "+");
		System.out.println(msg);
		System.out.println('+' + bar + "+");
	}

	private static void printNotOK(Hashtable h, String header, String marker) {
		if (h.size() == 0) {
			return;
		}
		System.out.println(header + " : ");
		for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			Throwable t = (Throwable)h.get(key);
			System.out.println(" - " + marker + " : " + key);
			t.printStackTrace(System.out);
			System.out.println();
		}
	}

	private static String repeat(char c, int times) {
		char[] a = new char[times];
		for (int i=0; i<times; i++) {
			a[i] = c;
		}
		return new String(a);
	}

	private static void invoke(Class c, Object instance, String method) throws Exception {
		Method m = null;
		try {
			m = c.getMethod(method, null);
		} catch (Exception e) {
		}
		if (m != null) {
			m.invoke(instance, null);
		}
	}


	private static void assertError(Object value, Object expected) {
		throw new AssertFailException(
				"assertion error. value : [" + String.valueOf(value) + "], expected : [" + String.valueOf(expected) + "]");
	}

	private static void assertErrorWithClass(Object value, Object expected) {
		String vc = (value != null) ? value.getClass().getName() : "null";
		String ec = (expected != null) ? expected.getClass().getName() : "null";
		throw new AssertFailException(
				"assertion error. value : ["+ String.valueOf(value) + "](" + vc + "), expected: ["
						+ String.valueOf(expected) + "](" + ec + ")");
	}

	public static void fail() {
		throw new AssertFailException("");
	}

	public static void fail(String message) {
		throw new AssertFailException(message);
	}

	public static void assertNotNull(Object value) {
		if (value == null) {
			assertError(value, "not null");
		}
	}

	public static void assertNull(Object value) {
		if (value != null) {
			assertError(value, "null");
		}
	}

	public static void assertInstanceOf(Object value, Class klass) {
		if (value == null || ! klass.isAssignableFrom(value.getClass())) {
			assertError((value != null) ? value.getClass() : null, klass);
		}
	}

	public static void assertEquals(char value, char expected) {
		if (value != expected) {
			assertError(new Character(value).toString(), new Character(expected).toString());
		}
	}

	public static void assertEquals(long value, long expected) {
		if (value != expected) {
			assertError(new Long(value).toString(), new Long(expected).toString());
		}
	}

	public static void assertEquals(double value, double expected) {
		if (value != expected) {
			assertError(new Double(value), new Double(expected));
		}
	}

	public static void assertTrue(boolean value) {
		if ( ! value) {
			throw new AssertFailException("value is false.");
		}
	}

	public static void assertFalse(boolean value) {
		if (value) {
			throw new AssertFailException("value is true.");
		}
	}

	public static void assertSame(Object value, Object expected) {
		if (value != expected) {
			assertErrorWithClass(value, expected);
		}
	}

	public static void assertEquals(Object value, Object expected) {
		if (value == null && expected == null) {
			return;
		}
		if ((value == null && expected != null) ||  ! value.equals(expected)) {
			assertErrorWithClass(value, expected);
		}
	}

	public static void assertArrayEquals(Object[] value, Object[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(value[i], expected[i]); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(Object[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	public static void assertArrayEquals(boolean[] value, boolean[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(new Boolean(value[i]), new Boolean(expected[i])); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(boolean[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	public static void assertArrayEquals(short[] value, short[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(value[i], expected[i]); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(short[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	public static void assertArrayEquals(int[] value, int[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(value[i], expected[i]); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(int[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	public static void assertArrayEquals(long[] value, long[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(value[i], expected[i]); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(long[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	public static void assertArrayEquals(float[] value, float[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(value[i], expected[i]); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(float[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	public static void assertArrayEquals(double[] value, double[] expected) {
		if ((value == null && expected != null) || (value != null && expected == null)) {
			assertErrorWithClass(value, expected);
		}
		if (value == null && expected == null) {
			return;
		}
		int min = Math.min(value.length, expected.length);
		boolean error = false;
		for (int i=0; i<min; i++) {
			try { assertEquals(value[i], expected[i]); }
			catch (AssertFailException afe) { error = true; break; }
		}
		error |= (value.length != expected.length);
		if (error) {
			assertError(toString(value), toString(expected));
		}
	}

	private static String toString(double[] a) {
		if (a == null) {
			return "null";
		}

		StringBuffer b = new StringBuffer();
		b.append('[');
		if (a.length > 0) {
			b.append(a[0]);
		}
		for (int i=1; i<a.length; i++) {
			b.append(", ").append(a[i]);
		}
		b.append(']').append(", length:").append(a.length);
		return b.toString();
	}

	private static class AssertFailException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3849231720885137391L;

		public AssertFailException(String message) {
			super(message);
		}
	}
	
	private static class TestErrorException extends RuntimeException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1730987797353911516L;

		private String message;
		private Throwable cause;

		public TestErrorException(Throwable cause) {
			this("", cause);
		}

		public TestErrorException(String message, Throwable cause) {
			this.message = message;
			this.cause = cause;
		}

		public void printStackTrace() {
			printStackTrace(System.err);
		}

		public void printStackTrace(PrintStream out) {
			out.print(message + " caused by : ");
			cause.printStackTrace(out);
		}

		public void printStackTrace(PrintWriter writer) {
			writer.print(message + " caused by : ");
			cause.printStackTrace(writer);
		}
	}
}
