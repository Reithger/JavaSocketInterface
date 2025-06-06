# JavaSocketInterface

Revamp of the original simple dual-thread manager of listening to a socket communication hooked up to a Runtime.getRuntime().exec sub-program on the same port.

Now has a central accessor for managing multiple, separate listening contexts, can listen to and send messages on multiple ports with or without an associated sub-program, can
run Java or Python sub-programs that can be run unattached to a listener, has a file validator built-in that will take a source file within the exported .jar and write it to
the local context of the running program for usage.

Main things I wanted in the overhaul was socket communication between Java programs and being able to run a Java sub-program easily. This now includes JavaReceiver and JavaSender
interfaces for you to implement classes with in your own project and pass into SocketControl to either receive messages from the sockets as they come in or get access to a message
sending class to send messages out on the port you've attached to.

Basically this should provide a simple, one-stop shop interface for doing socket stuff easily (primarily carpeting over the thread management of a listening thread, a timeout thread
for when the connection died, and a keep-alive thread to periodically send messages across a socket to ensure the connection is verified as alive). Also easily running sub-programs and
passing them a port number as a CLI argument and both validating the necessary subprogram files are present as expected and copying out a new version of the file from a reference if the
file is missing, corrupted, or out of date with the version currently in your reference data.

Just get the JavaSocketInterface.jar file and add it to your project's build path!

![JavaSocketTalk June 6 - Post Post Overhaul](https://github.com/user-attachments/assets/edbd8b76-13d2-4ce0-bbaf-b506e3f34093)
