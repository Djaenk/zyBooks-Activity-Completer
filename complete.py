from selenium import webdriver
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.common.action_chains import ActionChains
import os
import getpass
import traceback

drag_and_drop_js = '''
function createEvent(typeOfEvent) {
    var event = document.createEvent("CustomEvent");
    event.initCustomEvent(typeOfEvent, true, true, null);
    event.dataTransfer = {
        data: {},
        setData: function(key, value) {
            this.data[key] = value;
        },
        getData: function(key) {
            return this.data[key];
        }
    };
    return event;
}

function dispatchEvent(element, event, transferData) {
    if (transferData !== undefined) {
        event.dataTransfer = transferData;
    }
    if (element.dispatchEvent) {
        element.dispatchEvent(event);
    } else if (element.fireEvent) {
        element.fireEvent("on" + event.type, event);
    }
}

function simulateHTML5DragAndDrop(element, destination) {
    var dragStartEvent = createEvent('dragstart');
    dispatchEvent(element, dragStartEvent);
    var dropEvent = createEvent('drop');
    dispatchEvent(destination, dropEvent, dragStartEvent.dataTransfer);
    var dragEndEvent = createEvent('dragend');
    dispatchEvent(element, dragEndEvent, dropEvent.dataTransfer);
}
var source = arguments[0];
var destination = arguments[1];
simulateHTML5DragAndDrop(source, destination);
'''

def login(driver):
	driver.get("https://learn.zybooks.com/signin")
	while(True):
		email_input = driver.find_element_by_xpath("//input[@type='email']")
		password_input = driver.find_element_by_xpath("//input[@type='password']")
		signin_button = driver.find_element_by_class_name("signin-button")
		email = input("Please enter your zyBooks email: ")
		if(email == "quit"):
			print("--Exiting--")
			driver.quit()
			os._exit(0)
		email_input.send_keys(email)
		password = getpass.getpass("Enter your zyBooks password: ")
		if(password == "quit"):
			print("--Exiting--")
			driver.quit()
			os._exit(0)
		password_input.send_keys(password)
		signin_button.click()
		try:
			driver.implicitly_wait(1)
			WebDriverWait(driver, 30).until(expected_conditions.invisibility_of_element((By.CSS_SELECTOR, ".zb-progress-circular.orange.med.message-present.ember-view")))
			driver.implicitly_wait(0)
		except:
			driver.quit()
			print("Timed out while authenticating login, aborting...")
			os._exit(0)
		if(driver.find_elements_by_xpath("//button[@disabled='']") or driver.find_elements_by_xpath("//div[contains(text(), 'Invalid email or password')]")):
			print("--Invalid email or password--\n")
			email_input.clear()
			password_input.clear()
		else:
			print("\nLogin Successful\n")
			break

def selectzyBook(driver):
	while(True):
		course_identifier = input("Enter your course ID or the name of your course: ")
		if(course_identifier == "quit"):
			print("--Exiting--")
			driver.quit()
			os._exit(0)
		try:
			course_identifier = course_identifier.replace(" ", "")
			zybook_selection = driver.find_element_by_xpath("//a[contains(@href, '" + course_identifier + "')]")
			zybook_selection.click()
			break
		except:
			print("--Invalid course--\n")
	print("zyBook Selected\n")

def chapterSelection(driver):
	while(True):
		chapter = input("Enter the chapter number you want completed: ")
		if(chapter == "quit"):
			print("--Exiting--")
			driver.quit()
			os._exit(0)
		try:
			chapter_selection = driver.find_elements_by_xpath("//*[contains(@class, 'table-of-contents-list')]/*[contains(@class, 'chapter-item')]")[int(chapter) - 1]
			chapter_selection.click()
			print("Chapter Selected\n")
			return chapter
		except:
			print("--Invalid chapter--\n")
	
