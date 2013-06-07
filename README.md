# whassup

Provides access to WhatsApp messages stored on your Android phone, provided that automatic
backups are enabled in the settings. For information about WhatsApp's "security" see the
[WhatsApp Database Encryption Report].

## usage

Install to your local maven repository (it has not been published yet).

```
$ git clone https://github.com/jberkel/whassup.git
$ cd whassup && mvn install
```

Add a maven dependency in your main project:

```xml
<dependency>
    <groupId>com.github.jberkel.whassup</groupId>
    <artifactId>library</artifactId>
    <version>0.0.4-SNAPSHOT</version>
</dependency>
```

Use it in your app:

```java
import com.github.jberkel.whassup.Whassup;
import com.github.jberkel.whassup.model.WhatsAppMessage;

public void fetchMessages() {
    Whassup whassup = new Whassup();
    try {
        List<WhatsAppMessage> messages = whassup.getMessages();
        Log.d(TAG, "got " + messages);
    } catch (IOException e) {
        Log.e(TAG, "error getting messages", e);
    }
  }
```

Check [ExampleActivity] for a more complete example.

##<a name="license">License</a>

This application is released under the terms of the [Apache License, Version 2.0][].

[Apache License, Version 2.0]: http://www.apache.org/licenses/LICENSE-2.0.html

[WhatsApp Database Encryption Report]: https://www.os3.nl/_media/2011-2012/students/ssn_project_report.pdf
[ExampleActivity]: https://github.com/jberkel/whassup/blob/master/example/src/main/java/com/github/jberkel/whassup/ExampleActivity.java
