# zyBooks Activity Completer
This is a Python script that automatically completes participation activities on the zyBooks platform. I no longer maintain this project and don't intend to resolve any issues that arise with the script as the zybooks platform updates. However, I will accept any pull requests that implement bug fixes. Alternatively, feel free to fork this project.

## Requirements
The Firefox web browser is required to use this script, which can be downloaded at [Mozilla's official site](https://www.mozilla.org/en-US/firefox/new/). [Python 3](https://www.python.org/downloads/) must be installed in order to run this script.

## Installation
Download the `complete.py` file to the directory of choice. The version of [Mozilla's Geckodriver](https://github.com/mozilla/geckodriver/releases) appropriate for your system must also be downloaded and placed into the same directory as the script. Install necessary packages by running `pip3 install -r requirements.txt` from the installation directory.

## Usage
The script can be started by navigating to the location it has been unzipped to and running the command:
```
py -3 complete.py
```
After which the user will be prompted for their username and password.
On successful login, the user can then enter which chapter and section(s) they would like participation activities to be completed for.

Alternatively, lines 49 and 50 of the script can be edited to automatically use a specified email and course.
The script will then automatically use those values without prompting or input.

To exit the script before it begins completing activities, enter quit at any prompt.
Upon exiting, the script may throw an exception. This bug has no effect on usage and no detrimental effects as far I can tell, I just haven't gotten around to fixing it.
