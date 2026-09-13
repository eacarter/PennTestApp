# DECISIONS

- For "Already Inside" I created a function that that gets the current
location of the user and compares the coordinates of the user to the coordinates of the 
venue. First it gets the Users current/last known location and emits it to 
a location variable. From there the "distance between" function from the Location
class is used to calculate the user coordinates between the venues coordinates. 
If i had another week id probably build a function that parse the venues for larger
venue list, as is it seems fine but if the list was larger chances are parsing would 
have to happen on the app side of back-end to cut down the list of available venues 
in the vicinity of the user. I also use the balance power priority for getCurrentLocation.
thought additionally, there probably could be an additional function written here to 
to swap it from balanced to low if the user has been in the venue for X amount of time.
- For my smoothing method, A simple moving average over the last 5 reading were used. 
Again like stated before a more sophisticated smoothing model would be needed for 
better accuracy and adding a additional week would have allowed me to explore other 
options when it comes this portion of the assessment.
- For battery tradeoffs, i didnt do any extensive testing on the particular elements of 
the Location services or the BLE classes to explore them further I just chose Low Power 
for the BLE scan mode and a Balanced priority for the initial "containment" function  
for getting current location. I figured these would be the best viable option know that 
both of these processes can eat up battery power fast but can potentially run into accuracy 
issues at some point. That why I stated earlier that given another week... maybe another 2
I guess, I'd actually run physical test for accuracy depending on the power usage or
just figure out a function that would alter the power usage based on the users movement maybe.
its an idea at least, there could be worse.
- As for the foreground service, this ended up being necessary due to the BLE scanning 
needing to be available for as long as the user is inside the venue and had access to a 
beacon in the venue and theres no guarantee that the UI is available at all times. 
Pushing the scanning to the background with no UI kills the scan functionality causing an 
exception without the foreground service. I would have also liked to explore the other 
connected types if i had an additional week, more specifically the Location connected type 
to see if accuracy changes or not.
- 