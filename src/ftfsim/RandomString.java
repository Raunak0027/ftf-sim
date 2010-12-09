package ftfsim;
import java.util.Random;

public class RandomString {
	private static int maxStringLength = 100;
	
	public static String generateRandomString() {
		Random random = new Random();
		String result = "";
		int length = random.nextInt(maxStringLength);
		for (int i=0; i<length; i++) {
			result.concat(Integer.toString(random.nextInt()));
		}
		
		return result;
	}
}
