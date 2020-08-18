# silver_stations
special station ability mod for starmade

this is a mod for starmade  
it requires the starloader mod by Jake0997 to function.

description:  
this mod adds powerful abilities to stations. these abilites include:   
-anchor stations: system wide jump inhibitors that pull every jumping ship to their location (working alpha  
-radar stations: system wide knowledge of approximate ship positions/movement  (planned)
-factory stations: passive mining/resource generation, possibly fueled by nearby stars (planned)
  
 core idea:  
 Make non-homebase stations viable and make empire building/ system control possible through powerful defense mechanics like jump control (anchor stations), better intel (radar stations) and resource rewards (passive mining)
 
   
Contact me for any suggestions etc.  
irnsght@gmail.com  
  
this branch takes a different approach at logging stations. Instead of logging specific stations that match conditions for (f.e.) anchor stations, all stations are logged. Events and loops update their status and once the conditon for anchor station is met, its auto activated.  
-> gets rid of text based activation  
-> allows an infinite number of station types to be logged into one file.
