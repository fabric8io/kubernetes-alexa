# Kubernetes Skill for Alexa

## Overview
This is a simple skill for Amazon Alexa that allows Alexa to interact with an existing Kubernetes cluster.
It's implemented as Amazon Lambda Function, which is configured via environment variables (e.g. kubernetes master url, oauth token etc).

## Features
Currently it supports the following intents:

- Get namespaces / projects
- Switch to namespace / project
- Get deployments
- Get (failing )pods
- Get services

## Building & Installation

You can build and install the function using something like:

    mvn clean package shade:shade com.github.seanroy:lambda-maven-plugin:deploy-lambda -DaccessKey=`pass aws/access_key` -DsecretKey=`pass aws/secret`
    
The above assumes that you are using [password-store](https://passwordstore.org) for managing credentials (which btw you should do rahter than keeping them lying around as env vars...).
If not, feel free to substitute the `pass` bits with your actual access key and secret.

If for any reason, the function is not automatically created, you can manually upload the generated zip.

## Configuring the Function

To specify which is the cluster, how to authenticate etc, you'll need to specify a couple of environment variables to the lambda console:

- KUBERNETES_MASTER
- KUBERNETES_AUTH_TOKEN
- ALEXA_SKILL_ID


### Todo

- Use word distance so that we accept words that might not be `equals()` but are close to being equals (e.g. more than 80%).


    
    