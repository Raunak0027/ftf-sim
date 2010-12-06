package ftfsim;
import java.util.Random;

public class RandomString {
	private int maxStringLength = 100;
	
	public String generateRandomString() {
		Random random = new Random();
		String result = "";
		int length = random.nextInt(maxStringLength);
		for (int i=0; i<maxStringLength; i++) {
			result.concat(Integer.toString(random.nextInt()));
		}
		
		return result;
	}
}
