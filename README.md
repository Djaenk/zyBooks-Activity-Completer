# zyBooks Activity Completer
This is a Python script which automatically completes participation activities on the zyBooks platform.

## Requirements
A version of [Python 3](https://www.python.org/downloads/) must be installed in order to run this script.
In addition, the Python Selenium bindings must also be installed, which can be done by running the following command with Python installed:
```
pip3 install selenium
```

## Installation
Download and unzip the latest release into a folder. The complete.py script and geckodriver.exe exectable must be in the same folder.

## Usage
The script can be started by navigating to the location it has been unzipped to and running the command:
```
py -3 complete.py
```
After which the user will be prompted for their username and password.
On successful login, the user can then enter which chapter and section(s) they would like participation activities to be completed for.

To exit the script before it begins completing activities, enter quit at any prompt.