def sectionSelection(driver, chapter):
	while(True):
		chapter_selection = driver.find_elements_by_xpath("//*[contains(@class, 'table-of-contents-list')]/*[contains(@class, 'chapter-item')]")[int(chapter) - 1]
		section_selection = input("Enter the section number you want completed. Enter \"all\" if you would like the entire chapter completed: ")
		if(section_selection == "quit"):
			print("--Exiting--")
			driver.quit()
			os._exit(0)
		if(section_selection.isnumeric()):
			section_button = chapter_selection.find_elements_by_xpath("//span[@class='section-title']")[int(section_selection) - 1]
			section_button.click()
			print("\nStarting chapter " + chapter +" section " + section_selection + "...")
			try:
				WebDriverWait(driver, 10).until(expected_conditions.element_to_be_clickable((By.CSS_SELECTOR, ".zybook-section.zb-card.ember-view")))
			except:
				print("Timed out while loading chapter " + chapter + " section " + section_selection + " content, aborting...")
				driver.quit()
				os._exit(0)
			completeParticipationActivities(driver)
			driver.find_element_by_xpath("/html/body/div[3]/header/div[1]/div/ul/a[2]/li").click()
			break
		elif(section_selection == "all"):
			sections = chapter_selection.find_elements_by_xpath("//span[@class='section-title']")
			sections[0].click()
			for section_index in range(len(sections)):
				print("\nStarting chapter " + chapter +" section " + str(section_index + 1) + "...")
				try:
					WebDriverWait(driver, 10).until(expected_conditions.element_to_be_clickable((By.CSS_SELECTOR, ".zybook-section.zb-card.ember-view")))
				except:
					print("Timed out while loading chapter " + chapter + " section " + section_index + " content, aborting...")
					driver.quit()
					os._exit(0)
				completeParticipationActivities(driver)
				if(section_index != (len(sections) - 1)):
					driver.find_element_by_xpath("//span[@class='nav-text next ']").click()
			driver.find_element_by_xpath("/html/body/div[3]/header/div[1]/div/ul/a[2]/li").click()
			break
		else:
			print("Please enter a valid section number.")

def completeParticipationActivities(driver):
	playAnimations(driver)
	completeCustomInteractions(driver)
	completeMultipleChoice(driver)
	completeShortAnswer(driver)
	completeMatching(driver)
	completeSelectionProblems(driver)
		
def playAnimations(driver):
	animation_players = driver.find_elements_by_xpath("/html/body/div[3]/div/section/div/article/div/div[@class='interactive-activity-container animation-player-content-resource participation large ember-view']")
	animation_players += driver.find_elements_by_xpath("/html/body/div[3]/div/section/div/article/div/div[@class='interactive-activity-container animation-player-content-resource participation medium ember-view']")
	animation_players += driver.find_elements_by_xpath("/html/body/div[3]/div/section/div/article/div/div[@class='interactive-activity-container animation-player-content-resource participation small ember-view']")
	for animation in animation_players:
		driver.find_element_by_xpath("//div[@class='section-header-row']").click()
		double_speed = animation.find_element_by_xpath(".//div[@class='speed-control ']")
		double_speed.click()
		start_button = animation.find_element_by_xpath(".//button[@class='start-button start-graphic zb-button primary raised ember-view']")
		start_button.click()
		while(True):
			if(animation.find_elements_by_xpath(".//div[@class='pause-button']")):
				continue
			try:
				play_button = animation.find_element_by_xpath(".//div[@class='play-button  bounce']")
				play_button.click()
			except:
				pass
			if(animation.find_elements_by_xpath(".//div[@class='play-button rotate-180 ']")):
				break
		print("Completed animation activity")

def completeCustomInteractions(driver):
	custom_activties = driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource participation large ember-view']")
	custom_activties += driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource participation medium ember-view']")
	custom_activties += driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource participation small ember-view']")
	for activity in custom_activties:
		driver.find_element_by_xpath("//div[@class='section-header-row']").click()
		buttons = activity.find_elements_by_xpath(".//button[@class='button']")
		for button in buttons:
			button.click()

def completeMultipleChoice(driver):
	multiple_choice_sets = driver.find_elements_by_xpath("//div[@class='interactive-activity-container multiple-choice-content-resource participation large ember-view']")
	multiple_choice_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container multiple-choice-content-resource participation medium ember-view']")
	multiple_choice_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container multiple-choice-content-resource participation small ember-view']")
	for question_set in multiple_choice_sets:
		driver.find_element_by_xpath("//div[@class='section-header-row']").click()
		questions = question_set.find_elements_by_xpath(".//div[@class='question-set-question multiple-choice-question ember-view']")
		for question in questions:
			choices = question.find_elements_by_xpath(".//label[@aria-hidden='true']")
			for choice in choices:
				choice.click()
				if(question.find_elements_by_xpath(".//div[@class='explanation has-explanation correct']")):
					break
		print("Completed multiple choice set")
			
