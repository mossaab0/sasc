# Search in Secrets

## Features
- This package supports two retrieval modes:

  - The first, **search** uses the IMAP search protocol. That is, the terms are sent to the IMAP server, and we return whatever that server returns to us.
  - The second, **filter** iterates over *ALL* messages in the email, extracts text (including from attachments when possible, using Tika), and checks whether any term is contained in the extracted text. By default, both of the terms and the extracted text go through Lucene's StandardAnalyzer tokenizer with an empty stop word. Optionally, Porter stemming can be applied on both (with the *--stem* option).

- The text of the retrieved emails (including content extracted from attachments, when possible) is by default printed to STDIN, and can be saved to an empty (or nonexistent) folder, one file per email.

## System Requirements
- Java 8 JDK
- Maven

## Configure Gmail Account
You might need to go to https://myaccount.google.com/security?pli=1#connectedapps and do some magic [TODO] before Google allows the application to access your emails.

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
