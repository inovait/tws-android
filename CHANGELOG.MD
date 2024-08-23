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
