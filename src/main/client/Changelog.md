# 15.01.2018
* Fixed stupid null value bug in time selectors

# 14.01.2018
## GUI Fixes
* Fixed bug with field highlighting. 
* Fixed bug with cells not showing all information.
* Closing the app will attempt to inform the server of a logout.

## General Improvements
* Users with a "Full" subscription will not be allowed to order a parking space for more than 14 days.

## Login / Register
* Fixing some UI bugs with deleting cars.
* Added Email validation in registration/login. `u` is still accepted though.

## Subscriptions
* "View Subscription" screen finished (untested)  
* "Add Subscription" screen finished (untested)


# 13.01.2018
## General
* Handled error message on the client will only be printed if the --debug flag is active.

## Login / Registration
* Added "Password" field
* Grouped and ordered FXML decleration
* Upgraded "Session" object to suppert multiple types of users.
* Fixed register/login flow to work with new implementations.

## Enter Parking
* Car selection is now a combobox 
* This form now longer allows selection of parking lots. It will use the parking lot the user is currently logged into.

## Manage Cars
* Manage Cars screen now ready to rock.

## View Preorders
* Fixed the 'Refresh' functionality.