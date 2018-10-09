import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Tests
{

	private int extractNumber(String s)
	{
		return Integer.parseInt(s.replaceAll("[^0-9]", ""));
	}
	
	@Test
	void testBinSearch() 
	{
		ChatBot bot = new ChatBot(new GameFactory());
		int[] testCases = {100, 99, 1, 0};
		for (int x : testCases) 
		{
			ChatBotReply botAnswer = bot.answer("старт", 1);
			int guessNumber;
			do
			{
				if (botAnswer.message.charAt(0) != 'И' && botAnswer.message.charAt(0) != 'М')
					fail("Expected guessing number");
				guessNumber = extractNumber(botAnswer.message);
				if (guessNumber > x) 
					botAnswer = bot.answer("<", 1);
				else
					botAnswer = bot.answer(">", 1);
				
			} while(x != guessNumber);	
			bot.answer("стоп", 1);
		}
	}	
	
	@Test
	void badNumber() 
	{
		ChatBot bot = new ChatBot(new GameFactory());
		ChatBotReply botAnswer = bot.answer("старт", 1);
		do
		{
			if (botAnswer.message.charAt(0) != 'И' && botAnswer.message.charAt(0) != 'М')
				fail("Expected guessing number");
			botAnswer = bot.answer("<", 1);
		} while(!botAnswer.message.equals("Ты меня обманываешь"));
	}

	@Test
	void badCommand()
	{
		ChatBot bot = new ChatBot(new GameFactory());
		ChatBotReply botAnswer = bot.answer("ла-ла-ла", 1);
		assertEquals("Команда не распознана. Попробуй ещё раз или воспользуйся помощью.", botAnswer.message);
	}
	
}
