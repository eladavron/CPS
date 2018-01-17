# 17.01.2018
* Added SQL sanitation through StringUtils. Use `StringUtils.sanitizeForSQL(text)` to get a sanitized string for
 insertion, and then use `StringUtils.desanitizeFromSQL(text)` to get a de-sanitized string.

* Made sure a user couldn't register the same email twice.

# 16.01.2018
## Complaint Flow - COMPLETE!
Customers can now create and cancel complains.

## Generic Client Side Improvements
* Vastly improved the `MessageTasker` class to not be so cumbersome to construct.

## Generic Server Side Improvements
* Server now supports console commands, such as:
  * `refresh`: Re-inits all controllers (including DB). 
  * `sessions`: Shows all active sessions.
  * `drop`: Allows you to drop a specific session (will ask you which)
  * `purge`: Drops ALL sessions.
  * `exit`: Exits the server.
  
 * Improved entire login and validation flow.
 * Simplified the "Session" object to no longer hold a private field for `UserType` (it was only used in creation
  anyway, and after that you can just pull it from the user).
  * Improved erorr-handling immensely

## Employee Screen
* Logging in as an employee now display the newly implemented Employee screen.
* If logging in at a certain parking lot and the employee happens to be the manager of that parking lot,
  they will also see the "manage" panels.
* Created an **AWESOME** view for parking lots, though still untested, that will allow to see and manage parking spaces.
It's still just a graphical skeleton (with init logic), the status changing logic still needs to be implemented.

## GUI Fixes
* Fixed a bug where going back from a 2nd level screen would display an empty gui.
* GUI error screens will now be more verbose and allow seeing the stack trace.

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