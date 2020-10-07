# alcore
ahsailabs's android library core

There are a lot of utility that you can use from this library :
1. Form Builder
2. Form Validation
3. Android Permission Handler
4. Splash Page
5. Login Page
6. OnBoarding Page
7. Version Change History Page
8. Push Notification Handler With/Without Notification List Page
9. Webview Page
10. Percentage Page
11. RecyclerView that support load more, swype, drag
12. About Page
13. Data Intent Service
14. SMS Listener
15. View Binding Utility
16. Table View Utility
17. Date String Utility
18. Show Dialog/SnackBar Utility
19. Show Date/Time/Contact/File/Place Picker
20. SQLiteWrapper (multi database, support migration plan, simple use, without reflection, lightweight)
21. Lookup table (powered by SQLiteWrapper)
22. and many more in package folder 'com.zaitunlabs.zlcore.utils' especially 'CommonUtils' static class






How to use this library:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.ahsai001:alcore:{latest_release_version}'
          
          or use individually
          implementation 'com.github.ahsai001.alcore:alutils:{latest_release_version}'
          implementation 'com.github.ahsai001.alcore:alcore:{latest_release_version}'
          implementation 'com.github.ahsai001.alcore:zlcore:{latest_release_version}'
	}