def completeShortAnswer(driver):
	short_answer_sets = driver.find_elements_by_xpath("//div[@class='interactive-activity-container short-answer-content-resource participation large ember-view']")
	short_answer_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container short-answer-content-resource participation medium ember-view']")
	short_answer_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container short-answer-content-resource participation small ember-view']")
	for question_set in short_answer_sets:
		driver.find_element_by_xpath("//div[@class='section-header-row']").click()
		questions = question_set.find_elements_by_xpath(".//div[@class='question-set-question short-answer-question ember-view']")
		for question in questions:
			show_answer_button = question.find_element_by_xpath(".//button[@class='show-answer-button zb-button secondary ember-view']")
			show_answer_button.click()
			show_answer_button.click()
			answer = question.find_element_by_xpath(".//span[@class='forfeit-answer']").text
			text_area = question.find_element_by_xpath(".//textarea[@class='zb-text-area hide-scrollbar ember-text-area ember-view']")
			text_area.send_keys(answer)
			check_button = question.find_element_by_xpath(".//button[@class='check-button zb-button primary raised ember-view']")
			check_button.click()
		print("Completed short answer set")

def completeMatching(driver):
	matching_sets = driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource participation large ember-view']")
	matching_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource participation medium ember-view']")
	matching_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource participation small ember-view']")
	for matching in matching_sets:
		matching.click()
		rows = matching.find_elements_by_class_name("definition-row")
		for row in rows:
			while not row.text.endswith("Correct"):
				choice = matching.find_element_by_class_name("draggable-object")
				bucket = row.find_element_by_class_name("term-bucket")
				driver.execute_script(drag_and_drop_js, choice, bucket)
		print("Completed matching set")

def completeSelectionProblems(driver):
	selection_problem_sets = driver.find_elements_by_xpath("//div[@class='interactive-activity-container detect-answer-content-resource participation large ember-view']")
	selection_problem_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container detect-answer-content-resource participation medium ember-view']")
	selection_problem_sets += driver.find_elements_by_xpath("//div[@class='interactive-activity-container detect-answer-content-resource participation small ember-view']")
	for question_set in selection_problem_sets:
		driver.find_element_by_xpath("//div[@class='section-header-row']").click()
		questions = question_set.find_elements_by_xpath(".//div[@class='question-set-question detect-answer-question ember-view']")
		for question in questions:
			choices = question.find_elements_by_xpath(".//div[@class='unclicked']")
			for choice in choices:
				choice.click()
				if(question.find_elements_by_xpath(".//div[@class='explanation has-explanation correct']")):
					break
		print("Completed selection problem set")

def completeProgressionChallenges(driver):
	progression_challenges = driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource challenge large ember-view']")
	progression_challenges += driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource challenge medium ember-view']")
	progression_challenges += driver.find_elements_by_xpath("//div[@class='interactive-activity-container custom-content-resource challenge small ember-view']")
	for progression in progression_challenges:
		progression_status = progression.find_elements_by_xpath(".//div[@class='zyante-progression-status-bar'']/div")
		for status in progression_status:
			if status.text == 1:
				start_button = progression.find_element_by_xpath(".//button[@class='zyante-progression-start-button button']")
				start_button.click()
			else:
				next_button = progression.find_element_by_xpath("class='zyante-progression-next-button button']")
				next_button.click()
	return

if(os.name == 'nt'):
	geckodriver_path = '.\\geckodriver.exe'
else:
	geckodriver_path = './geckodriver'
options = Options()
options.headless = False
#browser = input("Choose the web browser you have installed:\n") #todo: give options for different installed browsers
driver = webdriver.Firefox(executable_path = geckodriver_path, options = options)
print("\nTo exit the script, enter \"quit\" at any prompt.")
print("\nHeadless Firefox browswer initiated.\n")

try:
	login(driver)
	try:
		WebDriverWait(driver, 10).until(expected_conditions.element_to_be_clickable((By.CSS_SELECTOR, ".library-page")))
	except:
		print("Timed out while loading zyBooks library, aborting...")
		driver.quit()
		os._exit(0)
	selectzyBook(driver)
	try:
		WebDriverWait(driver, 10).until(expected_conditions.element_to_be_clickable((By.CSS_SELECTOR, ".table-of-contents.ember-view")))
	except:
		print("Timed out while loading zyBook table of contents, aborting...")
		driver.quit()
		os._exit(0)
	while(True):
		chapter = chapterSelection(driver)
		try:
			WebDriverWait(driver, 10).until(expected_conditions.element_to_be_clickable((By.CSS_SELECTOR, ".section-list")))
		except:
			print("Timed out while loading zyBook list of sections, aborting...")
			driver.quit()
			os._exit(0)
		sectionSelection(driver, chapter)
		print("Participation activities completed.\n")
except:
	with open("exception.log", "w") as log:
		traceback.print_exc(file=log)
		log.write("\n")
		log.write("#" * 80)
		log.write("\n")
		log.write(driver.page_source)

driver.quit()
print("Headless Firefox browser closed")
