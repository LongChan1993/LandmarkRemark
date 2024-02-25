
# Landmark Remark Appilcation

The native Android application is written by Kotlin using Android studio and no third-party (non-Google) libraries. Google Cloud Firestore has been used as the backend-as-a-service provider.

## Design & Architecture

The app follows MVVM architecture with the following concepts utilized in the application

- Data binding
- View binding
- View Model

Following are the Program files

- LoginActivity - Sign in the app using a unique "username"
- GoogleMapActivity - The main screen of the app displaying the map and its controls
- LocationViewModel - The view model class to bind the view with UI controller(GoogleMapActivity) data
- Util - The class provide common function for all part of program files.
- Constant - The class provide static constant for all part of program files.
- ResourceProvider - The class provide resource access in limited access area.

Others

- Google Maps API for android
- Custom fonts
- Firebase & Google play services libraries

## Requirements
- Display current location on the map.
- Save a short note at the current location.
- See notes that a user have saved at the location they were saved on the map.
- See the location, text, and user-name of notes other users have saved.
- Search for a note based on contained text or username.

## Application Flow
- Signing in by inputing a unique username which is an entry point to the application.
- On singing in , user is presented with a location permissions dialog with the below access actions.
- Accept - On accepting, the current location of the user is displayed on the map.
- Deny - The map does not display the current location of user and instead a snackbar is displayed informing the user to enable location access. The user in this state will be able to create markers and notes normally.
- On sign in , the application checks for any location data from the firebase, loads and displays on the map.
- Displaying current user location by using a blue dot.
- Map click event to add a new marker with an associated note presented by an input dialog.The marker is represented by a standard black location icon.
- On clicking the marker, a custom info window is displayed with the following attribute data
    - Note
    - Username(creator)
    - Location(latitude and longitude)
- On clicking an info window, the user who created the note has an option to edit the note and update it.
- If a user clicks on info windows not created by him/her , he/she will not be able to edit the note and a toast message is displayed to inform the user.
- On searching based on a note or username, display a snackbar about the number of occurences of the search result.
- The searched result displays the corresponding map markers highlighted by a standard red location icon.
- On clearing the search bar the map markers are reset back to the black location icons.
- Menu has two option : search by google and logout option.
- A logout option on the menu to help the user log out.
- A function search by google used latest library Place API provided by Google. Which user can search real place and transit to this place and marked it by red mark. If user want, can click red mark and add note to this place. After that, this marker will change black.

## Tech Stack
- Android Studio using Kotlin
- Firebase Cloudstore
- Google Maps SDK and API
- MVVM Android Architecture

## Limitations
- Firebase database access have short life-time, because using free account. So that, we can advanced this by change to use local database, such as sqlite or room database.
- The map view activity of the application resets everytime a new note is added which means the activity is recreated. We can come up with an alternative to avoid this.

## Time Consumed
Total time 20 hours,it consumes more time than expect because api google deprecated, it provide new api need more test and implement.

- Thinking a solution for challenge  - 1hr
- Read documentation about new google api - 2hrs
- Register google map platform, firebase - 0.5hr
- Initial map - 0.5h
- Current location + permissions - 0.5hr
- Adding landmarks - 1hr
- Adding Firebase - 0.5hr
- Searching landmarks - 3hrs
- Redesigning the app - 2hrs
- Changing landmark icons to differentiate between searched and existing - 1hr
- Construct code in MVVM architecture - 1hr
- Custom dialogs for notes, marker info windows - 2hrs
- Testing and optimizing - 3hrs
- Final testing, code cleanup, documentation - 2hrs

## Support
- Github
- StackOverflow
