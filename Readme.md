# Fall Detection System for Wearables

> [Smartwatch Component ](https://github.com/ionascuandrei/fallDetection-Watch)
>
> [Machine Learning Component ](https://github.com/ionascuandrei/fallDetection-Classifier)
>
> Mobile Component

<p align="center">
<img src="https://i.ibb.co/vZnhT9Z/flow.png" width="67%"/>
</p>

## Usage

For the moment, the system needs to be deployed on the personal smartphone using **Android Studio**. After the deployment, it is required the **[Smartwatch Component](https://github.com/ionascuandrei/fallDetection-Watch)** on the personal smartwatch.

In the future, it may be available on the Android Store.

## Introduction

The  objective  of  this  system is  to  **properly  detect  falling**,  which  is  the  most  common  injury sustained  by  the  elderly.   This  system  aims  to  offer  a  classifier  built  on  a  **portable  system which  is  able  to  detect  the  wearer’s  falls**.    A  companion  able  to  announce  the  accidents will  decrease  the  risk  of  medical  complication  as  the  time  passes  without  help.   

We  built the  entire  system  with  portability  in  mind,  so  it  can  be  available  for  a  large  number  of users by **using only smartwatch and the personal phone**.  For the current prototype,  we have used  a  **Fitbit  smartwatch**  but  it  can  be  ported  on  different  devices  with  similar  architecture. 

We  transfer  the  **samples  gathered  from  watch’s  accelerometer**  to  the  phone,  where,  using  a **machine learning based classifier**, we process the data.  In the event of falling, the owner can confirm  the  accident,  if  he  is  able  to  do  so,  or  after  a  short  period  of  time,  the  system  can send an emergency call automatically.  The link between components does not need Internet connectivity so it can be available anywhere and at any time.



## Features

Our system provides the following features:

- Local fall detection based on machine learning classifier
  
- trained to distinguish fall from daily activities.
  
- Wearable and smartphone integration
  
- test data are collected from the smartwatch and sent for classification to the smartphone
  
- Wearable notification upon classification
  
- if a fall was detected, the wearer is asked on the smartwatch if he/she is alright
  
- Possibility to integrate emergency calling

  - upon watch notification confirmation or expiring the allocated 10 seconds, the system can send an emergency call.

    

## Architecture Overview

Designing a multi-platform solution, we have split the system in three main components: **Smartwatch Component**, **Mobile Component** and **Machine Learning Component**.

<p align="center">
<img src="https://i.ibb.co/kck2s1z/components-Overview.png" alt="componentsOverview" width="67%" />
</p>


**Smartwatch Component** represents the data acquisition layer implemented for Fitbit devices using the Fitbit SDK and tools. It consists of two components: the Application one that uses Fitbit's Device API to collect motion sensor data and the Companion one, which receives the data from the Application and runs inside the mobile Fitbit application.

**Mobile Component** represents our Android application installed on the smartphone. We have based it on a WebSocket server which communicates with the Companion and the built-in Machine Learning Component. In this application we are processing and extracting the required features for the classifier.

**Machine Learning Component** represents the Support Vector Machine Classifier. Using it, we extract the features needed to mark an action as a fall or a normal daily activity.

 All these components are communicating with each other with the purpose to detect if the wearer had a fall. The system does not require Internet connection to function after installation or other widgets apart from the smartphone and the smartwatch. The communication between the smartwatch application and the companion is over Bluetooth and uses the available messaging APIs provided by Fitbit SDK. The companion and the Android application will communicate via WebSocket messages. These
 messages are sent locally between the Fitbit app and the server hosted on the same mobile device.

<p align="center">
<img src="https://i.ibb.co/8x9pNGp/System-Overview.png" alt="systemOverview" width="67%" />
</p>

## Mobile Component

As the system core, we built the Mobile Component. It contains the classifier which is handling with the fall detection, and all the main processing components. It was designed to support the entire flow without any external processing or intervention, for portability purposes. We integrated it for the Android platform and is communicating with the Fitbit Companion. It also incorporate the machine learning component which is handling the classification. This structure focuses on the availability. 

As we mentioned before, the current architecture of smartphones is very powerful and able to sustain lite machine learning algorithms without problems. There is no need of external communication, all being available locally using internal links and Bluetooth connection for the smartwatch. Furthermore, the entire flow has low latency, which is 5 seconds, being essential for a time sensitive application like this.

### Structure

We built this component as an Android application. It contains the server which communicates with the Fitbit Companion and the Machine Learning Component. 

The server is an **WebSocket** server based on [TooTallNate](https://github.com/TooTallNate/Java-WebSocket)'s Java implementation.  We chose this type of server to match the already implemented system of the Fitbit devices. Once started, the server listens on the localhost address on a designated port for all the incoming messages and connections. It receives the accelerometer data from the smartwatch, passes it to the Machine Learning Component and sends back the classification to the wearable device. 

In the current state, the server can be opened and closed from the user interface, notifying the connected clients. From a software point of view, binding a socket to the same port in the same thread is not possible. To do so, we had to create e mechanism which will close and clear the current server thread and initiate a new one. This is required in order to keep a static address:port pair which is known to all the clients, keeping the structure as simple as possible. The mechanism was created with an internal Singleton because it is necessary to get in touch with an active server upon application restoration.  

### Interface

For the mobile application we implemented a simple view dedicated for the user, all the functions and processing being available in the back-end. It contains two buttons from which we can start and stop the server and a debug panel. The panel represents the most of the view containing all the needed notifications: when the client is connecting or disconnecting, the result of the classification and link tests.


<p align="center">
<img src="https://i.ibb.co/DRjb82n/mobile-explained-Menu.png" alt="mobile-explainedMenu" width="67%" />
</p>


## Smartwatch Component & Machine Learning Component

**Machine Learning Component** is already integrated in the mobile application. More details of the implementation can be found at the following repository - [Machine Learning Component.](https://github.com/ionascuandrei/fallDetection-Classifier) 

**Smartwatch Component** represents an external application which can be found at the following repository - [Smartwatch Component.](https://github.com/ionascuandrei/fallDetection-Watch)



## Workflow

The main idea behind the system is to gather acceleration data from the smartwatch during the day. In 10 seconds intervals, these values are pre-parsed and packed in the smartwatch and sent to the Fitbit Companion installed in the smartphone. Upon receiving all the required data, we send them to the server. There are parsed and prepared for the classification. The classifier is receiving the parsing data and classifies the input as Fall or as Daily Action. This result is transferred to the wearer smartwatch. If the result marks an actual fall, the wearer is asked for confirmation. If confirmed or the time expires, the smartwatch can send an alert message to emergency contacts.


<p align="center">
<img src="https://i.ibb.co/vZnhT9Z/flow.png" width="67%"/>
</p>


## Testing Environment

We built the mobile component compatible with at least Android API 26 - Android 8.0 (Oreo). It was necessary due to compatibility of WebSocket module used for the server. It was successfully tested on a Samsung Galaxy S10e built with Android 10.

The smartwatch application is built and tested on a Fitbit Versa watch with the 32.70.80.0 version. The software is compatible with all the Fitbit smartwatches upon small modifications over the graphical interface and physical button shortcuts.

During the development phase, the entire system was subject to various stress tests. 

We let the server from the mobile component active for approximately seven hours while testing the entire flow. In this time, the application was minimised, resized and rotated from the smartwatch to check if the link with the processing thread is kept active. Also, we blocked the smartphone or used it for different applications resulting that the application is built without bugs or errors. 

The smartwatch component was tested as well. We checked all the possible flows, testing if the application will respond properly even if the requirement are not fulfilled, E.g.: Starting the classifier test even if we are not connected to the server will result a warning notification.



## Future Work

Although the application has great features, the system requires adjustments and has room for additional improvements. It represents a great starting point in this needed domain, having an important applicability and not many implementations.

For the moment, there is a major difference in the trained models from the classifier and the offered data from the watch's accelerometer. Due to privacy reasons, Fitbit is not offering owned datasets or access to the users data to third party applications and creating a consistent dataset for this domain represents a hard-working process and mostly, a time consuming one. We weren't able to create one and as replacement, we tried to use similar ones but with great cost in accuracy. The current classifier is not able to detect an actual fall because it was not trained with similar data. Even if we tested with multiple datasets and checked the correctness of the algorithm, we are not able to detect a fall with the data gathered from our accelerometer. As a result, this will be the main functionality which needs the primary focus as a future development.

Looking at the battery consumption, a permanent running cycle of classifications will end pretty quickly the battery life. To minimize this problem, we can built in the smartwatch component a threshold based detection. With it, we can initialize the classification flow only if the accelerometer data will pass implemented threshold value. This warning will mark a suspicious action from the wearer, creating a good reason to check the data with the classifier. In this way, fewer actions will pass through the entire classification flow, reducing power consumption.

As another feature for the classifier, we can create a storing module which will save all the detected falls or daily actions which passed the threshold. After a number of stored samples, the package can be sent to a web server where we can update the existing classifier model with them to actively improve future classifications. Actively upgrading the machine learning algorithm with samples from all the users, will offer a huge leap towards strong classifying results.

A great future feature for which we already prepared the system will be the possibility to send an emergency call upon fall detection. The current implementation already have prepared the flow to trigger the feature from the **Fall Notification**. This component will require a menu from where the user will be able to add emergency contacts and a link with the Phone Call module. As an expansion to the idea, we can build an accessible menu in the smartwatch from where the wearer will be able to send an emergency call in case of a not detected accident.
