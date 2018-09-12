# Cryptrack
Cryptrack is an Android application to track your cryptocurrency portfolio. 
The current implementation, written entirley in Kotlin, has the following features built & running:
* Initial Login Setup
* Pin lock when opening/re-opening the application for increased security
* Attractive dark theme UI with [Material Design](https://material.io) in mind
* Fetching realtime data from a Firebase Database instance
* Ability to add user selected coins to a personalized watchlist
* Search for a coin
* Custom graph views to for viewing historical coin data
* Fetching news on coins

## Notable Programming Topics Utilized
### RESTful API Calls
#### Retrofit2
API calls made to the [News API](https://newsapi.org) as well as calls for made for retrieving historical coin data for building graphs are made via the use 
of RxJava and [Retrofit2](http://square.github.io/retrofit/). Retrofit2 makes it very easy to create HTTP queries to RESTful web services 
through the utilization of 
Kotlin/Java interfaces. 

#### RxJava
[RxJava](https://github.com/ReactiveX/RxJava) allows for asynchronous events that follow the 
[observer pattern](https://en.wikipedia.org/wiki/Observer_pattern). This means the user can still access his/her portfolio (using data from the SQLite Database) without having to wait for the applicaiton to finish retrieving data from the external server. 

#### Google Firebase
My first implementation used to pull coin data from the [Cryptocompare API](https://www.cryptocompare.com) used an asynchronous process 
created by RxJava and made a RESTful API call directly to the cryptocompare endpoints. While this worked, it was also slow to 
complete the data fetch. My solution to this issue was to create a [Firebase Database](https://firebase.google.com/docs/database/)  instance via [Google's Firebase](https://firebase.google.com) 
and write a [Firebase Function](https://firebase.google.com/docs/functions/) that populates the database with all required information every 10 minutes. The Firebase function is 
triggered by a chron-job that pings the Firebase function's http url every 10 minutes. Pulling the data from the Firebase Instance was 
much faster than retrieving data from the endpoint from cryptocompare's API.

### Custom Views
The graphs present in the application were all done by creating [custom views](https://developer.android.com/guide/topics/ui/custom-components.html) in Kotlin. The onDraw() and onLayout() functions here were 
crucial to the proper drawing of the graphs onto the screen. The same graph view is used for the small graphs presented when viewing 
multiple coins as well as the large graph view when looking at the price history of a single coin.

## Screenshots
<p align="center">
  <img src="https://farm5.staticflickr.com/4757/39493944935_6bfd6b7386_o.jpg" width="300">
  <img src="https://farm5.staticflickr.com/4723/39493944905_d0b4df84bf_o.jpg" width="300">
  <img src="https://farm5.staticflickr.com/4724/40390715311_b2ee1bc687_o.jpg" width="300">
  <img src="https://farm5.staticflickr.com/4745/40345813572_6bdaffceae_o.jpg" width="300">
  <img src="https://farm5.staticflickr.com/4618/25518641857_92852aafb8_o.jpg" width="300">
</p>
