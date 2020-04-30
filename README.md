# ParkMapProject

## Introduction
This project is an Android application written in Java that provides users navigation and information about available motorbikes parking lots in Ho Chi Minh City for every location. This project is used to take part in the DSC Solution Challenge 2020 of Google.

## Components

The final code includes:
- Splash Screen Activity: the activity showing up every time the application is launched
- Main Activity: including a Host Fragment that can be replaced by 1 of 3 fragments: Maps Fragment, Dashboard Fragment and Me Fragment.
1. The Maps Fragment include a GoogleMap, an Autocomplete Search, a Cluster Item Manager. This fragment controls listeners when users choose a place from Autocomplete or click the GPS icon. On clicking the info window of each cluster item, the corresponding Parking Lot Activity will be launched.
2. The Dashboard Fragment includes a Recycler View that displays all parking lots and a search EditText that can filter the Recycler Viewholders. On clicking a viewholder, the  corresponding Parking Lot Activity will be launched.
3. The Me Fragment includes 3 options: How to use, Feedback and About us. On clicking each option, the corresponding activity will be launched.
- Parking Lot Activity: showcases the details of each parking lot including name, address, parking price, comments and photos. The user can contribute by adding a photo from their local storage and the photo will be automatically uploaded to Firebase.
- HowTo Activity, Feedback Activity and About Activity: respectively show the instructions to use the application, the form to give feedback about app performance and an introduction about our project.

## Run the code

This project uses a API Key that enables 4 APIs: Maps SDK for Android, Places API, Geocoding API and Directions API. Its data is retrieved from a real-time database on Firbase. In this repository, the API Key has been removed because of security reasons. You must insert a new API Key that enables the above 4 APIs in debug/res/values/google_maps_api.xml to run the code.
