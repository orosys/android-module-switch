#!/usr/bin/python

import sys
import re
import os
import time
import json
import subprocess
from datetime import datetime
from collections import OrderedDict

# behavior    
# install       
# uninstall     
# launch        
# config        {module enable}_{group id}_{version}_{position permission}_{user info agree}
# watch         master, slave
# watch_all

packages = []
installedPackageCount = 0
installedPackages = []
packagesWatchDog = {}
packagesConfig = {}
lineCount = 0
totalLine = 0
devicieOSVersion = 0

def main():
    if len(sys.argv) == 1:
        print "Please add a input FILE..."
        return

    print "Open file"
    # Line Total Count
    file = open(sys.argv[1], "r")
    line = file.readline()
    global totalLine
    totalLine = 0
    while line:
        totalLine = totalLine + 1
        line = file.readline()
    file.close()

    print "Total line count " + str(totalLine)

    # Start Test
    file = open(sys.argv[1], "r")
    line = file.readline()
    global lineCount
    lineCount = 0
    while line:
        lineCount = lineCount + 1

        line = line.rstrip()
        lines = line.split(",")
        if len(packages) == 0 or line.startswith('behavior'):
            setPackages(lines)
            line = file.readline()
            continue
        processBehavior(lines)
        line = file.readline()

    print "Close file"
    file.close()
    updateInstalledPackage()
    printPackagesInfo(installedPackages)
    print "Process end"

def processBehavior(lines):
    if lines == None or len(lines) == 0:
        print "Line is empty!"
        return
    behavior = lines[0]
    do = {}
    answers = {}

    startIndex = len(packages) + 1
    index = 0
    for string in lines[startIndex:startIndex + len(packages)]:
        answers[packages[index]] = string
        index = index + 1

    print "Start Test Case " + str(lineCount) + "/" + str(totalLine) + "(" + str(int((float(lineCount) / float(totalLine)) * 100)) + "%), behavior : " + behavior
    if "watch" in behavior:
        doProcess(behavior, "None", "None")
    else:
        startIndex = 1
        index = 0
        for string in lines[startIndex:startIndex + len(packages)]:
            do[packages[index]] = string
            index = index + 1
    
    do = OrderedDict(sorted(do.items(), key=lambda t: t[1]))

    for key in do.keys():
        if do[key] == None or len(do[key]) == 0 or "-" in do[key]:
            continue
        doProcess(behavior, key, do[key])

    checkAnswers(behavior, answers)

def checkAnswers(behavior, answers):
    isSuccess = True
    for package in answers.keys():
        if package == None or len(package) == 0:
            continue
        if answers[package] == None or len(answers[package]) == 0:
            continue
        info = getPackageInfo(package)
        if info == None or len(info) == 0:
            isSuccess = False
            continue

        status = info.split("_")[5]
        if "1" in status:
            status = "master"
        elif "2" in status:
            status = "slave"
        elif "3" in status:
            status = "none"
        else:
            status = ""

        if status in answers[package]:
            tmp = 0
        else:
            isSuccess = False

    if not isSuccess:
        print "expect answers is " + str(answers)
        print "current answers is "
        printPackagesInfo(installedPackages)

        assert (False), "    Test falure! line " + str(lineCount) + ", behavior : " + behavior
    else:
        print "    Test success! " + str(lineCount) + ", behavior : " + behavior
        print str(int((float(lineCount) / float(totalLine)) * 100)) + "% Complete!"
        print " "

def doProcess(behavior, package, key):
    print "    doProcess " + behavior + " " + package + " " + key

    if "uninstall" in behavior:
        uninstallApp(package)

    elif "install" in behavior:
        installApp(package)

    elif "launch" in behavior:
        launchApp(package)

    elif "config" in behavior:
        configs = key.split("_")
        setConfig(package, configs[0], configs[1], configs[2], configs[3], configs[4])

    elif "watch_all" in behavior:
        startWatchDogAll()
        time.sleep(5)

    elif "watch" in behavior:
        startWatchDogArray(getMaster())
        startWatchDogArray(getSlave())
        time.sleep(5)
    time.sleep(5)

def setPackages(lines):
    lineLen = 0
    for l in lines:
        if l == None or len(l) == 0:
            continue
        lineLen = lineLen + 1

    if lines == None or lineLen == 0:
        print "CSV head is not exist!"
        return
    global packages
    index = (lineLen - 1) / 2
    for string in lines[1:index + 1]:
        packages.append(string)
        print string
    updateInstalledPackage()
    printPackagesInfo(installedPackages)

