---
tags: [ToDo]
title: Accurev-client plugin
created: '2020-01-07T13:46:40.854Z'
modified: '2020-01-07T14:07:14.688Z'
---

# Accurev-client plugin

The accurev client is build with Gradle and done in Kotlin. 

Each Accurev command has its separate class in the command/ folder. These are implemented in the AccurevCLIAPI, and made available through the AccurevClient interface.

The commands are executed through the launchCommand found in AccurevCLIAPI, that takes the different arguments used at the respective command, and launch it through a Hudson Launcher.

As example, if you want to log in through the accurev client, you would do

```accurevClient.login().username(username).password(password).execute()```

If you only do

```accurevClient.login().username(username).password(password)```

The username and password will be appended as arguments to the command, but not executed.
