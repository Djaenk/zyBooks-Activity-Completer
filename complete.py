from selenium import webdriver
import getpass
import time

def login(driver):
	driver.get("https://learn.zybooks.com/signin")
	email_input = driver.find_element_by_xpath("//*[@id=\"ember912\"]")
	password_input = driver.find_element_by_xpath("//*[@id=\"ember918\"]")
	signin_button = driver.find_element_by_xpath("//*[@id=\"ember920\"]")
	
	while(True):
		email = input("Please enter your zyBooks email: ")
		email_input.send_keys(email)
		password = getpass.getpass("Enter your zyBooks password: ")
		password_input.send_keys(password)
		signin_button.click()
		time.sleep(3)
		if(driver.find_elements_by_xpath('//button[@disabled=\"\"]') or driver.find_elements_by_xpath('//div[contains(text(), "Invalid email or password")]')):
			print("--Invalid email or password--\n")
			email_input.clear()
			password_input.clear()
		else:
			print("\nLogin Successful\n")
			break

def selectzyBook(driver):
	while(True):
		try:
			course_identifier = input("Enter your course ID or the name of your course: ")
			course_identifier = course_identifier.replace(" ", "")
			zybook_selection = driver.find_element_by_xpath("//a[contains(@href, \"" + course_identifier + "\")]")
			zybook_selection.click()
			break
		except:
			print("Invalid course")
		else:
			print("Course selected\n")

def chapterSelection(driver):
	while(True):
		chapter = input("Enter the chapter number you want completed: ")
		try:
			chapter_selection = driver.find_element_by_xpath("//*[@class=\"chapter-title\" and contains(text(), \"" + chapter + ".\")]")
			chapter_selection.click()
			return chapter
		except:
			print("Invalid chapter")
	
def sectionSelection(driver, chapter):
	while(True):
		section = input ("\nEnter the section number you want completed, enter 'all' if you would like the entire chapter completed: ")
		if(section != "all"):
			try:
				section_selection = driver.find_element_by_xpath("//span[@class=\"section-title\" and contains(text(), \"" + chapter + "." + section + "\")]")
				section_selection.click()
				print("Completing Chapter " + chapter + " Section " + section + "...")	
				break
			except:
				pass			

driver = webdriver.Firefox()
login(driver)
selectzyBook(driver)
chapter = chapterSelection(driver)
sectionSelection(driver, chapter)
#driver.quit()