def isEnable(package):
    if packagesConfig[package] == None:
        return False

    config = packagesConfig[package].split("_")
    isEnable = True
    if not ("1" in config[0]) :
        isEnable = False
    if not ("1" in config[3]) :
        isEnable = False
    if not ("1" in config[4]) :
        isEnable = False
    return isEnable

def getMaster():
    global installedPackages
    updatePackagesInfo(installedPackages)
    res = []
    for package in installedPackages:
        config = packagesConfig[package].split("_")
        if "1" in config[5] :
            res.append(package)
    return res

def getSlave():
    global installedPackages
    updatePackagesInfo(installedPackages)
    res = []
    for package in installedPackages:
        config = packagesConfig[package].split("_")
        if "2" in config[5] :
            res.append(package)
    return res

def startWatchDogArray(packages):
    for package in packages:
        startWatchDog(package)

def startWatchDogAll():
    for package in installedPackages:
        startWatchDog(package)

def startWatchDog(package):
    if package == None:
        return
    if packagesWatchDog.get(package, None) == None:
        return
    watchDogSetting = packagesWatchDog[package].split(",")
    
    print "    watch dog " + package + " " + packagesWatchDog[package]

    proc = subprocess.Popen('adb logcat -c', shell=True, stdout=subprocess.PIPE)
    proc = subprocess.Popen('adb shell am broadcast -n ' + package + '/com.orosys.sdk.proximity.sw.SwitchReceiver -a com.orosys.sdk.proximity.sw.action.WATCH_DOG --es "name.PACKAGE" "' + watchDogSetting[0] + '" --es "name.REQUEST_VALUE" "' + watchDogSetting[2] + '" --el "name.PERIOD" ' + watchDogSetting[1], shell=True, stdout=subprocess.PIPE)
    proc = subprocess.Popen('adb logcat time', shell=True, stdout=subprocess.PIPE)
    
    while True:
        line = proc.stdout.readline()
        if "com.orosys.sdk.proximity.sw.action.REQUEST_SDK_ENABLE" in line:
            proc.kill()
            time.sleep(1)
            break
        elif "com.orosys.sdk.proximity.sw.action.COMPLETE_WATCH_DOG" in line:
            proc.kill()
            time.sleep(5)
            break

def setConfig(package, enable, groupId, version, position, agree):
    isPackage = False
    for packageName in installedPackages:
        if package in packageName:
            isPackage = True
    if not isPackage:
        print "setConfig package is not exist!"
        return

    if getDeviceOSVersion() < 6:
        print "    OS Version is not M "
        position = 1
    else:
        if (int(position) > 0):
            proc = subprocess.Popen('adb shell pm grant ' + package + ' android.permission.ACCESS_FINE_LOCATION', shell=True, stdout=subprocess.PIPE)
            while True:
                line = proc.stdout.readline()
                break
        else :
            proc = subprocess.Popen('adb shell pm revoke ' + package + ' android.permission.ACCESS_FINE_LOCATION', shell=True, stdout=subprocess.PIPE)
            while True:
                line = proc.stdout.readline()
                break

    fileName = getPackageInfo(package)
    if fileName == None:
        fileName = "_0"
    newName = str(enable) + "_" + str(groupId) + "_" + str(version) + "_" + str(position) + "_" + str(agree) + fileName[9:]

    proc = subprocess.Popen('adb shell mv /sdcard/android/data/' + package + '/files/' + fileName + ' /sdcard/android/data/' + package + '/files/' + newName, shell=True, stdout=subprocess.PIPE)
    while True:
        line = proc.stdout.readline()
        break

    proc = subprocess.Popen('adb shell am broadcast -n ' + package + '/com.orosys.sdk.proximity.sw.SwitchReceiver -a com.orosys.sdk.proximity.sw.action.UPDATE_INFO', shell=True, stdout=subprocess.PIPE)
    check("com.orosys.sdk.proximity.sw.action.UPDATE_INFO")

def launchApp(package):
    updateInstalledPackage()
    print "    Launch App " + package
    global installedPackages
    isInstalled = False
    for installedPackage in installedPackages:
        if package in installedPackage:
            isInstalled = True
    if isInstalled:
        proc = subprocess.Popen('adb shell am broadcast -n ' + package + '/com.orosys.sdk.proximity.sw.SwitchReceiver -a com.orosys.sdk.proximity.sw.action.LAUNCH_APP', shell=True, stdout=subprocess.PIPE)
    else:
        print "    " + package + " is not installed"

    if isInstalled:
        packageCount = 0
        currentGroupId = packagesConfig[package].split("_")[1]
        for key in packagesConfig.keys():
            if currentGroupId in packagesConfig[key].split("_")[1]:
                packageCount = packageCount + 1
            
        collectWatckDog(packageCount)
    time.sleep(6)

