package info.anthonywang.zybookautocompleter;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
		Driver test = new Driver();
		test.login();
		test.selectzyBook();
		test.selectChapter();
		test.selectSection();
		//test.navigateSection();
		//test.close();
        assertTrue( true );
    }
}
