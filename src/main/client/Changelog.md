# 14.01.2018
* **12:11** - Fixing some UI bugs with deleting cars.
* **15:15** - Added Email validation in registration/login. `u` is still accepted though.
* **16:17** - Fixed bug with field highlighting. Started working on Subscription screen.
* Fixed bug with cells not showing all information.
* **18:05** - Finished the "Add Subscription" GUI and validation. Still WIP: The client/server flow.
* **20:15** - Finished the entire "Subscription" GUI and client-side logic.

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