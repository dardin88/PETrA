[![Build Status](https://travis-ci.com/dardin88/PETrA.svg?token=693Py2p7SBGgCKKJPQaZ&branch=master)](https://travis-ci.com/dardin88/PETrA)

# PETrA: Power Estimation Tool for Android

PETrA is a software able to estimate the energy consumption of method calls in Android apps.
It is written in Java and it is based on some Android tools such as Monkey, Batterystats, Systrace, and dmtracedump.

For more info on these tools please take a look to the references.

PETrA is the outcome of the research conducted by the [Software Engineering Lab of the University of Salerno, Italy](http://www.sesa.unisa.it).

## Requirements

PETrA needs Android SDK and JRE 8. You can find more info on how to install Android SDK [here](https://spring.io/guides/gs/android/).

## Running PETrA

Use this command from terminal.

```
chmod +x PETrA.sh
./PETrA.sh
```
## Supported smartphones

PETrA has been tested on a LG Nexus 4, but other Android smartphones should work. If you are experiencing problems, please add a new issue.

## Hiding the status bar

If you would like to hide the status bar and allow Monkey to not loose focus from the app under test, please refer to [SIMIASQUE](https://github.com/Orange-OpenSource/simiasque).

## References

https://developer.android.com/studio/test/monkey.html

https://developer.android.com/studio/profile/battery-historian.html

https://developer.android.com/studio/profile/systrace.html

https://developer.android.com/studio/profile/traceview.html

## License

PETrA is released under MIT License.

Icons made by Freepik from www.flaticon.com is licensed by CC 3.0 BY.

Icons made by Madebyoliver from www.flaticon.com is licensed by CC 3.0 BY.

Icons made by Kirill Kazachek from www.flaticon.com is licensed by CC 3.0 BY.
