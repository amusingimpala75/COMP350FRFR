# Hall Monitor Scheduling App

## Description

This scheduling app seeks to offer an alternative to [myGCC](http://my.gcc.edu).
Once the courses listed are loaded into the system, the browser enables the user to
look at courses primarily by a search query, and by applying optional filters. The user
can then create a candidate schedule which they can revisit at a later time.

## Frontend Dependencies

### npm and Node.js

Firstly, check if npm and Node.js are already installed using ```npm -v``` and ```node -v```.
If these are not installed, follow instructions from the [Download Node.js page](https://nodejs.org/en/download/).
Once installed, verify using ```npm -v``` and ```node -v```.

### TypeScript

To install the latest stable version of TypeScript, run ```npm install -D typescript```.

### Vite

To install the latest stable version of Vite, run ```npm install -D vite```.

## Backend Dependencies

### Java

A java JDK installation of version at least 21 is required to build and run this project.
You can install one with ```winget install EclipseAdoptium.Temurin.21.JDK```.

### Python
A python version of 3.10 or higher is required to run this project.


## Running

Currently, we do not have a prebuilt executable. You can run the project by executing the `run` gradle task.
There are two ways to do this:
1. Change directories to the backend folder and run `./gradlew run`
2. Open the project in IntelliJ and execute the `application/run` gradle task from
   the right hand menu.
3. From a terminal inside the python-backend folder, run `python -m venv venv` to create the virtual environment.
4. Activate the Venv by running `venv\Scripts\activate` if using Windows, or `source venv/bin/activate` for Mac/Linux.
5. Run `pip install -r requirements.txt` to download the python backend dependencies. 
6. Run `uvicorn main:app --reload` to start the application.
7. Navigate to `http://localhost:7070`.


## OCI Containers
If you have Docker or Podman installed you can create an OCI container image .tar by running `./gradlew containerBuild` in the backend folder
