# Penn Test Application 
### (Not Red Team or Blue Team related)

Venue geofencing application that uses googles location services geofencing api 
with BLE scanning for a finner user location tracking. The UI is simple, it just 
tell you whether the user is within or outside the fence and when a beacon is 
detected lets you know how far or how close you are to it. 

## Beacon Testing
For beacon testing the use of NRF connect was selected due to my familiarity of it. 
I have provide the steps to setup my beacon below (EddyStone was used):

- Open NRF Connect App
- Tap `Advertiser`
- Provide a display name (i.e. Venue1-CoffeeShop-Eddystone)
- Provide Service Data
- UUID: FEAA, DATA: 00AABBCCDDEEFF0011AABB112233445566
- Tap `OK`
- Switch On or Off as you please

## Geofence Testing
For Geofence testing i followed the instructions and created a .json asset with 2 venues 
object inside. The locations i kind of just chose at random but they are a Coffee Shop 
and the old main library in downtown Detroit. to reproduce the transitions ive added the 
steps below, to mock my location i used a free app called LocaEdit:

- Open the Penn Test Application alongside the LocaEdit App.
- Make sure LocalEdit is selected as your location mocking app in your developer options.
- Observe that the Penn Test App shows no logs and that the user is outside the fence 
and not near a beacon
- Open the LocalEdit app and type the coordinates of one of the venues into your search bar
the json file will be below.
- Once you have found and selected the location as you mock, head back to the Penn App and
observe that the app shows that you are within the bounds of the geofence. Combined with your
beacon from above, you should also see, proximity and rssi

<img width="800" height="1280" alt="Screen_Recording_20260913_111836_PennTestApp-ezgif com-video-to-gif-converter" src="https://github.com/user-attachments/assets/c831c54e-01dc-489f-a723-506b6ad172c9" />


