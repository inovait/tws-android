# 0.13.0

### Features

* [TWS-209](/browse/TWS-209) - **core**: add default variables for mustache ([c34b8036](https://github.com/inovait/tws-android-sdk/commit/c34b80361311aaceb20cba021c60be6e3a58a73b))
* **core**: set mustache from cloud props ([ddd130f7](https://github.com/inovait/tws-android-sdk/commit/ddd130f78a142648cdc20d2c38d6f065927b3143))
* **manager**: allow developer to create global manager with TWSSdk object ([9551c7f3](https://github.com/inovait/tws-android-sdk/commit/9551c7f374a0e93abaab0deff93162f6b1b6f865))

### Bug Fixes

* **core**: show error instead of raw mustache template if mustache parsing fails because of a mustache syntax error ([335266dd](https://github.com/inovait/tws-android-sdk/commit/335266ddb1960562188655ca33b28eed52c41f14))
* **core**: mustache props not used if updated after page has already been loaded ([d18b4d5e](https://github.com/inovait/tws-android-sdk/commit/d18b4d5efb24bf87edbeae3ebfcfdc0f62bdcac1))
* **core**: use only target props for mustache ([a81c06d8](https://github.com/inovait/tws-android-sdk/commit/a81c06d875a4a43fb55709786bd142bc3387b35f))
* **core**: do not ignore mustache if props are empty ([a439a725](https://github.com/inovait/tws-android-sdk/commit/a439a7257336f240976285f9042f90709c06492a))
* [TWS-235](/browse/TWS-235) - **core**: Added onRefreshListener on SwipeRefreshLayout that refreshes the webView ([2346baa7](https://github.com/inovait/tws-android-sdk/commit/2346baa784a3c302989f99534cde3d5a5f198794))
* [TWS-236](/browse/TWS-236) - **manager**: local snippets set to Snippet should be persistent for app session ([b12c8741](https://github.com/inovait/tws-android-sdk/commit/b12c8741f6e6ff51ba23fbfee09b9cb653288d37))
* **manager**: let the garbage collector collect manager if it is not used anymore ([8c7e9de5](https://github.com/inovait/tws-android-sdk/commit/8c7e9de5cd1555871aa7b2bb099863ec683ac4f6))
* **manager**: run TWSManager after it was initialized ([4239618f](https://github.com/inovait/tws-android-sdk/commit/4239618ff9f79f49d8675ceb60d94e723f0592af))
* **manager**: let cache manager handle errors instead of manager ([16d464cd](https://github.com/inovait/tws-android-sdk/commit/16d464cdca77464a8607d2c4d4b84a201292b106))
* **interstitial**: get manager only if managerTag is present ([e04deec8](https://github.com/inovait/tws-android-sdk/commit/e04deec8005b5e3fe4e07821f01b1ce10b271fa7))
* **manager**: setting local props for snippet should override previous local props for that snippet ([6bb024a0](https://github.com/inovait/tws-android-sdk/commit/6bb024a0aa3a84be1c22e189383f9ba796914602))
* **manager**: ensure that only one collector is added to socker status and network status ([1f12a4d7](https://github.com/inovait/tws-android-sdk/commit/1f12a4d7e6b6fde62b8d17f6c4b1226e902d6593))
* **manager**: ensure tag does not contain /, to avoid cache folder not found issues ([c8aa3386](https://github.com/inovait/tws-android-sdk/commit/c8aa3386e52cc4da3a4c53e8e09915159b190bfb))
* **manager**: add missing web socket log message ([ef9f9962](https://github.com/inovait/tws-android-sdk/commit/ef9f9962a0c0584625834dbe7848f86999973972))
* **manager**: remove unnecessary public method on manager ([f940ec54](https://github.com/inovait/tws-android-sdk/commit/f940ec5470c021daf188a4b20d4b5bbc0b70cadd))
* **manager**: tws socket never stops collecting network status ([6908423d](https://github.com/inovait/tws-android-sdk/commit/6908423d4749a44599db6c050a6cd5304a6efde8))
* **manager**: webSocket not cleared after closure because of failure ([570eb73e](https://github.com/inovait/tws-android-sdk/commit/570eb73efa96a4d9c77232ef43a52a68e417cdff))
* **manager**: parsing error not handled if socket message is corrupted ([cb3bccc6](https://github.com/inovait/tws-android-sdk/commit/cb3bccc670e1a739e63d8b967d88f3e7817dc79f))
* **manager**: use provided apiKey instead of hardcoded one ([5fb48c87](https://github.com/inovait/tws-android-sdk/commit/5fb48c87713d61107e1dd52477aac2252c490ac2))

# 0.12.0

### Features

* [TWS-196](/browse/TWS-196) - **manager, interstitial, data**: removed type and popup flows ([07e3478e](https://github.com/inovait/tws-android-sdk/commit/07e3478e2c84bb287a29aa8389c1569613f2414f))
* [TWS-208](/browse/TWS-208) - **core**: process mustache templates with props ([86f3ab8d](https://github.com/inovait/tws-android-sdk/commit/86f3ab8df4c1081ed704e55228535e034119e34e))

# 0.11.1

### Bug Fixes

* **data**: module not properly published ([fcad00ca](https://github.com/inovait/tws-android-sdk/commit/fcad00ca51a825e2c10dc008022b229045193312))

# 0.11.0

### Features

* **core**: added pull to refresh on snippet ([39b7e873](https://github.com/inovait/tws-android-sdk/commit/39b7e8734cd871cf2cd341d1862c2703d1ef41c2))
* **general**: add dokka plugin, to allow developers to generate documentation ([c65a6f2b](https://github.com/inovait/tws-android-sdk/commit/c65a6f2b27be7f469134095cac93a2139240cfe2))
* **core**: added support that you can still have webview without pull to refresh ([eef98974](https://github.com/inovait/tws-android-sdk/commit/eef98974ac0797c4a72a5d9fae9b6a4c3e7989e5))
* **manager**: added support to update over socket dynamicResources ([7f1014e6](https://github.com/inovait/tws-android-sdk/commit/7f1014e652c92391d6c045acb8ade8c9659168cc))
* **main**: prepare dokka tasks for generating docs and add setup google login section ([6ffde619](https://github.com/inovait/tws-android-sdk/commit/6ffde619e95c7dc4d8ff12c2b97ba54207b7cd3d))
* **core**: add documentation ([a9905028](https://github.com/inovait/tws-android-sdk/commit/a99050281d47b1c2210391aa0108233cd1e56fad))
* **manager**: refresh snippets when app comes to foreground ([1c93c6ec](https://github.com/inovait/tws-android-sdk/commit/1c93c6ecaf39cd1672c43998399146c6ac0e9528))
* **manager**: refresh snippets when app comes to foreground ([1ca49a01](https://github.com/inovait/tws-android-sdk/commit/1ca49a014c90515a40782db78ed0cbd2666a59db))

### Bug Fixes

* **manager**: do not display exception if cache is corrupted, invalidate from web ([b4ee83b2](https://github.com/inovait/tws-android-sdk/commit/b4ee83b2b26874a935db50399745bba4505b0171))
* **manager**: when websocket url expires try to reconnect with new url (call /register) ([db3b0ef8](https://github.com/inovait/tws-android-sdk/commit/db3b0ef8dfcb58846a4a3dea965792a793b593c3))
* **manager**: use cache for unseen popups also, to handle no network when opening app ([6c18e1fd](https://github.com/inovait/tws-android-sdk/commit/6c18e1fd7ceac6021b8ceb316bbf8ed36e9a07fb))
* **manager**: removed redundant targetType ([1ca230b0](https://github.com/inovait/tws-android-sdk/commit/1ca230b073e4c37ea817890d85adc7747ba109e8))
* **core**: redirect urls with relative css/js paths in html are not handled correctly, can result in external resources not loading correctly ([b4dde9e1](https://github.com/inovait/tws-android-sdk/commit/b4dde9e14ee5c0fa985e5c4691aa7da1ee173a5d))
* **core**: open new web view tab over existing ([495fa1dd](https://github.com/inovait/tws-android-sdk/commit/495fa1dd43488d4f00538713cb7627fe09271304))
* **core**: open new web view tab over existing as a fullscreen dialog ([dd2310ce](https://github.com/inovait/tws-android-sdk/commit/dd2310ce3ee448a4add1d40ef8c4b9e590964667))
* **core**: remove unsupported UI component ([95907cc9](https://github.com/inovait/tws-android-sdk/commit/95907cc936cd9531d18ed38f2a15c3cfdcd12a33))
* **core**: remove unsupported TabIcons ([4e070f7a](https://github.com/inovait/tws-android-sdk/commit/4e070f7a43a051df217ba9207f21499d661fa6cf))
* **manager**: make sure loading snippets is done with Dispatchers.IO context ([b16832e7](https://github.com/inovait/tws-android-sdk/commit/b16832e760e00ecb75a8b901ee41f043a631825b))

# 0.10.0

### Features

* **core**: display warning banner if displayed data is stale ([138600af](https://github.com/inovait/tws-android-sdk/commit/138600af6a5f525778d075bf1a65281dd39388f1))
* **core**: display dismissable banner if staled data because of stale-if-error is displayed ([f8ac3d8a](https://github.com/inovait/tws-android-sdk/commit/f8ac3d8a2080c86be3dc849af799c552b7fbe400))

### Bug Fixes

* **core**: top bar overlays web content ([f1f1ad50](https://github.com/inovait/tws-android-sdk/commit/f1f1ad5038e3c55afaeb419f55bc155769293259))
* **manager**: reconnect when websocket url expires and added 5s delay on reconnection ([096cd769](https://github.com/inovait/tws-android-sdk/commit/096cd769d3a650ff56a3cd5e08513cd8e34c8836))
* **core**: display composable error view only if request for main frame fails ([eb2fca32](https://github.com/inovait/tws-android-sdk/commit/eb2fca321405c7061f53c3ba3128f862a87056e7))
* **interstitial**: default to null manager tag, which will result in using DEFAULT_TAG ([776071b0](https://github.com/inovait/tws-android-sdk/commit/776071b09b93b60b53be2e7911df6d6ae7c80571))

# 0.9.0

### Features

* **interstitial**: customisable systeam bar ([f0de8f2a](https://github.com/inovait/tws-android-sdk/commit/f0de8f2abca176bcc0b1af63be6d147d72003932))
* **manager**: expose unseen popups and allow developer to mark a popup as seen ([233b67f5](https://github.com/inovait/tws-android-sdk/commit/233b67f5197846f3e2f33a5f789e66efb021cfad))
* **interstitial**: let the developer enable popups collecting and opening ([0c13b51b](https://github.com/inovait/tws-android-sdk/commit/0c13b51bdeaacf3a4e90387468507f93dae30e23))
* **core**: let the developer override main request in webview for better css/js injection ([e74051ea](https://github.com/inovait/tws-android-sdk/commit/e74051eab8b5f086a648f2b708d284218aa28b37))
* **core**: sync cookies between web view and okhttp3 ([8ac17d8e](https://github.com/inovait/tws-android-sdk/commit/8ac17d8ef1a50ec9b7f0236819a88987751e6c84))
* **manager**: try to reconnect to websocket if connection fails ([8944cc6e](https://github.com/inovait/tws-android-sdk/commit/8944cc6ec9150287e71ccd0677c8b80c54e3b6e6))
* **core**: add js injection with modifying response content ([00725a64](https://github.com/inovait/tws-android-sdk/commit/00725a64e5debc6386d87436e7c8ed643aab1178))
* **core**: add support for stale-if-error cache-control header ([28ec7ba5](https://github.com/inovait/tws-android-sdk/commit/28ec7ba5607890d9e26b9b62d01bb0996fa72d3e))
* **core**: expose override requests errors if any occurs ([14096eff](https://github.com/inovait/tws-android-sdk/commit/14096eff6c478ebcda8867ae71667522e2d179d2))
* **core**: if webview loaded stay saved in tab screen ([42788a4d](https://github.com/inovait/tws-android-sdk/commit/42788a4dcfcbd937557cbd07f5bbe16d6b4e5f50))
* **core**: always inject tws_injected=true to web snippet mainFrame requests ([369cb443](https://github.com/inovait/tws-android-sdk/commit/369cb4433cdc16c3908078fef5b60ddb8482122e))

### Bug Fixes

* **core**: open unspecified file type ([42d81c36](https://github.com/inovait/tws-android-sdk/commit/42d81c3616c4532084eecf0bde164a27d09d55f7))
* **manager**: replace CoroutineResourceManager with Scope in ManagerImpl ([dbe48a72](https://github.com/inovait/tws-android-sdk/commit/dbe48a720c6a7ed84c105f8f049326ba3240b775))
* **manager**: add manager tag to FileCacheManager, to separate between caches for different managers ([14fbab37](https://github.com/inovait/tws-android-sdk/commit/14fbab37ffc564a00dd790d691b4697b3ad1c721))
* **manager**: emit only distinct values ([ed4324ec](https://github.com/inovait/tws-android-sdk/commit/ed4324ecf45bd16c9ccc7f17c1bbcd7748bac3ca))
* **interstitial**: check if nullable separately ([2925831e](https://github.com/inovait/tws-android-sdk/commit/2925831e6d60aedb85b349888e10fa0a6edaaaf1))
* **manager**: get date and time from server ([0d2bd576](https://github.com/inovait/tws-android-sdk/commit/0d2bd576692cff7df4649334800915fbf3211bf2))
* **core**: do not add system padding to tab snippets ([6277dd97](https://github.com/inovait/tws-android-sdk/commit/6277dd97f5c8e2d47348ea02361794d52d62953c))
* **core**: it can happen that webview is restored before client is overriden, meaning default client is used for request ([7312e3fe](https://github.com/inovait/tws-android-sdk/commit/7312e3fee75992dbba0878bd28bc8bd1537c259c))
* **core**: keep title and icon visible when page starts loading ([76077bb5](https://github.com/inovait/tws-android-sdk/commit/76077bb584d13ff43e757bb74f51eeab9c239697))
* **manager**: use lowercase json name for enum values ([5c57dd72](https://github.com/inovait/tws-android-sdk/commit/5c57dd72bee6568a0f06a891e66cbe51a5216bb1))
* **manager**: if network connection is lost and on network reconnect get web socket connection back ([453088be](https://github.com/inovait/tws-android-sdk/commit/453088be658b70002febae8373bc8bcbaa97ff81))
* reconnecting websocket on lifecycle and on network reconnect ([ef83d57e](https://github.com/inovait/tws-android-sdk/commit/ef83d57e3f70a464683d97003a4e56657d63b8de))
* **core**: stop video playing when switching tabs ([587c0d20](https://github.com/inovait/tws-android-sdk/commit/587c0d2002d152cd73a9ae08f98961bc3e25f2b2))
* **core**: added animation on changing tabs ([40b8ac4e](https://github.com/inovait/tws-android-sdk/commit/40b8ac4e148b6c827e1a1a36515ff9492a533fd6))

# 0.8.0

### Features

* **manager**: tws manager should emit cached snippets before loading from web to reduce loading time ([884813bb](https://github.com/inovait/tws-android-sdk/commit/884813bbd9ab329c7e141380b9d4661080be13fb))
* **core**: customizable bottom tab content ([fce2c4cf](https://github.com/inovait/tws-android-sdk/commit/fce2c4cf9dbe4ad1cb5d238a4d6bcce29cad716a))

### Bug Fixes

* **manager**: launch saving to cache in coroutine, to avoid blocking other actions ([6a013c5e](https://github.com/inovait/tws-android-sdk/commit/6a013c5e040b2ded1412205adafc3807501a6fb5))
* **manager**: local handler should be relaunched every time snippets are changed ([b7ab3bed](https://github.com/inovait/tws-android-sdk/commit/b7ab3bed9035967cb86a4eb3461672e16b9d4843))

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
