package mvc.external;
public class PeselValidation {
		static private Boolean wrong=false;
		static private Boolean right=true;
		public static Boolean byString(String input)
		{
			// is input 11 long number
			if (!input.matches("^[0-9]{11}"))
				return wrong;
			
			// birth date validation
			int month =Integer.parseInt(input.substring(2, 4))%20;
			int day=Integer.parseInt(input.substring(4, 6));
			int[] monthDaysQuantity={31,28,31,30,31,30,31,31,30,31,30,31};
			if (Integer.parseInt(input.substring(0, 2))%4==0) // leap-year
				monthDaysQuantity[1]++;
			
			try 
			{
				if (monthDaysQuantity[month-1]<day || day<1)
					return wrong;
			}
			catch (Exception IndexOutOfBoundsException)
			{
				return wrong;
			}
			
			//checking validity
			int[] weights={9,7,3,1};
			int sum=0;
			for (int count=0; count<10; 
			sum+=weights[count%(weights.length)]*
			Character.getNumericValue(input.charAt(count)),
			count++);
			
			if (sum%10!=Character.getNumericValue(input.charAt(10)))
				return wrong;

			// input passed all validation
			return right;
		}
	}