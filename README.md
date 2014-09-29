AndroidTwitterClient
====================

A simple Twitter client that supports viewing a Twitter timeline and composing a new tweet.

####Total time spent: 10 Hours

####User Stories:

   * [x] User can sign in to Twitter using OAuth login
   * [x] User can view the tweets from their home timeline
     * User should be displayed the username, name, and body for each tweet
     * User should be displayed the relative timestamp for each tweet "8m", "7h"
     * User can view more tweets as they scroll with infinite pagination
     * Links in tweets are clickable and will launch the web browser (see autolink)
   * [x] User can click a “Compose” icon in the Action Bar on the top right
     * User can then enter a new tweet and post this to twitter
     * User is taken back to home timeline with new tweet visible in timeline
     * User can see a counter with total number of characters left for tweet
   * [x] User can refresh tweets timeline by pulling down to refresh
   * [x] Improve the user interface and theme the app to feel "twitter branded"
   * [x] Compose activity is replaced with a modal overlay
  
Libraries used: android-async-http-1.4.3, ActiveAndroid-3.1, codepath-oauth-0.3.0.jar, codepath-utils.jar, scribe-codepath.jar, universal-image-loader-1.8.4.jar and android-support-v4.

####Demo:
![Video Walkthrough](demo.gif)
