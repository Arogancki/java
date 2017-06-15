public class PeselValidationTest {
	public static void main(String[] args) {
	if (args.length==0)
	{
		System.err.println("Missing argument");
		return;
	}
	System.out.println("Pesel is "+PeselValidation.byString(args[0])+".");
	}
}

// javac PeselValidation.java PeselValidationTest.java
// java PeselValidationTest [pesel]