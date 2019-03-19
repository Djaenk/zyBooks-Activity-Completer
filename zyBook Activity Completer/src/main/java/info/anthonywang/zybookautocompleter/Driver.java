package info.anthonywang.zybookautocompleter;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
//import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.support.ui.WebDriverWait;
//import org.openqa.selenium.firefox.FirefoxDriver;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.UserAgent;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.RequestHeaders;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;

class Driver {
	private WebDriver driver;
	private JavascriptExecutor js;
	private static BrowserMobProxy proxy;
	private Scanner scanner;
	private WebDriverWait wait;
	private String email;
	private String password;
	private String course_identifier;
	private String table_of_contents_url;
	private String chapter_selection;
	private String section_selection;

	Driver(){
		//System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
		//System.setProperty("phantomjs.binary.path", "phantomjs.exe");
		proxy = new BrowserMobProxyServer();
		proxy.start();
		//Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
		//DesiredCapabilities capabilities = new DesiredCapabilities();
		//capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
		//capabilities.setAcceptInsecureCerts(true);
		//driver = new PhantomJSDriver(capabilities);
		//driver = new FirefoxDriver(capabilities);
		ProxyConfig proxyConfig = new ProxyConfig(ProxyConfig.Type.HTTP, ClientUtil.getConnectableAddress().getCanonicalHostName(), proxy.getPort());
		Settings.Builder builder = new Settings.Builder();
		builder.screen(new Dimension(1600,900));
		builder.proxy(proxyConfig);
		builder.ssl("src/main/resources/ca-bundle-bmp.crt");
		builder.userAgent(UserAgent.CHROME);
		builder.requestHeaders(RequestHeaders.CHROME);
		builder.headless(false);
		builder.loggerLevel(Level.WARNING);
		builder.quickRender(false);
		driver = new JBrowserDriver(builder.build());
		proxy.setHarCaptureTypes(CaptureType.getAllContentCaptureTypes());
		proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
		js = (JavascriptExecutor) driver;
		scanner = new Scanner(System.in);
		wait = new WebDriverWait(driver, 30);
	}

	void login(){
		try{
			driver.get("https://learn.zybooks.com/signin");
		}
		catch(WebDriverException e){
			System.out.println("--Unable to load page, quitting--");
			e.printStackTrace();
			driver.quit();
			proxy.stop();
		}
		while(true){
			WebElement email_input = driver.findElement(By.cssSelector("input[type='email']"));
			WebElement password_input = driver.findElement(By.cssSelector("input[type='password']"));
			WebElement sign_in = driver.findElement(By.cssSelector("button.signin-button"));
			System.out.print("Enter zyBooks email:");
			//email = scanner.nextLine();
			email = "wanganthony.wang@gmail.com";
			System.out.print("Enter zyBooks password:");
			//password = scanner.nextLine();
			password = "0MgScv5lqj2T";
			email_input.sendKeys(email);
			password_input.sendKeys(password);
			DriverFunctions.jsClick(sign_in);
			DriverFunctions.waitUntilElementVisible(By.cssSelector("button[disabled='']"));
			DriverFunctions.waitUntilFinishedLoading();
			if(driver.findElements(By.cssSelector("button.signin-button[disabled='']")).size() != 0
			|| driver.findElements(By.xpath("//div[text()='Invalid email or password']")).size() != 0
			|| driver.findElements(By.xpath("//div[text()='Must specify a valid email address.']")).size() != 0
			|| driver.findElements(By.xpath("//div[text()='Password must be at least 8 characters.']")).size() != 0){
					System.out.println("--Invalid email or password--");
					email_input.clear();
					password_input.clear();
				}
			else{
				System.out.println("Login Successful");
				break;
			}
		}
	}

	void selectzyBook(){
		DriverFunctions.waitUntilFinishedLoading();
		while(true){
			System.out.print("Enter your course ID or the name of your course:");
			//course_identifier = scanner.nextLine().replace(" ", "");
			course_identifier = "CSE1342";
			List<WebElement> zybooks = driver.findElements(By.cssSelector("div.zybook"));
			try{
				WebElement zybook = driver.findElement(By.xpath("//a[contains(@href, '" + course_identifier + "')]"));
				DriverFunctions.jsClick(zybook);
				DriverFunctions.waitUntilFinishedLoading();
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
				DriverFunctions.jsClick(chapter);
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
				DriverFunctions.waitUntilFinishedLoading();
				driver.findElement(By.xpath("//h3[contains(text(), '" + chapter_selection + ".')]")).click();
				DriverFunctions.waitUntilElementVisible(By.cssSelector("ul.section-list"));
				sections = driver.findElements(By.xpath("//span[@class='section-title' and contains(text(), '" + chapter_selection + ".')]"));
				sections.get(i).click();
				System.out.println("Starting chapter " + chapter_selection + " section " + (i + 1) + "...");
				DriverFunctions.waitUntilSectionLoaded();
				completeActivities();
			}
		}
		else{
			WebElement section = driver.findElement(By.xpath("//span[@class='section-title' and contains(text(), '" + chapter_selection + "." + section_selection + "')]"));
			section.click();
			DriverFunctions.waitUntilSectionLoaded();
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
						Completers.completeMatching(js, wait, matching_activity);
					}
				}
			}
			else if(activity_class.contains("challenge")){
				if(activities.get(i).findElements(By.cssSelector("div.progressionTool")).size() != 0){
					Completers.completeProgression(driver, js, proxy, activities.get(i));
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
}