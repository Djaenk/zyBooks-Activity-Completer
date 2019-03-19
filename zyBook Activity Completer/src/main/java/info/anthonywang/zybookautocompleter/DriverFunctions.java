package info.anthonywang.zybookautocompleter;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

class DriverFunctions{
	private static JavascriptExecutor js;
	private static WebDriverWait wait;

	DriverFunctions(WebDriver driver){
		js = (JavascriptExecutor) driver;
		wait = new WebDriverWait(driver, 30);
	}

	public static void jsClick(WebElement element){
		js.executeScript("arguments[0].click()", element);
	}

	public static void waitUntilElementVisible(By locator){
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		}
		catch(TimeoutException e){
			e.printStackTrace();
		}
	}

	public static void waitUntilNestedElementVisible(WebElement parent, By child){
		try{
			wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(parent, child));
		}
		catch(TimeoutException e){
			e.printStackTrace();
		}
	}

	public static void waitUntilFinishedLoading(){
		try{
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".zb-progress-circular.orange")));
		}
		catch(TimeoutException e){
			e.printStackTrace();
		}
	}

	public static void waitUntilSectionLoaded(){
		try{
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".zybook-section.zb-card")));
		}
		catch(TimeoutException e){
			e.printStackTrace();
		}
	}

	public static void waitForRequest(WebDriver driver){
		while((Boolean) js.executeScript("return jQuery.active != 0")){
			driver.manage().timeouts().implicitlyWait(50, TimeUnit.MILLISECONDS);
		}
	}

	public static void dragDropJavaScript(WebElement dragElement, WebElement dropElement){
		final String JS_BUILD_CSS_SELECTOR =
			"for(var e=arguments[0],n=[],i=function(e,n){if(!e||!n)return 0;f" +
			"or(var i=0,a=e.length;a>i;i++)if(-1==n.indexOf(e[i]))return 0;re" +
			"turn 1};e&&1==e.nodeType&&'HTML'!=e.nodeName;e=e.parentNode){if(" +
			"e.id){n.unshift('#'+e.id);break}for(var a=1,r=1,o=e.localName,l=" +
			"e.className&&e.className.trim().split(/[\\s,]+/g),t=e.previousSi" +
			"bling;t;t=t.previousSibling)10!=t.nodeType&&t.nodeName==e.nodeNa" +
			"me&&(i(l,t.className)&&(l=null),r=0,++a);for(var t=e.nextSibling" +
			";t;t=t.nextSibling)t.nodeName==e.nodeName&&(i(l,t.className)&&(l" +
			"=null),r=0);n.unshift(r?o:o+(l?'.'+l.join('.'):':nth-child('+a+'" +
			")'))}return n.join(' > ');";
		String startDrag = (String) js.executeScript(JS_BUILD_CSS_SELECTOR, dragElement);
		String endDrag = (String) js.executeScript(JS_BUILD_CSS_SELECTOR, dropElement);
		Scanner scan_js = new Scanner(DriverFunctions.class.getResourceAsStream("/dndsim.js"), "UTF-8");
		String drag_and_drop_js = scan_js.useDelimiter("\\A").next();
		scan_js.close();
		js.executeScript(drag_and_drop_js + "DndSimulator.simulate(arguments[0], arguments[1]);", startDrag, endDrag);
	}
}