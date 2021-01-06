package dev.djaenk.zybookcompleter;

import java.util.Scanner;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;

class Completer {
	private WebDriver driver_;
	private WebDriverWait wait_;
	private BrowserMobProxy proxy_;
	private String email_ = "";
	private List<String> zybookHeadings_ = new List<String>();
	private List<String> zybookCodes_ = new List<String>();
	private String currentZybook_ = "";
	private List<String> chapters_ = new List<String>();
	private List<List<String>> sections_ =
		new List<List<String>>();

	Completer() {
		proxy_ = new BrowserMobProxyServer();
		proxy_.setTrustAllServers(true);
		proxy_.enableHarCaptureTypes(
			CaptureType.RESPONSE_CONTENT
		);
		proxy_.start();

		DesiredCapabilities capabilities =
			new DesiredCapabilities();
		capabilities.setCapability(
			CapabilityType.PROXY,
			ClientUtil.createSeleniumProxy(proxy_)
		);
		capabilities.setCapability(
			CapabilityType.SUPPORTS_JAVASCRIPT,
			true
		);
		driver_ = new HtmlUnitDriver(capabilities);
		wait_ = new WebDriverWait(driver_, 10);
	}

	String login(String email, String password) {
		if(!email.isEmpty()){
			driver_.findElement(By.className("zb-menu")).click();
			driver_
			.findElement(By.className("signout-button"))
			.click();
			email_ = "";
		}
		
		driver_.get("https://learn.zybooks.com/signin");
		WebElement emailInput =
			driver_.findElement(
				By.cssSelector("input[type='email']")
			);
		WebElement passwordInput =
			driver_.findElement(
				By.cssSelector("input[type='password']")
			);
		WebElement signInButton =
			driver_.findElement(
				By.cssSelector("button.signin-button")
			);
		emailInput.sendKeys(email);
		passwordInput.sendKeys(password);
		signInButton.click();

		waitUntilDisabled(signInButton);
		waitUntilLoaded();
		if (driver_.getCurrentUrl().contains("library")){
			email_ = email;
			return "";
		} else {
			WebElement error =
				driver_.findElement(
					By.cssSelector("div[role='alert']")
				);
			return error.getText();
		}
	}

	void loadZybooks() {
		if (email_.isEmpty()) {
			//must be logged in to retrieve zybooks
			//throw exception
		}
		library_.clear();
		driver_.get("https://learn.zybooks.com/library");
		List<WebElement> zybooks =
			driver_.findElements(
				By.cssSelector("div[zybookcode]")
			);
		for (WebElement zybook : zybooks) {
			WebElement heading =
				zybook.findElement(
					By.className("heading")
				);
			zybookHeadings_.add(heading.getText());
			zybookCodes_.add(getAttribute("zybookcode"));
		}
	}

	void selectZybook(int index) {
		if (email_.isEmpty()) {
			//must be logged in to select zybook
			//throw exception
		}
		currentZybook_ = zybookCodes_.get(index);
	}

	void loadChaptersAndSections() {
		chapters_.clear();
		sections_.clear();
		driver_.get(
			"https://learn.zybooks.com/zybook/" + currentZybook_
		);
		WebElement tableOfContents =
			driver_.findElement(
				By.cssSelector("ul.table-of-contents-list")
			);

		List<WebElement> chapters =
			tableOfContents.findElements(
				By.cssSelector("li.chapter-item")
			);
		for (WebElement chapter : chapters) {
			WebElement chapterTitle =
				chapter.findElement(
					By.cssSelector("span.chapter-title")
				);
			chapters_.add(chapterTitle.getText());

			chapter.click();
			WebElement sectionList =
				chapter.findElement(
					By.cssSelector("ul.section-list")
				);

			List<WebElement> sections =
				sectionList.findElements(
					By.cssSelector("li.section-item")
				);
			sections_.add(new List<String>());
			for (WebElement section : sections) {
				WebElement sectionTitle =
					section.findElement(
						By.cssSelector("span.section-title")
					);
				sections_.get(sections_.size() - 1).add(
					sectionTitle.getText()
				);
			}
		}
	}

	void selectzyBook(){
		DriverFunctions.waitUntilFinishedLoading(driver);
		while(true){
			System.out.print("Enter your course ID or the name of your course:");
			//course_identifier = scanner.nextLine().replace(" ", "");
			List<WebElement> zybooks = driver.findElements(By.cssSelector("div.zybook"));
			try{
				WebElement zybook = driver.findElement(By.xpath("//a[contains(@href, '" + course_identifier + "')]"));
				DriverFunctions.jsClick(driver, zybook);
				DriverFunctions.waitUntilFinishedLoading(driver);
				table_of_contents_url = driver.getCurrentUrl();
				break;
			}
			catch(NoSuchElementException e){
				System.out.println("--Invalid course--");
			}
		}
		System.out.println("zyBook Selected");
	}

