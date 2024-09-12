# 0.7.1

### Bug Fixes

* [TWS-141](/browse/TWS-141) - **manager**: it can happen, that same snippet is added twice from web socket ([57349d03](https://github.com/inovait/tws-android-sdk/commit/57349d03c3173c8f6dad55855d0306284a2acd5e))
* **interstitial**: developer can not open multiple popups ([4ec1df64](https://github.com/inovait/tws-android-sdk/commit/4ec1df649bb29857147bd1337e50efc353f51157))
* **interstitial**: disable window preview for interstitial ad ([d8d5d641](https://github.com/inovait/tws-android-sdk/commit/d8d5d641480900411e448e06b709a2ff4409e05f))

# 0.7.0

### Features

* **core**: use interstitial ad popup with activity ([bc0e7d37](https://github.com/inovait/tws-android-sdk/commit/bc0e7d3772a9954f0812fc69c80dda3989d5b875))
* **core, manager**: add support for triggering popup snippet ([4b015079](https://github.com/inovait/tws-android-sdk/commit/4b015079bcbecb5212636838ca388cea49ebc65e))
* prepare LocalSnippetHandler, which will remove snippets locally according to its visiblity ([3ef663e8](https://github.com/inovait/tws-android-sdk/commit/3ef663e88b23e9058befc817a460da10d4ce4d77))
* integrate LocalSnippetHandler with WebSnippetManager, to remove hidden snippets locally ([be80b8c0](https://github.com/inovait/tws-android-sdk/commit/be80b8c0326cdb42885b9fea12095e741ad921a1))
* prepare tws_injected interface for all webviews ([b20e73f4](https://github.com/inovait/tws-android-sdk/commit/b20e73f40c28010a1b99c118d90c356fcc445182))
* append TheWebSnippet to user agent ([14c1f776](https://github.com/inovait/tws-android-sdk/commit/14c1f77671ad9122b3a26645293fed418f5f87bf))
* add support for type in web snippet action ([6638630a](https://github.com/inovait/tws-android-sdk/commit/6638630a8c504ebca91ce91673d5427f598d80f7))
* **interstitial**: open interstitial with manager ([0266f8c4](https://github.com/inovait/tws-android-sdk/commit/0266f8c4ca7f9473a28d8ed8c8e0203ef9206c21))
* use default tag for getting manager instance ([b8796157](https://github.com/inovait/tws-android-sdk/commit/b87961575f4b208f5211ed63b1f23c327c2a4b83))
* allow developer to get shared instance of WebSnippetManagerImpl ([4bf4affd](https://github.com/inovait/tws-android-sdk/commit/4bf4affda2f0ebddf3daa3e1c8e980a033236483))
* add support for type to web socket action ([af58ef17](https://github.com/inovait/tws-android-sdk/commit/af58ef17355248ac0d52ef572955f350e20b5018))

### Bug Fixes

* **core**: on app startup fixed show animation ([623c7f80](https://github.com/inovait/tws-android-sdk/commit/623c7f808be14b29d7182e01c8502f60a0e74e98))
* handle opening Interstitial with data only ([d0c68b9e](https://github.com/inovait/tws-android-sdk/commit/d0c68b9e304b836262e05555afe1da4e2e2b9a06))
* remove activity from core module manifest ([0df5f28f](https://github.com/inovait/tws-android-sdk/commit/0df5f28f437130799df54955fd5d957eb7abc3c9))
* typo for popup ([ee77c9a3](https://github.com/inovait/tws-android-sdk/commit/ee77c9a37736fae723ae9d2cf2c390a38b02210d))
* temporary remove type parsing from web socket ([034a8233](https://github.com/inovait/tws-android-sdk/commit/034a82337a97aba777d3ceebd49f1f0ac6c597cd))
* **core**: stop video play when new activity is opened ([53c98773](https://github.com/inovait/tws-android-sdk/commit/53c9877361823fb5387f2b659daddd51fe7523ea))
* snippets visibility should be updated from websockets and local hider should be relaunched ([9a0f8dda](https://github.com/inovait/tws-android-sdk/commit/9a0f8ddab3d647bafeb9f1e770920a8297eb6204))
* parsing failure for snippet status, visibility and snippet type ([aa931df1](https://github.com/inovait/tws-android-sdk/commit/aa931df1d75159d86a741de2de48ce062bcb04bd))
* **github**: uncomment ([76da4af5](https://github.com/inovait/tws-android-sdk/commit/76da4af5fe6a09884219af3fee66262509adb853))
* **github**: syntax error in commits ([7a4129aa](https://github.com/inovait/tws-android-sdk/commit/7a4129aa6aa9d50577f4ee46a7b79f48aa3cb7dc))

# 0.6.0

### Features

* allow user to download file from web snippet ([1eba2203](https://github.com/inovait/tws-android-sdk/commit/1eba220318f1d12d49ccd298334a29b2e44de4df))
* **core**: added notification request permission for latest mobile devices when download blob url ([fbfcd800](https://github.com/inovait/tws-android-sdk/commit/fbfcd800dc27f3debf773bf5d92fd2cdb8089f58))
* add support for opening file picker ([2623b0db](https://github.com/inovait/tws-android-sdk/commit/2623b0db7f4656b5fa7cb6a2deb00da62bc6a043))

### Bug Fixes

* [TWS-119](/browse/TWS-119) - **core**: occasional crash when opening location permission request ([a37be5f2](https://github.com/inovait/tws-android-sdk/commit/a37be5f2553db715251013043c1c5ef38c4c7a5c))
* enable to open downloaded image over notification ([0795a4c6](https://github.com/inovait/tws-android-sdk/commit/0795a4c60d3a5f18ac6af85bfcb4ce6bc21fcd61))
* **core**: do not recreate webview on loading/error state change ([2bccb9e2](https://github.com/inovait/tws-android-sdk/commit/2bccb9e2d620bb4be3ec7acdbd48a6319759d376))
* notification isGranted permission parameter is not send as boolean to javascript ([51f4d4ae](https://github.com/inovait/tws-android-sdk/commit/51f4d4aeed8bbde84aafff63ecbdd16a02bfcc83))
* released webview should not be reused, save its state and reuse it instead ([e2643282](https://github.com/inovait/tws-android-sdk/commit/e264328236ba0ff08e6d38243ebd05c4254dc8ce))
* remove redundant check, which should always be null ([8586f206](https://github.com/inovait/tws-android-sdk/commit/8586f206cd25084a145963c6050bcfa27838ff61))
* proper handling of webview recreation ([96ece4df](https://github.com/inovait/tws-android-sdk/commit/96ece4df69063ec7035cb86f3899ea6d27d9edd9))

* Update README.MD - core ([9b98d6f6](https://github.com/inovait/tws-android-sdk/commit/9b98d6f6117bccd6cbb4597a95f9da89fd427049))
* Update README.MD - app ([9cc97741](https://github.com/inovait/tws-android-sdk/commit/9cc977411f3b8f8c3698784ab70dd6628746ff31))
* Create README.MD ([8eb7e761](https://github.com/inovait/tws-android-sdk/commit/8eb7e7616c007a2ee391f22059e354ce58319991))

# 0.5.0

### Features

* **core, manager**: added support to inject css or javascript into webpage ([0089f596](https://github.com/inovait/tws-android-sdk/commit/0089f596c76d8d7da51f14e19650b9b3ca0698fd))
* **core**: ask permission for video capture and location ([47aa3950](https://github.com/inovait/tws-android-sdk/commit/47aa3950182d2a8db04e067802b031afffaea653))
* **core**: deny webviews permission if user denies native apps permission ([5304c5e3](https://github.com/inovait/tws-android-sdk/commit/5304c5e33bb16cb8ccf7973822a5f3a942fecb5c))
* **manager**: parse dynamic resources ([797fe280](https://github.com/inovait/tws-android-sdk/commit/797fe280b5a80de96225b35b390a9015c1c14194))
* **core**: parse dynamicResource type from contentType ([a45dacc4](https://github.com/inovait/tws-android-sdk/commit/a45dacc417135a4588af3ab0ce1f68fc744f9429))
* **core**: request permissions only if present in manifest ([0aea7a58](https://github.com/inovait/tws-android-sdk/commit/0aea7a58c0b6d5a6bd57fe3785b093fa55f8d376))

### Bug Fixes

* **core**: use proper type for javascript ([ad2bf73d](https://github.com/inovait/tws-android-sdk/commit/ad2bf73d2072e8c6f6fe2a6b0479dcd7aca98019))
* **core**: permissions should not be included in sdk manifest, developers should include it only if needed ([d1ad0d9f](https://github.com/inovait/tws-android-sdk/commit/d1ad0d9f0751d0e2c0f8b65111b9caf449924e4f))

# 0.4.1

### Bug Fixes

* **manager**: added html field to WebSnippetDto ([217415dc](https://github.com/inovait/tws-android-sdk/commit/217415dce9f994a746f05c6da5266f410aa6594d))
* **manager**: update html on websocket update ([c1ea13a7](https://github.com/inovait/tws-android-sdk/commit/c1ea13a74925947b079bd219e9af5889e4af510f))
* **manager**: socket body when updated organizationId and projectId can be nullable ([877e9ebf](https://github.com/inovait/tws-android-sdk/commit/877e9ebf33c8f22e4aad92af949dfca6b6fe34b8))

# 0.4.0

### Features

* **manager**: added cloud connection manager ([8302a652](https://github.com/inovait/tws-android-sdk/commit/8302a65217138c6a3ae566a301ffbf1d3d51e0e9))
* **manager**: added support for shared snippet ([c03d6929](https://github.com/inovait/tws-android-sdk/commit/c03d6929ce1e980c6f8e242d711bf53b6535bcc9))

### Bug Fixes

* **core**: snippet component is not updated when snippets target url is changed but id stays the same ([bc1670b9](https://github.com/inovait/tws-android-sdk/commit/bc1670b940faf0cf48f002c67c24aed50e0aff0d))
* **core**: userAgent should not overridden, to comply with googles policy ([c69d4654](https://github.com/inovait/tws-android-sdk/commit/c69d4654a294ac99b5f4f64befd03458676900ae))
* **manager**: generate jsonAdapter ([69c58729](https://github.com/inovait/tws-android-sdk/commit/69c58729c41a7611573c423cc3e197104cbd8fdd))

# 0.3.3

### Bug Fixes

* **core**: keep on release build web snippet data ([4d6f6420](https://github.com/inovait/tws-android-sdk/commit/4d6f6420f50cb2edcf11350c2d06ca6cc633c060))
* **core**: keep SnippetUpdateAction not WebSnippetData ([335c926b](https://github.com/inovait/tws-android-sdk/commit/335c926b9f44481c6f1510c2be307744824d532b))

# 0.3.2

### Bug Fixes

* **core**: only open new intent for google login ([704c9e0c](https://github.com/inovait/tws-android-sdk/commit/704c9e0c80a381c328743aaaaf59f7d7c69b4b2c))

# 0.3.1

### Bug Fixes

* **web-socket**: update target url and header over socket update ([78237ff2](https://github.com/inovait/tws-android-sdk/commit/78237ff29c1c6a2e2644ec0b7cdd75c5248ba9fb))

# 0.3.0

### Features

* **web-socket**: added web socket support ([cef4d53f](https://github.com/inovait/tws-android-sdk/commit/cef4d53ff5021c8953ed9039c0d38135e32062d0))

### Bug Fixes

* **core**: on WebSnippetComponent LocalScreenResetNotifier does not exists for resetting screen ([ede13d4f](https://github.com/inovait/tws-android-sdk/commit/ede13d4ff0595c99fe92876bd4f1ba8d49ededa2))
* **core-ui**: popups also allows to intercept override url ([b37e9d1c](https://github.com/inovait/tws-android-sdk/commit/b37e9d1cc1563164803f689a36a8f51159e19290))
* **core-ui**: chrome tabs result should be loaded only in active web view ([a992639c](https://github.com/inovait/tws-android-sdk/commit/a992639c8415ad06b48a7aadd614059ae6c4c79d))

# 0.2.0

### Features

* **web-socket**: added web socket support ([cef4d53f](https://github.com/inovait/tws-android-sdk/commit/cef4d53ff5021c8953ed9039c0d38135e32062d0))

# 0.1.3

### Bug Fixes

* **core**: set screen reset to public ([301fc5be](https://github.com/inovait/tws-android-sdk/commit/301fc5be7b0033e29a64a200accef16726a06e5e))
* **core**: ComponentActivity can be nullable in compose preview ([12a636d6](https://github.com/inovait/tws-android-sdk/commit/12a636d613cf5bc7565be7978065f2856a1cf51c))

# 0.1.2

### Bug Fixes

* **core**: test for release ([ad6d4295](https://github.com/inovait/tws-android-sdk/commit/ad6d4295dbb93be39be352d9fc3cb12f0c07d6a2))
