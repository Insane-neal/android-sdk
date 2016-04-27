Change Log
==========

Version 1.0.3 *(2015-02-08)*
----------------------------
* Update Java SDK version
* New build parameters for SDK: 
   * environment (production/development)
   * log level for API calls
   * device model caching


Version 1.0.2 *(2015-09-23)*
----------------------------
* Fix compilation Java version

Version 1.0.0 *(2015-09-22)*
----------------------------
* Integrate relayr Java-SDK
* Refactor package names to separate Java from Android
* Add device models - replace static model ids with dynamic ones
   * model cache
   * JSON value schema support
* Prototype device creation and publishing
* Add third party account support
* Add groups support

Version 0.0.13 *(2015-04-30)*
----------------------------
 * BLE DirectConnection mode
   * Fix data parsing for Light/Proximity sensor
   * Send command to a sensor over BLE
   * Bridge module data parsing
 * Remove unnecessary API calls

Version 0.0.12 *(2015-03-04)*
----------------------------
 * Fix problem with debug data parsing and incomplete test.

Version 0.0.11 *(2015-03-04)*
----------------------------
 * Use the RelayrSdk.Builder to initialise the SDK.
 * Reset SDK automatically every time it's rebuilt.
 * Make API calls directly from models to reduce verbosity.
 * Change Readings structure.
 * Remove BleReadings and always use Readings instead.

Version 0.0.10 *(2015-02-27)*
----------------------------
 * Change Model and DeviceModel entities to support new readings and commands.
 * Fix connection lost problem.

Version 0.0.9 *(2015-02-26)*
----------------------------
 * Add configure-devices scope to login URL.
 * Fix non-resizable list bug.

Version 0.0.8 *(2015-02-25)*
----------------------------
 * Replace PubNub with MQTT protocol for streaming data from devices. 
 * Breaking changes: 
    * sendCommand API call url and parameters.
    * Move Reading model to BleReading.
    * New Reading model structure.
 
Version 0.0.7 *(2015-01-16)*
----------------------------
 * Fix mock method isPlatformReachable in reachability utils to be always reachable.

Version 0.0.6 *(2015-01-16)*
----------------------------
 * Stabilise BLE (Bluetooth Low Energy) connections.
 * Batching for logging and ability to flush all logged messages still in memory.
 * Reachability calls to see whether our platform is accessible.
 * Reachability and permissions check in LoginActivity to avoid friction with new users when 
   forgetting permissions.
 * Breaking changes: WebSocketClient returns an Observable instead of a Subscription on subscribe 
   to values.

Version 0.0.5 *(2014-11-27)*
----------------------------
 * Provide the ability to log events / messages
 * Make BLE more resilient
 * Remove deprecated clientId property and use given redirectUri property for oauth
 * Update rxjava to 1.0.0
 * New API calls:
    * Get Bookmarked Device
    * Get Public Devices
    * Get Device Model
    * Get Device Models
    * Register Transmitter
    * Bookmark Device
    * Remove Bookmark

Version 0.0.4 *(2014-10-22)*
----------------------------
 * Fix: SDK crash on Android API versions 15-17 since the BLE classes cannot be found.
 * New: Command API call for devices.

Version 0.0.3 *(2014-10-16)*
----------------------------
 * New: Connection to devices over BLE (Bluetooth Low Energy).
   * Use the RelayrBleSdk to scan for BleDevices.
   * Connect to BleDevices to be able to read, write and subscribe to their data values.

Version 0.0.1 *(2014-08-20)*
----------------------------
Initial release.