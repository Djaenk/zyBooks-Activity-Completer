package dev.djaenk.zybookcompleter;

import java.util.Scanner;
import java.util.ArrayList;
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
import org.bouncycastle.crypto.modes.KCCMBlockCipher;
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
	private List<String> zybookHeadings_ = new ArrayList<String>();
	private List<String> zybookCodes_ = new ArrayList<String>();
	private String currentZybook_ = "";
	private List<String> chapters_ = new ArrayList<String>();
	private String currentChapter_ = "";
	private List<List<String>> sections_ =
		new ArrayList<List<String>>();
	private String currentSection_ = "";

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
		zybookHeadings_.clear();
		zybookCodes_.clear();
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

	void loadSections() {
		if (email_.isEmpty()) {
			//must be logged in to load chapters/sections
			//throw exception
		}
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
			sections_.add(new ArrayList<String>());
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

	boolean selectSection(
			int chapterNumber,
			int sectionNumber) {
		if (chapterNumber > chapters_.size()) {
			return false;
		}
		if (sectionNumber
				> sections_.get(chapterNumber - 1).size()
		) {
			return false;
		}
		//check for logged in
		driver_.get("https://learn.zybooks.com/library");
		currentChapter_ = chapters_.get(chapterNumber - 1);
		currentSection_ =
			sections_.get(chapterNumber - 1)
			.get(sectionNumber - 1);
		WebElement chapter =
			driver_.findElement(
				By.xpath("//ul[1]/li[" + chapterNumber + "]")
			);
		chapter.findElement(By.xpath("/div[1]")).click();
		chapter.findElement(
			By.xpath("/div[2]//li[" + sectionNumber + "]")
		).click();
		return true;
	}

	void completeSection(
			int chapterNumber,
			int sectionNumber,
			boolean animation,
			boolean customInteraction,
			boolean multipleChoice,
			boolean shortAnswer,
			boolean answerDetection,
			boolean matching,
			boolean progression
	) {
		if (!selectSection(chapterNumber, sectionNumber)) {
			return;
		}

		List<WebElement> activities;
		if (animation) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.animation-player-content-resource"
					)
				);
			for (WebElement activity : activities) {
				completeAnimation(activity);
			}
		}
		if (customInteraction) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.custom-resource-payload"
					)
				);
			for (WebElement activity : activities) {
				completeCustomInteraction(activity);
			}
		}
		if (multipleChoice) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.multiple-choice-content-resource"
					)
				);
			for (WebElement activity : activities) {
				completeMultipleChoice(activity);
			}
		}
		if (shortAnswer) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.short-answer-content-resource"
					)
				);
			for (WebElement activity : activities) {
				completeShortAnswer(activity);
			}
		}
		if (answerDetection) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.detect-answer-content-resource"
					)
				);
			for (WebElement activity : activities) {
				completeAnswerDetection(activity);
			}
		}
		if (matching) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.definition-match-payload"
					)
				);
			for (WebElement activity : activities) {
				completeMatcing(activity);
			}
		}
		if (progression) {
			activities =
				driver_.findElements(
					By.cssSelector(
						"div.progressionTool"
					)
				);
			for (WebElement activity : activities) {
				completeProgression(activity);
			}
		}
	}

	void close(){
		driver_.quit();
		proxy_.stop();
	}

	private void waitUntilLoaded(){
		try{
			wait_.until(
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
			wait_.until(
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