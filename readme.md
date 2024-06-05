# Cloud Computing Project - Labels and Translations

![Contributors](https://img.shields.io/github/contributors/Diogofmr/CloudProject-GoogleServices)
![GitHub repo size](https://img.shields.io/github/repo-size/Diogofmr/CloudProject-GoogleServices)
![GitHub top language](https://img.shields.io/github/languages/top/Diogofmr/CloudProject-GoogleServices)
![GitHub Release Date](https://img.shields.io/github/release-date/Diogofmr/CloudProject-GoogleServices)

<a name="readme-top"></a>

<h3 align="center">Cloud Computer Prject</h3>

  <p align="center">
    Getting Labels and Translations from a image using Google Services.
    <br />
    <a href="https://github.com/Diogofmr/CloudProject-GoogleServices"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    ·
    <a href="https://github.com/Diogofmr/CloudProject-GoogleServices/issues">Report Bug</a>
    ·
    <a href="https://github.com/Diogofmr/CloudProject-GoogleServices/issues">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## About The Project

The project is a little terminal appplication that allows the user to get labels and translations from a image.
This explores the use of various cloud services:

- Google Cloud Vision API
- Google Cloud Translation API
- Google Cloud Storage
- Google Cloud Functions
- Google Cloud Pub/Sub
- Google Cloud Firestore

Every instance of the gRPC and Label is alocated in VM instances of Google Cloud Platform.
It was also used for contract between the client and the server the gRPC protocol "protobuf".

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->

## Getting Started

Before you start you need to have a Google Cloud Platform account and a project created https://console.cloud.google.com/.
After that you need to enable the following APIs:

- Google Cloud Vision API
- Google Cloud Translation API
- Google Cloud Storage
- Google Cloud Functions
- Google Cloud Pub/Sub
- Google Cloud Firestore
- Google Cloud Compute Engine

After that you need to create a service account and download the key in json format.
Then you need to set the GOOGLE_APPLICATION_CREDENTIALS environment variable to the path of the json file.

### Prerequisites

We need to install the following software:

- Maven
  ```sh
  sudo apt install maven
  ```
- Java 11 or higher
  ```sh
  sudo apt install default-jdk
  ```

### Installation

1. Clone the repo

   ```sh
   git clone ...
   ```

2. Build and create a Client jar(root folder)

   ```sh
   mvn ./clientApp/package
   ```

3. Run the Cloud Function for IP lookup and Logging (in the ipLookup module)

   ```sh
   gcloud functions deploy funcIPLookup --project=cn2324-t1-g18 --region=europe-west1 --allow-unauthenticated --entry-point=ipLookup.Entrypoint --no-gen2 --runtime=java11 --trigger-http --source=target --service-account=backup-test-account@cn2324-t1-g18.iam.gserviceaccount.com --max-instances=3
   ```

   (in the loggingApp module)

   ```sh
   gcloud functions deploy logging-app --project=cn2324-t1-g18 --region=europe-west2  --entry-point=loggingApp.LoggingApp --no-gen2 --runtime=java11 --trigger-topic image-requests --source=target/deployment --service-account=cn-v2324-firestore-18@cn2324-t1-g18.iam.gserviceaccount.com
   ```

4. Run the client (in the clientApp module)
   ```sh
   java -jar target/clientApp-1.0-jar-with-dependencies.jar
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->

## Contact

:inbox_tray: Diogo Rodrigues(developer) - [a49513@alunos.isel.pt](mailto:a49513@alunos.isel.pt)

:inbox_tray: Daniel Carvalho(developer) - [a49419@alunos.isel.pt](mailto:a49419@alunos.isel.pt)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
