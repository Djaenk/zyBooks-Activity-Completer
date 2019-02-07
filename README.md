# zyBooks Activity Completer
This is a Python script which automatically completes participation activities on the zyBooks platform.

## Installation
A version of [Python 3](https://www.python.org/downloads/) must be installed in order to run this script.
In addition, the Python Selenium bindings must also be installed, which can be done by running the following command:
```
pip3 install selenium
```

## Usage
With Python and the appropriate dependencies installed, the script can be run with the simple command:
```
py -3 complete.py
```
After which the user will be prompted for their username and password.
On successful login, the user can then enter which chapter and section(s) they would like participation activities to be completed for.
To exit the script before it begins completing activities, enter quit at any prompt.
