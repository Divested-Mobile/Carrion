Carrion
==========

Automatically rejects all calls failing STIR/SHAKEN verification.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/us.spotco.carrion/)

What?
-----
- Ever receieve a spam call and try to call it back, only for it to be out of service or some stranger? This blocks that.
- STIR/SHAKEN provides cryptographic attestation of caller ID and is mandated by law in USA and Canada
- https://www.fcc.gov/call-authentication
- https://en.wikipedia.org/wiki/STIR/SHAKEN
- https://fccprod.servicenowservices.com/rmd?id=rmd_listings

Usage
-----
- Install the app, grant the screening perission, nothing more!
- A call that failed verification will be disallowed and a failure notification shown
- A call that is unknown will be allowed and a notification shown
- A call that is verified will be allowed without any notification
- Calls from contacts or emergency numbers are always allowed

Requirements
------------
- VoLTE must be working
- Your device must have official support for Android 11 or higher
- If seemingly all received calls are unknown state, your device/carrier is likely incompatible

Device Compatibility
--------------------
- The below is not a guarantee, just a minimum requirement
- Pixel 4 and newer
- OnePlus 5 and newer
- Fairphone 3 and newer
- F(x)tec Pro¹ X

Known Issues
------------
- May fail to ask for screening permission, close the app and open until it prompts

Credits
-------
- Icons: Google/Android/AOSP, License: Apache 2.0, https://google.github.io/material-design-icons/

Donate
-------
- https://divested.dev/donate