	void selectChapter(){
		while(true){
			System.out.print("Enter the chapter to complete: ");
			//chapter_selection = scanner.nextLine();
			chapter_selection = "8";
			try{
				WebElement chapter = driver.findElement(By.xpath("//h3[contains(text(), '" + chapter_selection + ".')]"));
				DriverFunctions.jsClick(driver, chapter);
				break;
			}
			catch(NoSuchElementException e){
				System.out.println("--Invalid chapter--");
			}
		}
	}

	void selectSection(){
		while(true){
			System.out.print("Enter the section to complete. Enter \"all\" to complete all sections: ");
			//section_selection = scanner.nextLine();
			section_selection = "1";
			if(section_selection.equals("all")){
				break;
			}
			try{
				Integer.parseInt(section_selection);
				break;
			}
			catch(NumberFormatException e){
				System.out.println("--Invalid section--");
			}
		}
	}

	void navigateSection(){
		if(section_selection.equals("all")){
			List<WebElement> sections = driver.findElements(By.xpath("//span[@class='section-title' and contains(text(), '" + chapter_selection + ".')]"));
			for(int i = 0; i < sections.size(); i++){
				driver.get(table_of_contents_url);
				DriverFunctions.waitUntilFinishedLoading(driver);
				driver.findElement(By.xpath("//h3[contains(text(), '" + chapter_selection + ".')]")).click();
				DriverFunctions.waitUntilElementVisible(driver, By.cssSelector("ul.section-list"));
				sections = driver.findElements(By.xpath("//span[@class='section-title' and contains(text(), '" + chapter_selection + ".')]"));
				DriverFunctions.jsClick(driver, sections.get(i));
				System.out.println("Starting chapter " + chapter_selection + " section " + (i + 1) + "...");
				DriverFunctions.waitUntilSectionLoaded(driver);
				completeActivities();
			}
		}
		else{
			WebElement section = driver.findElement(By.xpath("//span[@class='section-title' and contains(text(), '" + chapter_selection + "." + section_selection + "')]"));
			DriverFunctions.jsClick(driver, section);
			DriverFunctions.waitUntilSectionLoaded(driver);
			completeActivities();
		}
	}

	private void completeActivities(){
		List<WebElement> activities = driver.findElements(By.cssSelector("div.interactive-activity-container"));
		for(int i = 0; i < activities.size(); i++){
			String activity_class = activities.get(i).getAttribute("class");
			if(activity_class.contains("participation")){
				if(activity_class.contains("animation-player-content-resource")){
					Completers.completeAnimation(activities.get(i));
				}
				else if(activity_class.contains("multiple-choice-content-resource")){
					Completers.completeMultipleChoice(activities.get(i));
				}
				else if(activity_class.contains("short-answer-content-resource")){
					Completers.completeShortAnswer(activities.get(i));
				}
				else if(activity_class.contains("detect-answer-content-resource")){
					Completers.completeAnswerDetection(activities.get(i));
				}
				else if(activity_class.contains("custom-content-resource")){
					if(activities.get(i).findElements(By.cssSelector("div.definition-match-payload")).size() != 0){
						WebElement matching_activity = activities.get(i).findElement(By.cssSelector("div.definition-match-payload"));
						Completers.completeMatching(driver, matching_activity);
					}
				}
			}
			else if(activity_class.contains("challenge")){
				if(activities.get(i).findElements(By.cssSelector("div.progressionTool")).size() != 0){
					Completers.completeProgression(driver, proxy, activities.get(i));
				}
			}
		}
	}
	
	void close(){
		driver.quit();
		System.out.println("Closed driver");
		proxy.stop();
		System.out.println("Closed proxy");
		scanner.close();
	}

	private void waitUntilLoaded(){
		try{
			wait.until(
				ExpectedConditions.invisibilityOfElementLocated(
					By.cssSelector(".zb-progress-circular.orange")
				)
			);
		}
		catch(TimeoutException e){
			System.err.println("Failed to load");
			e.printStackTrace();
		}
	}

	private void waitUntilDisabled(WebElement element){
		try{
			wait.until(
				ExpectedConditions.attributeToBe(
					element,
					"disabled",
					""
				)
			);
		}
		catch(TimeoutException e){
			System.err.println("Timed out while waiting");
			e.printStackTrace();
		}
	}
}