# Search in Secrets

## Features
- This package supports two retrieval modes:

  - The first, **search** uses the IMAP search protocol. That is, the terms are sent to the IMAP server, and we return whatever that server returns to us.
  - The second, **filter** iterates over *ALL* messages in the email, extracts text (including from attachments when possible, using Tika), and checks whether any term is contained in the extracted text. By default, both of the terms and the extracted text go through Lucene's StandardAnalyzer tokenizer with an empty stop word. Optionally, Porter stemming can be applied on both (with the *--stem* option).

## System Requirements
- Java 8 JDK
- Maven

## Configure Gmail Account
You might need to go to https://myaccount.google.com/security?pli=1#connectedapps and do some magic [TODO] before Google allows the application to access your emails.

## Installation
    git clone https://github.com/mossaab0/search-in-secrets.git
    cd search-in-secrets
    ./install.sh

## Execution
This package currently two retrieval modes. The first, **search**, uses the IMAP search protocol. That is, the terms are sent to the IMAP server, and we 
