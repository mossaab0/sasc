# Search in Secrets

## Features
- This package supports two retrieval modes:

  - The first, **search** uses the IMAP search protocol. That is, the terms are sent to the IMAP server, and we return whatever that server returns to us.
  - The second, **filter** iterates over *ALL* messages in the email, extracts text (including from attachments when possible, using Tika), and checks whether any term is contained in the extracted text. By default, both of the terms and the extracted text go through Lucene's StandardAnalyzer tokenizer with an empty stop word. Optionally, Porter stemming can be applied on both (with the *--stem* option).

- The text of the retrieved emails (including content extracted from attachments, when possible) is by default printed to STDIN, and can be saved to an empty (or nonexistent) folder, one file per email.

## System Requirements
- To **compile** the app with all of its dependencies, you need to install **Java 8 JDK** and **Apache Maven**. [Here](https://www.twilio.com/blog/2017/01/install-java-8-apache-maven-google-web-toolkit-windows-10.html) is a tutorial that shows how to do so on Windows 10 (you do **not** need to do the third step of installing Google Web Toolkit).
- To **run** the app, you only need **Java 8 JRE**. That is you can copy the compiled app from a machine that have the compilation requirements to another machine that has the JRE without JDK nor Maven.

## Configure Gmail Account
By default, gmail won't allow the app to access your email. Here are some options to address this security restriction:

 - Option 1:
 
  1. Go to https://myaccount.google.com/security
  2. Enable **2-Step Verification**
  3. Click on **App Password**
  4. Select **Mail** and **Windows Computer**
  5. Click on **Generate**
  6. You will see a 16-character password. Use this password instead of your regular one.

 - Option 2:
 
  1. Go to https://myaccount.google.com/security?pli=1#connectedapps
  2. Enable **Allow less secure apps**

## Installation
    git clone https://github.com/mossaab0/search-in-secrets.git
    cd search-in-secrets
    ./install.sh

## Usage
    ./run.sh [options]
      Options:
        -?, -h, --help
          Display this help.
        --host
          IMAP host address.
          Default: imap.gmail.com
      * --mode
          Retrieval mode. Supported values: search, filter.
        --output
          Output folder (absolute path).
        --password
          Password.
        --port
          IMAP host part number.
          Default: 993
        --stem
          Use stemming. Works only with --mode filter.
          Default: false
      * --terms
          Search terms, separated by comma.
        --username
          Username.
        --verbosity
          Verbosity level. Silent = 0, verbose = 1, print content to stdout = 2.
          Default: 2
