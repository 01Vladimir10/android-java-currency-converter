# android-java-currency-converter

This is an example of how you can build a simple currency converter service in android using Google's API. You can get a free access key here: https://currencylayer.com/signup/free

Requirements: 
Volley Library
  - Add this to your App Gradle ->  implementation 'com.mcxiaoke.volley:library:1.0.1'
Changes to the Manifest:
      - Under the application tag ->
                  &lt;uses-library android:name="org.apache.http.legacy" android:required="false" /&gt;
      - Under the Permissions tag ->
                  &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /&gt;
                  &lt;uses-permission android:name="android.permission.INTERNET" /&gt;
            

