## Nice Reminder

 Nice Reminder is an app that utlizes GPS and the cloud to remind the user the last time they visted the location. User can take picture within the app and save those pictures that corresponds to the current location. The next time the user visit is around that location, the app will remind the user and provide an image gallery of all the photos taken around the location.


### Weekly Progress:

#### Week 1 (4/28 - 5/4):
 This week's focus is on the user authentication using firebase API. User can signin and signup using email/password through the UI. An setting activity is also added to allow the user to update the user account such as password, name, or even deactivate the account.

#### Week 2 (5/5 - 5/11):
 This week's focus is on camera and the cloud image gallery. Image taken from the camera is saved into the cloud and those images are downloaded into the cloud to from a gallery for the user to see. A number of basic feature for the image gallery is added such as grid previews and selecting indivual image. The scheduling for the gallery downloaded was down to limit the amount of data downloaded by downloading the whold gallery the first time the app opens up. After then the image saved to the cloud will be added directly to the gallery during one session of the application. Other thing worked on includes adding the option to change user profile picture from local image gallery. 
 
 Next step: The next thing to work on for the image gallery includes the opetion to delete images from the gallery and background tasks for downloading images from the cloud.
 
#### Week 3 (5/12 - 5/18):
 The main focus for this week is on the process of uploading and downloading image from the firebase storage (cloud). An IntentService was added to perform background task in another thread for uploading and downloading. Another issue that was worked on this week is the image quailty of the image saved to the cloud. The image quailty before was poor since I did not save the image on the device first before saving it on the cloud. Now that the image is saved in full quailty, the time for download and upload sigificantly increased. The tradeoff is not important to the main point of the project, so I didn't do too much to increase the performance of cloud access. The only thing that was done is to remove the needs to download the image if the device still have the image in its local image gallery. In that case, the image data is read from the device instead of downloading from the cloud.
 
 Next Step, locationzation features.
#### Week 4 (5/19 - 5/25):
#### Week 5 (5/26 - 6/1):
#### Week 6 (6/2 - 6/8):
#### Final Week:
