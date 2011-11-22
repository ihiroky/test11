package com.ihiroky.test11;


/**
 * @author Hiroki Itoh
 */
public class SampleTest extends Test11 {

	public static void beforeClass() {
		System.out.println("beforeClass.");
	}

	public static void afterClass() {
		System.out.println("afterClass.");
	}

	public void before() {
		System.out.println("before.");
	}

	public void after() {
		System.out.println("after.");
	}

	public void testOK() {
		System.out.println("testOK.");
		long a = 0;
		assertEquals(a, 0);
		assertInstanceOf(new Integer(0), Number.class);
		assertInstanceOf(new Integer(0), Integer.class);
	}

	public void testFail() {
		System.out.println("testFail.");
		String a = "a";
		assertEquals(a, "b");
	}

	public void testError() {
		System.out.println("testError.");
		throw new RuntimeException("test error.");
	}

	public void methodNotStartsWith_test() {
		System.out.println("methodNotStartsWith_test");
	}

	// for test,  type Alt + Shift + x -> j
	// for debug, type Alt + Shift + d -> j
	public static void main(String[] args) {
		run(new Class[]{SampleTest.class});
	}
}
