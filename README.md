# zyBooks Activity Completer
This is a Python script which automatically completes participation activities on the zyBooks platform. A Java GUI version was previously in development, but has been dropped as I no longer have access to a zyBook. If an issue is encountered, feel free to log it and also contact me about it, as I don't check this project often anymore.

## Requirements
The Firefox web broswer is required to use this script, which can be downloaded at [Mozilla's official site](https://www.mozilla.org/en-US/firefox/new/).
A version of [Python 3](https://www.python.org/downloads/) must be installed in order to run this script.
As of updating this readme [this](https://www.python.org/ftp/python/3.7.2/python-3.7.2-amd64-webinstall.exe) is the latest version of python.
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

## Future Work
- Allow completion of multiple chapters at a time
- Allow completion of multiple specific sections
- Support for more pre-installed browsers (e.g. Google Chrome, Safari, Microsoft Edge, Internet Explorer, Opera, Vivaldi)
