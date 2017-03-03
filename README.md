#PETrA: Power Estimation Tool for Android

PETrA is a software able to estimate the energy consumption of method calls in Android apps.
It is based on some Android tools that are Monkey, Batterystats, Systrace, and dmtracedump.

For more information on these tools please take a look to the references.

PETrA is the outcome of the research conducted by the Software Engineering Lab of the University of Salerno, Italy.

##Requirements

In order to properly work PETrA needs a working Android Development Environment. You can find more information on how install Android SDK here:

https://spring.io/guides/gs/android/

##Running PETrA

PETrA can be run using this command from terminal.

```
chmod +x PETrA.sh
./PETrA.sh
```

##Hiding the status bar

If you would like to hide the status bar to allow Monkey to not lose focus from the app under test please refer to SIMIASQUE.

https://github.com/Orange-OpenSource/simiasque

##References

https://developer.android.com/studio/test/monkey.html

https://developer.android.com/studio/profile/battery-historian.html

https://developer.android.com/studio/profile/systrace.html

https://developer.android.com/studio/profile/traceview.html

##License

PETrA is released under MIT License.

Icons made by Freepik from www.flaticon.com is licensed by CC 3.0 BY.

Icons made by Madebyoliver from www.flaticon.com is licensed by CC 3.0 BY.

Icons made by Kirill Kazachek from www.flaticon.com is licensed by CC 3.0 BY.
