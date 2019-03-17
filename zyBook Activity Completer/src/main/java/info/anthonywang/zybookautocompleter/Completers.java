package info.anthonywang.zybookautocompleter;

import com.google.common.collect.Iterables;
import com.google.gson.JsonParser;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarContent;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarPostDataParam;

class Completers{
	public static void completeAnimation(WebElement animation){
		animation.findElement(By.cssSelector("div.speed-control")).click();
		animation.findElement(By.cssSelector("button.start-button.start-graphic")).click();
		while(Iterables.getLast(animation.findElements(By.cssSelector("button.step"))).getAttribute("class").contains("highlight") == false){
			if(animation.findElements(By.cssSelector("div.pause-button")).size() != 0){
				continue;
			}
			animation.findElement(By.cssSelector("div.play-button")).click();
		}
		System.out.println("Completed animation");
	}

	public static void completeMultipleChoice(WebElement multiple_choice_set){
		List<WebElement> questions = multiple_choice_set.findElements(By.cssSelector("div.multiple-choice-question"));
		for(int i = 0; i < questions.size(); i++){
			List<WebElement> choices = questions.get(i).findElements(By.cssSelector("div.zb-radio-button"));
			for(int j = 0; j < choices.size(); j++){
				choices.get(j).findElement(By.tagName("label")).click();
				if(questions.get(i).findElements(By.cssSelector("div.correct")).size() != 0){
					break;
				}
			}
		}
		System.out.println("Completed multiple choice");
	}

	public static void completeShortAnswer(WebElement short_answer_set){
		List<WebElement> questions = short_answer_set.findElements(By.cssSelector("div.short-answer-question"));
		for(int i = 0; i < questions.size(); i++){
			WebElement show_answer = questions.get(i).findElement(By.cssSelector("button.show-answer-button"));
			show_answer.click();
			show_answer.click();
			String answer = questions.get(i).findElement(By.cssSelector("span.forfeit-answer")).getText();
			questions.get(i).findElement(By.cssSelector("textarea.zb-text-area")).sendKeys(answer);
			questions.get(i).findElement(By.cssSelector("button.check-button")).click();
		}
		System.out.println("Completed short answer");
	}

	public static void completeAnswerDetection(WebElement detect_answer_set){
		List<WebElement> questions = detect_answer_set.findElements(By.cssSelector("div.detect-answer-question"));
		for(int i = 0; i < questions.size(); i++){
			List<WebElement> answers = questions.get(i).findElements(By.cssSelector("button.unclicked.zb-button"));
			for(int j = 0; j < answers.size(); j++){
				answers.get(j).click();
				if(questions.get(i).findElements(By.cssSelector("div.correct")).size() != 0){
					break;
				}
			}
		}
		System.out.println("Completed answer detection");
	}

	public static void completeMatching(JavascriptExecutor js, WebDriverWait wait, WebElement matching_set){
		WebElement bank = matching_set.findElement(By.cssSelector("ul.term-bank"));
		List<WebElement> rows = matching_set.findElements(By.cssSelector("div.definition-row"));
		for(int i = 0; i < rows.size(); i++){
			for(int j = 0; j < rows.size() - i; j++){
				WebElement term = bank.findElement(By.cssSelector("div.js-draggableObject"));
				WebElement bucket = rows.get(i).findElement(By.cssSelector("div.draggable-object-target"));
				DriverFunctions.dragDropJavaScript(js, term, bucket);
				DriverFunctions.waitUntilNestedElementVisible(wait, rows.get(i), By.cssSelector("span.message"), "--Timed out watching for message response to matching row--");
				if(rows.get(i).findElements(By.cssSelector("div.correct")).size() != 0){
					break;
				}
			}
		}
		System.out.println("Completed matching");
	}

	public static void completeProgression(WebDriver driver, JavascriptExecutor js, BrowserMobProxy proxy, WebElement progression_challenge){
		int steps = progression_challenge.findElement(By.cssSelector("div.zyante-progression-status-bar")).findElements(By.cssSelector("div.filled")).size();
		if(progression_challenge.findElements(By.cssSelector("div.codeOutput")).size() != 0){
			for(int progress = 0; progress < steps; progress++){
				proxy.newHar();
				if(progress == 0){
					progression_challenge.findElement(By.cssSelector("button.zyante-progression-start-button")).click();
				}
				else{
					progression_challenge.findElement(By.cssSelector("button.zyante-progression-next-button")).click();
				}
				DriverFunctions.waitForRequest(driver, js, "--Timed out while fetching code output--");
				Har har = proxy.getHar();
				List<HarEntry> entries = har.getLog().getEntries();
				String session_id = "";
				String language = "";
				String answer = "";
				for(int i = 0; i < entries.size(); i++){
					if(!entries.get(i).getRequest().getMethod().equals("POST")){
						continue;
					}
					List<HarPostDataParam> post_params = entries.get(i).getRequest().getPostData().getParams();
					for(int j = 0; j < post_params.size(); j++){
						if(post_params.get(j).getName().equals("code")
						&& post_params.get(j + 1).getName().equals("language")){
							language = post_params.get(j + 1).getValue();
							HarContent content = entries.get(i).getResponse().getContent();
							if(content.getMimeType().equals("application/json")){
								session_id = new JsonParser().parse(content.getText()).getAsJsonObject().get("session_id").getAsString();
							}
						}
					}
				}
					for(int i = 0; i < entries.size(); i++){
					if(entries.get(i).getRequest().getQueryString().size() < 2){
						continue;
					}
					if(entries.get(i).getRequest().getQueryString().get(0).getName().equals("session_id")
					&& entries.get(i).getRequest().getQueryString().get(0).getValue().equals(session_id)
					&& entries.get(i).getRequest().getQueryString().get(1).getName().equals("language")
					&& entries.get(i).getRequest().getQueryString().get(1).getValue().equals(language)){
						answer = new JsonParser().parse(entries.get(i).getResponse().getContent().getText()).getAsJsonObject().get("result").getAsString();
					}
				}
				progression_challenge.findElement(By.cssSelector("textarea.console")).sendKeys(answer);
				progression_challenge.findElement(By.cssSelector("button.zyante-progression-check-button")).click();
			}
		}
	}
}