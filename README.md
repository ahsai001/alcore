# alcore - com.github.ahsai001:alcore
ahsailabs's android library core


### alutils - com.github.ahsai001.alcore:alutils
1. Form Validation
1. Android Permission Handler
1. View Binding Utility
1. Table View Utility
1. Date String Utility
1. Show Dialog/SnackBar Utility
1. Show Date/Time/Contact/File/Place Picker
1. and many more in package folder 'com.ahsailabs.alutils' especially 'CommonUtils' static class

### alcore - com.github.ahsai001.alcore:alcore
1. Form Builder
1. Splash Page
1. Login Page
1. OnBoarding Page
1. Push Notification Handler With/Without Notification List Page
1. Webview Page
1. Percentage Page
1. RecyclerView that support load more, swype, drag
1. Data Intent Service
1. SMS Listener

### zlcore - com.github.ahsai001.alcore:zlcore
1. Version Change History Page
1. About Page 
1. Store List Page
1. Bookmark List Page
1. App Recommendation Page


### How to use this library:

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