def installApp(package):
    updateInstalledPackage()

    for p in installedPackages:
        if package in p:
            print "    Already installed package " + package
            return

    file = package + ".apk"
    print "    Install App " + file
    proc = subprocess.Popen('adb install -r ' + file, shell=True, stdout=subprocess.PIPE)
    stTime = time.time()
    while True:
        line = proc.stdout.readline()
        if time.time() - stTime > 3:
            break
        if "Success" in line:
            print "    Installed App " + file
            break

    proc = subprocess.Popen('adb shell am start -a android.intent.action.MAIN -n ' + package + '/.MainActivity', shell=True, stdout=subprocess.PIPE)        
    while True:
        line = proc.stdout.readline()
        break
    check("com.orosys.sdk.proximity.sw.action.LAUNCH_APP")
    time.sleep(1)
    updateInstalledPackage()

def uninstallApp(package):
    print "    Uninstall App " + package
    proc = subprocess.Popen('adb uninstall ' + package, shell=True, stdout=subprocess.PIPE)
    stTime = time.time()
    while True:
        line = proc.stdout.readline()
        if time.time() - stTime > 3:
            break
        if "Success" in line:
            break
    updateInstalledPackage()

def updatePackagesInfo(installedPackages):
    global packagesConfig
    packagesConfig = {}
    for package in installedPackages:
        packagesConfig[package] = getPackageInfo(package)

def printPackagesInfo(installedPackages):
    updatePackagesInfo(installedPackages)
    for package in installedPackages:
        print "package info " + str(package) + ", " + str(getPackageInfo(package))

def check(action):
    proc = subprocess.Popen('adb logcat -c', shell=True, stdout=subprocess.PIPE)
    proc = subprocess.Popen('adb logcat time', shell=True, stdout=subprocess.PIPE)
    
    count = 0
    global packagesWatchDog
    while True:
        line = proc.stdout.readline()
        if action in line:
            proc.kill()
            break
    time.sleep(1)

def collectWatckDog(installedPackageCount):
    proc = subprocess.Popen('adb logcat -c', shell=True, stdout=subprocess.PIPE)
    proc = subprocess.Popen('adb logcat time', shell=True, stdout=subprocess.PIPE)
    
    count = 0
    global packagesWatchDog
    while True:
        line = proc.stdout.readline()
        if " setMonitorSDK " in line:
            count = count + 1
            print "    collectWatckDog " + str(count) + "/" + str(installedPackageCount) + " " + line.rstrip().split("setMonitorSDK ")[1]
            info = line.rstrip().split("setMonitorSDK ")[1].split("->")
            packagesWatchDog[info[0]] = info[1]
        if count == installedPackageCount:
            proc.kill()
            break

def updateInstalledPackage():
    global installedPackages
    command = ""
    for package in packages:
        if len(command) == 0:
            command = command + package
        else:
            command = command + "|" + package
    proc = subprocess.Popen('adb shell pm list package -f | grep -E "' + command + '"', shell=True, stdout=subprocess.PIPE)
    count = 0
    installedPackages = []
    for line in proc.stdout:
        installedPackages.append(line.rstrip().split("apk=")[1])
        count = count + 1
    global installedPackageCount
    installedPackageCount = len(installedPackages)
    updatePackagesInfo(installedPackages)

def getDeviceOSVersion():
    global devicieOSVersion
    if devicieOSVersion == 0:
        proc = subprocess.Popen('adb shell getprop ro.build.version.release', shell=True, stdout=subprocess.PIPE)
        version = "0"
        for line in proc.stdout:
            version = line.rstrip().split(".")[0]
            devicieOSVersion = int(version)
    return devicieOSVersion

def getPackageInfo(package):
    proc = subprocess.Popen('adb shell ls -lR /sdcard/android/data/' + package + '/files | grep ".prox"', shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    for line in proc.stdout:
        try:
            lines = line.rstrip().split(" ")
            result = lines[len(lines) - 1]
            return result
        except Exception as e:
            raise e
            return ""

if __name__ == "__main__":
    main()
