# imagefile

# Add it in your root build.gradle at the end of repositories:

        allprojects { 
                repositories { 
                        maven { 
                        url "https://jitpack.io" 
                        } 
                }
        }

# And add this to your module's build.gradle

        dependencies {
               implementation 'com.github.GopikrishnanN:imagefile:1.0.0'
        }
