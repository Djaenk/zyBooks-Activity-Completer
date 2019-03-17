package info.anthonywang.zybookautocompleter;

public class TestLauncher {
	public static void main(String[] args){
		Driver test = new Driver();
		test.login();
		test.selectzyBook();
		test.selectChapter();
		test.selectSection();
		test.navigateSection();
		test.close();
	}
}