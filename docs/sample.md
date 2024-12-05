<div style="text-align: center;width: 100%;">
    <h1>TWS Sample application</h1>
    <img src="images/appIcon.png" alt="My Custom Icon" style="display: block; margin: 32px auto; max-width: 100%; height: auto;" />
</div>

## Sample application

For a simpler and clearer understanding of the TWS SDK, we have prepared a sample application to help you get started.
It includes several use case examples and implementations that demonstrate how to integrate TWSView with your Android application.

You can download the sample application for free on our web page <a href="https://thewebsnippet.dev/">TheWebSnippet</a>.
The downloaded bundle provides access to a temporary project, allowing you to explore how the snippet
dashboard interacts with your application.

In your Snippets Dashboard, you’ll find two code editors: one for your snippet’s __properties__ and another for its
__HTML, JavaScript, and CSS.__

#### Snippet properties

Snippet properties are set in __Props editor__ within your Snippets Dashboard. Properties should be provided in JSON format.
They are accessible natively and can be used for various use cases.

In our sample app we use them to implement custom tab names and icons, by setting "tabName" and "tabIcon" properties.
These properties determine the name and icon of the respective tab, as demonstrated in the TWSViewCustomPropsExample.

Throughout the app, we also use property "page" to group related snippets together.
You can find this implementation in each example's viewModel.

Snippet properties can also be added locally. For instance, if you want to display user-specific information or data fetched from
an external source,
you can add it to the local snippet properties using TWSManager. See an example of this in the TWSViewMustacheExample.

We also provide you with some of the system properties that can be used:

- version
- device.vendor
- device.name
- os.version

#### Mustache processing

You can enable Mustache processing for your snippets with a {{ Mustache }} button in your Snippets Dashboard.
Set the properties in Props editor and use them in your HTML with Mustache templates {{}}.

Snippet properties:

```json
{
  "name": "John",
  "surname": "Smith",
  "address": {
    "houseNum": "72",
    "streetName": "High Street",
    "city": "London"
  }
}
```

HTML:

```html
<h1>User Information</h1>
<p>full name: {{name}} {{surname}}</p>
<p>full address: {{address.streetName}} {{address.houseNum}}, {{address.city}}</p>

```

When the snippets are loaded in to the TWSView, these templates will get replaced by the actual values.
Changes are reflected in real time. You can edit properties in your Snippets Dashboard and the values will get changed,
without the need to rebuild the entire application.

End result:

```html
<h1>User Information</h1>
<p>full name: John Smith</p>
<p>full address: High Street 72, London</p>
```

#### URL Intercepts

By passing a custom TWSViewInterceptor implementation to the TWSView, you can handle URL interceptions natively. In our sample
application we use URL interception for navigation, we intercept redirects on the home screen and render the corresponding native
screens. You can see our implementation in TWSViewCustomInterceptorExample.

#### Permissions

Web permission are handled natively within TWSView. All you need to do is include the required permissions in AndroidManifest,
if your web page uses any of the following features:

- for camera permissions, include: `<uses-permission android:name="android.permission.CAMERA"/>` and
  `<uses-feature android:name="android.hardware.camera" android:required="false" />`,
- and for accessing the users location, include: `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />`

See the "permissions" Snippets Dashboard for the JavaScript implementation download/upload, camera and location access.

When any of the permissions is triggered, a native "permissions required" modal will be shown to the user, and the permission
settings will be remembered by the application.

#### CSS and JavaScript injection

If you are creating a custom web page for your mobile application, you may want to add styling and functionality to it.
You can achieve this by injecting CSS or JavaScript into the snippet via your Snippets Dashboard.
The injection process is handled internally, so you don’t need to worry about anything except uploading your CSS and JavaScript
files.