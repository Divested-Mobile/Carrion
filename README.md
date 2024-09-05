Carrion
==========

Automatically declines all calls failing STIR/SHAKEN verification.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/us.spotco.carrion/)
[<img src="https://hosted.weblate.org/widget/divestos/carrion/287x66-grey.png"
     alt="Translation status"
     height="66">](https://hosted.weblate.org/engage/divestos/)

What?
-----
- Ever receive a spam call and try to call it back, only for it to be out of service or some stranger? This blocks that.
- STIR/SHAKEN provides cryptographic attestation of caller ID and is mandated by law in USA and Canada
- https://www.fcc.gov/call-authentication
- https://en.wikipedia.org/wiki/STIR/SHAKEN
- https://fccprod.servicenowservices.com/rmd?id=rmd_listings

Usage
-----
- Install the app, grant the screening permission, nothing more!
- A call that failed verification will be disallowed and a failure notification shown
- A call that is unsigned will be allowed and a notification shown
  - You can optionally choose to silence or block verification unsigned calls
- A call that is verified will be allowed without any notification
- Calls from contacts or emergency numbers are always allowed

Database Usage
--------------
- For extra coverage and to enable functionality on devices without STIR/SHAKEN there is a local database lookup feature
- Calls from numbers matched in the database will not be declined, but will be silenced
- The archive database contains all numbers reported at least ten times in the past 360 data files
- The high confidence database contains all numbers reported at least twice in the past 90 data files + the archive database
- The "full" database contains all numbers reported in the past 30 data files + the high confidence database
- You must manually download the database occasionally
- The database function can be disabled via the `delete database` button
- There is more information about the database here: https://www.ftc.gov/policy-notices/open-government/data-sets/do-not-call-data

Requirements
------------
- VoLTE must be working
- Your device must have official support for Android 11 or higher
- If seemingly all received calls are unknown/unsigned state, your device/carrier is likely incompatible

Notes
-----
- Contacts permission is NOT required/used for exclusion
  - The system itself will not pass calls from contacts to the active Call Screening app
- The robocaller database is entirely optional, STIR/SHAKEN verification is completely independent
- Internet is only optionally used to download the robocaller database
  - Caller information, stats, and other information is never sent anywhere
- The stats feature is entirely local for the user to observe effectiveness and is never sent anywhere
- The archive database is included in both high and full database and cannot be used on its own
- If you want to switch between databases and you just recently updated you must first delete the database
  - This is because the app doesn't know which is actually saved and both databases likely have the same generation timestamp
- The 'Excluded' stat counts outgoing calls to non-contacts, outgoing emergency calls, and all non-contact calls during emergency callback mode
- Your carrier likely already does filter out calls that entirely fail verification
- Your carrier likely does not filter out calls that are unsigned or partially signed

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

Translations
------------
- Chinese (Simplified): Crit
- Croatian: lukapiplica
- Estonian: Priit Jõerüüt
- Galician: josé m
- German: thereisnoanderson
- Indonesian: Adrien N
- Norwegian Bokmål: Even Bull-Tornøe
- Portuguese (Brazil): lucasmz-dev
- Portuguese: ssantos
- Spanish: vzamanillo, gallegonovato
- Ukrainian: Fqwe1

Donate
-------
- https://divested.dev/donate
