# 1.0.0 (2023-04-16)


### Bug Fixes

* add Currency value object ([552d2fb](https://github.com/pervasive-cats/toys-store-stores/commit/552d2fbd5bb6cdc4673fa3b55d1a197c361a59f7))
* add equality by id in store entity ([b502980](https://github.com/pervasive-cats/toys-store-stores/commit/b5029804c3ff66ecb4a936ae5206310553135ffc))
* add missing add and remove shelving ditto commands with tests, fix ditto actor tests with real item state handlers ([b1356de](https://github.com/pervasive-cats/toys-store-stores/commit/b1356de355bcd11e86c12bff77f7e9ee37d67ba5))
* change action and event in anti theft system ThingModel ([a840446](https://github.com/pervasive-cats/toys-store-stores/commit/a8404462b1fb2dd0a80dceb89e7339824e68adfe))
* change ItemDetected event signature ([8c2e1eb](https://github.com/pervasive-cats/toys-store-stores/commit/8c2e1eb2d9bbda9417214e193b12ff8e0e120239))
* drop system action test ([75e9ce9](https://github.com/pervasive-cats/toys-store-stores/commit/75e9ce91a80ba30fbeb94bb04c65b5bb0e49fe51))
* fix implementation of broker actor ([07e8396](https://github.com/pervasive-cats/toys-store-stores/commit/07e8396f1735d98e1c08d94497990472bfc11547))
* fix minor issues with generic types and postfix ops ([5f5ee02](https://github.com/pervasive-cats/toys-store-stores/commit/5f5ee021c506b9bf75d77ef1a1acb138ffaa5376))
* improved message broker actor ([6b87015](https://github.com/pervasive-cats/toys-store-stores/commit/6b870150f94eb14485b206d0512b758a7652efee))
* modified the type from list to seq in the itemsRow parameter in Shelf ([dd9af61](https://github.com/pervasive-cats/toys-store-stores/commit/dd9af61036a4e4ddc2740e56a6020d2afc49c00a))
* modified the type from list to seq of a parameter in shelving, shelving group and store ([e9b7a22](https://github.com/pervasive-cats/toys-store-stores/commit/e9b7a22d40d2cf34e3007b06f97b3fe2d21e3f2f))
* remove excessive curly brackets from thing id in Ditto actor ([71313bd](https://github.com/pervasive-cats/toys-store-stores/commit/71313bd93f633de76c9e90e1bfb479352ac2e70a))
* remove useless events, services, routes, commands and related tests ([adf1f38](https://github.com/pervasive-cats/toys-store-stores/commit/adf1f383854a8a087220c34dc1087675d61d3967))
* run ditto actor long-running behavior code in separate executor ([b8ab3f6](https://github.com/pervasive-cats/toys-store-stores/commit/b8ab3f605ad0c100e3d975a9756ac40eff5271e4))
* setup repository for testing with containers, fix tests ([a5319b9](https://github.com/pervasive-cats/toys-store-stores/commit/a5319b9e50150f9431d951fa79d84e053be82187))
* thingModel config ([4ba2f65](https://github.com/pervasive-cats/toys-store-stores/commit/4ba2f652834f802cb646dc68d1fb07d2fc3e392d))
* ThingModel url ([232ab83](https://github.com/pervasive-cats/toys-store-stores/commit/232ab83779d80897899e4421a3c2bb44b5248d55))
* update sql creation script ([94f7fb5](https://github.com/pervasive-cats/toys-store-stores/commit/94f7fb5a0ad4f113fbd2e9d6d98125c297091925))
* update updateShelvingGroup method in Store ([6df7815](https://github.com/pervasive-cats/toys-store-stores/commit/6df7815d0b1e82fd347e5a0558b9f4abdb5b60a2))
* updated catalog item put in place test ([8dcc149](https://github.com/pervasive-cats/toys-store-stores/commit/8dcc14914ba5eb9aacf1765a34099989ca576eca))


### Features

* add antiTheftSystem ThingModel ([913c18c](https://github.com/pervasive-cats/toys-store-stores/commit/913c18ca86c66209ef1f78b139765203c32ecaa7))
* add AntiTheftSystemAlarmTriggered implementation and test ([afc4018](https://github.com/pervasive-cats/toys-store-stores/commit/afc4018d585141f096ba3e1b89a41f93f6d979f6))
* add AnyOps ([7c72037](https://github.com/pervasive-cats/toys-store-stores/commit/7c7203780cabba63ed83fb86f3abd03fe95c8844))
* add base implementation for DittoActor ([c9a124c](https://github.com/pervasive-cats/toys-store-stores/commit/c9a124c824764cfa5db0c989c25527588588b5f3))
* add catalog item and test ([419d9e2](https://github.com/pervasive-cats/toys-store-stores/commit/419d9e2b75d47ee1b7f8c1cdb139cdf4c8013012))
* add catalog item lifted implementation and test ([37b38d8](https://github.com/pervasive-cats/toys-store-stores/commit/37b38d823ab98ad64d847494583ff8c2addd7039))
* add Catalog Item Lifting Registered implementation and test ([c1c6dd3](https://github.com/pervasive-cats/toys-store-stores/commit/c1c6dd31d244ccbf791f03ffade7953e1f574d86))
* add catalog item put in place implementation and test ([a047159](https://github.com/pervasive-cats/toys-store-stores/commit/a0471594b7e46b36ae5bb428f4a234097cb1dafb))
* add count test ([77f999d](https://github.com/pervasive-cats/toys-store-stores/commit/77f999d2fb6b7efdadd6c612dec27a8501d30097))
* add domain events in serializer ([048558d](https://github.com/pervasive-cats/toys-store-stores/commit/048558d5170efd537b058a3ad407b50a4cc12c9a))
* add domain events in serializer ([84a1984](https://github.com/pervasive-cats/toys-store-stores/commit/84a19844e7046b07a0eccfd6b9cde07ba5708ceb))
* add drop system showItemData ([39775f0](https://github.com/pervasive-cats/toys-store-stores/commit/39775f07ac8ade96c0d0f20cf939b3258855c15b))
* add drop system thing model ([742b407](https://github.com/pervasive-cats/toys-store-stores/commit/742b407c963f1128c4a6e3dfb29e2eca8866a0c6))
* add dropSystem event handling ([d4f2f66](https://github.com/pervasive-cats/toys-store-stores/commit/d4f2f666379eb8eb7eb866e4d62676a254c111d1))
* add entities interfaces for store aggregate ([e87257d](https://github.com/pervasive-cats/toys-store-stores/commit/e87257db1e2299bf76f94827e366cda730a81ecd))
* add events interfaces for store aggregate ([85a7fbc](https://github.com/pervasive-cats/toys-store-stores/commit/85a7fbc55a3ce379d7bdb3e870cc79e0c67bf2cb))
* add item deleted implementation and test ([776b30d](https://github.com/pervasive-cats/toys-store-stores/commit/776b30dc5bbb9dc9984c42bc72b79db04bae8cb6))
* add item implementation and test ([3186718](https://github.com/pervasive-cats/toys-store-stores/commit/3186718aafcb0b5293caeeba27d3f3fb86853541))
* add item inserted in drop system implementation and test ([21231d5](https://github.com/pervasive-cats/toys-store-stores/commit/21231d5b1c0dd58b78a4efed532215fc60f00bb9))
* add item put in place implementation and test ([0e58d65](https://github.com/pervasive-cats/toys-store-stores/commit/0e58d6518940de9648870a82ed23aec01b4c8e15))
* add item returned implementation and test ([e6e88da](https://github.com/pervasive-cats/toys-store-stores/commit/e6e88daaac6500cf58727903cf7f1b36be4dd728))
* add item state handlers implmentation ([67c3bc9](https://github.com/pervasive-cats/toys-store-stores/commit/67c3bc908fa322893b5a9fe64760b258353fa66f))
* add itemid implementation and test ([02de4dc](https://github.com/pervasive-cats/toys-store-stores/commit/02de4dcad875608021b59039135a0b9a23050366))
* add items row id implementation and test ([e6e9cb6](https://github.com/pervasive-cats/toys-store-stores/commit/e6e9cb6920d5ebf09ea08af4854b6e6c8d417949))
* add items row implementation and test ([725da35](https://github.com/pervasive-cats/toys-store-stores/commit/725da35514be40c24863755338e217b8d358861c))
* add items row test ([9bbf45b](https://github.com/pervasive-cats/toys-store-stores/commit/9bbf45bd7fdd08d9fbebe3e79dc0836086975001))
* add itemsRow and count  implementation ([1caaf98](https://github.com/pervasive-cats/toys-store-stores/commit/1caaf98c68bfe68310751e5354258953fd5e732e))
* add itemsrow update extension method and test ([de375fe](https://github.com/pervasive-cats/toys-store-stores/commit/de375fe1e41956dce5437b34ec830d4b146dbf6e))
* add message broker actor and command ([e58d2ff](https://github.com/pervasive-cats/toys-store-stores/commit/e58d2ff9656a99c7ebc38e824d1bbb098ce95093))
* add message broker test implementation ([9675b2d](https://github.com/pervasive-cats/toys-store-stores/commit/9675b2d98eeee3de4794bb1ee872928489e84a21))
* add mock implementation for handlers of item state changes ([e5b9093](https://github.com/pervasive-cats/toys-store-stores/commit/e5b9093295944bdc0ff3215aa857cab8107936e4))
* add quill dependency ([8bbf26c](https://github.com/pervasive-cats/toys-store-stores/commit/8bbf26c3f1eed039bbd75257798cb7ba75ee6e5f))
* add remove to repository and test ([03ce9a2](https://github.com/pervasive-cats/toys-store-stores/commit/03ce9a25217b13a981777d818c0cd0b2d4707404))
* add repository ([ca28689](https://github.com/pervasive-cats/toys-store-stores/commit/ca2868976b378d2fa89b35841ef7e2ed9443382a))
* add repository and test ([72550f9](https://github.com/pervasive-cats/toys-store-stores/commit/72550f92bb84039d8d0eb183f5fb686018a701a0))
* add repository to shelf event handler ([42f396e](https://github.com/pervasive-cats/toys-store-stores/commit/42f396e0181253928e1ebf1914d244ce680c2324))
* add serializers for value objects ([03030ba](https://github.com/pervasive-cats/toys-store-stores/commit/03030ba4aff981d99d0b4efd653ca9e1e8349ab2))
* add services interfaces for store aggregate ([75880bf](https://github.com/pervasive-cats/toys-store-stores/commit/75880bf38d579bfd88df5935e7bc8d0f0df970cb))
* add shelf id implementation and test ([1dfda3a](https://github.com/pervasive-cats/toys-store-stores/commit/1dfda3a8b6e88744bd68aa1e1db39358fbdd29ae))
* add shelf implementation and test ([d992c62](https://github.com/pervasive-cats/toys-store-stores/commit/d992c62632c829aae6b354ebebaccc0f6b8a42ec))
* add shelf ops implementation and test ([e86b2de](https://github.com/pervasive-cats/toys-store-stores/commit/e86b2de9908a7f9e7f2bb07a4494ce34096d137f))
* add shelving group id implementation and test ([09bbe95](https://github.com/pervasive-cats/toys-store-stores/commit/09bbe95e53548bb3e14ef55375141f5e455dad65))
* add shelving group implementation and test ([94ecdf0](https://github.com/pervasive-cats/toys-store-stores/commit/94ecdf026375ddf23dccf2ca0cf083b33cf78ab5))
* add shelving group ops implementation and test ([8e28a91](https://github.com/pervasive-cats/toys-store-stores/commit/8e28a91eba23255fe080c97b828a10bcc9700426))
* add shelving id implementation and test ([7aad065](https://github.com/pervasive-cats/toys-store-stores/commit/7aad065b55fd1dd04a7b6393a0fba927bc6128ca))
* add shelving implementation and test ([8a27c64](https://github.com/pervasive-cats/toys-store-stores/commit/8a27c641b580f13faa9e618b4d82bd692553b689))
* add shelving ops implementation and test ([0a026b6](https://github.com/pervasive-cats/toys-store-stores/commit/0a026b6ad1b1469007e7800b80af824162c5f676))
* add shelving Thing action triggers to ditto actor ([9b4a239](https://github.com/pervasive-cats/toys-store-stores/commit/9b4a23983406585b01a47432ccfe44e49cd1fd13))
* add shelving Thing event to ditto actor ([0e12b65](https://github.com/pervasive-cats/toys-store-stores/commit/0e12b652b4b8036a1ebe3fafd9b70125ff05c551))
* add shelving thing model to ditto actor ([7774f05](https://github.com/pervasive-cats/toys-store-stores/commit/7774f0593b073e816edea35c10b983c578350a7c))
* add shelving ThingModel ([67baedb](https://github.com/pervasive-cats/toys-store-stores/commit/67baedbf368c56dedeb0ff2fcca032ebe58077d1))
* add sql table for stores ([6658eb7](https://github.com/pervasive-cats/toys-store-stores/commit/6658eb76e2f40c0bc3e1d2cbce1a53594792416f))
* add store implementation ([dd56466](https://github.com/pervasive-cats/toys-store-stores/commit/dd564669437074a5b66ccf873596c9d0cbbaf73e))
* add store test implementation ([850f1bf](https://github.com/pervasive-cats/toys-store-stores/commit/850f1bf1b0840cc0567529c3054f43595cb661dd))
* add valueobjects interfaces for store aggregate ([e343894](https://github.com/pervasive-cats/toys-store-stores/commit/e34389451134fc247d404f652a372a5bb6713ea4))
* improve repository test ([3475d4c](https://github.com/pervasive-cats/toys-store-stores/commit/3475d4c74c25153faa51d9856c026363f8af4643))
* improved message actor broker test ([9355928](https://github.com/pervasive-cats/toys-store-stores/commit/9355928a31c917bf456f1f698822df8a24d14290))
* update gitignore to ignore file with .semanticdb extension ([314311f](https://github.com/pervasive-cats/toys-store-stores/commit/314311f7df043622f57c98ed19ad03b25656d6df))

# [1.0.0-beta.5](https://github.com/pervasive-cats/toys-store-stores/compare/v1.0.0-beta.4...v1.0.0-beta.5) (2023-04-11)


### Bug Fixes

* add Currency value object ([552d2fb](https://github.com/pervasive-cats/toys-store-stores/commit/552d2fbd5bb6cdc4673fa3b55d1a197c361a59f7))
* add missing add and remove shelving ditto commands with tests, fix ditto actor tests with real item state handlers ([b1356de](https://github.com/pervasive-cats/toys-store-stores/commit/b1356de355bcd11e86c12bff77f7e9ee37d67ba5))
* change action and event in anti theft system ThingModel ([a840446](https://github.com/pervasive-cats/toys-store-stores/commit/a8404462b1fb2dd0a80dceb89e7339824e68adfe))
* change ItemDetected event signature ([8c2e1eb](https://github.com/pervasive-cats/toys-store-stores/commit/8c2e1eb2d9bbda9417214e193b12ff8e0e120239))
* drop system action test ([75e9ce9](https://github.com/pervasive-cats/toys-store-stores/commit/75e9ce91a80ba30fbeb94bb04c65b5bb0e49fe51))
* remove excessive curly brackets from thing id in Ditto actor ([71313bd](https://github.com/pervasive-cats/toys-store-stores/commit/71313bd93f633de76c9e90e1bfb479352ac2e70a))
* remove useless events, services, routes, commands and related tests ([adf1f38](https://github.com/pervasive-cats/toys-store-stores/commit/adf1f383854a8a087220c34dc1087675d61d3967))
* run ditto actor long-running behavior code in separate executor ([b8ab3f6](https://github.com/pervasive-cats/toys-store-stores/commit/b8ab3f605ad0c100e3d975a9756ac40eff5271e4))
* thingModel config ([4ba2f65](https://github.com/pervasive-cats/toys-store-stores/commit/4ba2f652834f802cb646dc68d1fb07d2fc3e392d))
* ThingModel url ([232ab83](https://github.com/pervasive-cats/toys-store-stores/commit/232ab83779d80897899e4421a3c2bb44b5248d55))


### Features

* add antiTheftSystem ThingModel ([913c18c](https://github.com/pervasive-cats/toys-store-stores/commit/913c18ca86c66209ef1f78b139765203c32ecaa7))
* add base implementation for DittoActor ([c9a124c](https://github.com/pervasive-cats/toys-store-stores/commit/c9a124c824764cfa5db0c989c25527588588b5f3))
* add drop system showItemData ([39775f0](https://github.com/pervasive-cats/toys-store-stores/commit/39775f07ac8ade96c0d0f20cf939b3258855c15b))
* add drop system thing model ([742b407](https://github.com/pervasive-cats/toys-store-stores/commit/742b407c963f1128c4a6e3dfb29e2eca8866a0c6))
* add dropSystem event handling ([d4f2f66](https://github.com/pervasive-cats/toys-store-stores/commit/d4f2f666379eb8eb7eb866e4d62676a254c111d1))
* add item state handlers implmentation ([67c3bc9](https://github.com/pervasive-cats/toys-store-stores/commit/67c3bc908fa322893b5a9fe64760b258353fa66f))
* add mock implementation for handlers of item state changes ([e5b9093](https://github.com/pervasive-cats/toys-store-stores/commit/e5b9093295944bdc0ff3215aa857cab8107936e4))
* add repository to shelf event handler ([42f396e](https://github.com/pervasive-cats/toys-store-stores/commit/42f396e0181253928e1ebf1914d244ce680c2324))
* add shelving Thing action triggers to ditto actor ([9b4a239](https://github.com/pervasive-cats/toys-store-stores/commit/9b4a23983406585b01a47432ccfe44e49cd1fd13))
* add shelving Thing event to ditto actor ([0e12b65](https://github.com/pervasive-cats/toys-store-stores/commit/0e12b652b4b8036a1ebe3fafd9b70125ff05c551))
* add shelving thing model to ditto actor ([7774f05](https://github.com/pervasive-cats/toys-store-stores/commit/7774f0593b073e816edea35c10b983c578350a7c))
* add shelving ThingModel ([67baedb](https://github.com/pervasive-cats/toys-store-stores/commit/67baedbf368c56dedeb0ff2fcca032ebe58077d1))

# [1.0.0-beta.4](https://github.com/pervasive-cats/toys-store-stores/compare/v1.0.0-beta.3...v1.0.0-beta.4) (2023-03-28)


### Bug Fixes

* add equality by id in store entity ([b502980](https://github.com/pervasive-cats/toys-store-stores/commit/b5029804c3ff66ecb4a936ae5206310553135ffc))
* fix minor issues with generic types and postfix ops ([5f5ee02](https://github.com/pervasive-cats/toys-store-stores/commit/5f5ee021c506b9bf75d77ef1a1acb138ffaa5376))
* modified the type from list to seq in the itemsRow parameter in Shelf ([dd9af61](https://github.com/pervasive-cats/toys-store-stores/commit/dd9af61036a4e4ddc2740e56a6020d2afc49c00a))
* modified the type from list to seq of a parameter in shelving, shelving group and store ([e9b7a22](https://github.com/pervasive-cats/toys-store-stores/commit/e9b7a22d40d2cf34e3007b06f97b3fe2d21e3f2f))
* setup repository for testing with containers, fix tests ([a5319b9](https://github.com/pervasive-cats/toys-store-stores/commit/a5319b9e50150f9431d951fa79d84e053be82187))
* update sql creation script ([94f7fb5](https://github.com/pervasive-cats/toys-store-stores/commit/94f7fb5a0ad4f113fbd2e9d6d98125c297091925))
* update updateShelvingGroup method in Store ([6df7815](https://github.com/pervasive-cats/toys-store-stores/commit/6df7815d0b1e82fd347e5a0558b9f4abdb5b60a2))


### Features

* add AnyOps ([7c72037](https://github.com/pervasive-cats/toys-store-stores/commit/7c7203780cabba63ed83fb86f3abd03fe95c8844))
* add items row implementation and test ([725da35](https://github.com/pervasive-cats/toys-store-stores/commit/725da35514be40c24863755338e217b8d358861c))
* add itemsrow update extension method and test ([de375fe](https://github.com/pervasive-cats/toys-store-stores/commit/de375fe1e41956dce5437b34ec830d4b146dbf6e))
* add quill dependency ([8bbf26c](https://github.com/pervasive-cats/toys-store-stores/commit/8bbf26c3f1eed039bbd75257798cb7ba75ee6e5f))
* add repository ([ca28689](https://github.com/pervasive-cats/toys-store-stores/commit/ca2868976b378d2fa89b35841ef7e2ed9443382a))
* add shelf implementation and test ([d992c62](https://github.com/pervasive-cats/toys-store-stores/commit/d992c62632c829aae6b354ebebaccc0f6b8a42ec))
* add shelf ops implementation and test ([e86b2de](https://github.com/pervasive-cats/toys-store-stores/commit/e86b2de9908a7f9e7f2bb07a4494ce34096d137f))
* add shelving group implementation and test ([94ecdf0](https://github.com/pervasive-cats/toys-store-stores/commit/94ecdf026375ddf23dccf2ca0cf083b33cf78ab5))
* add shelving group ops implementation and test ([8e28a91](https://github.com/pervasive-cats/toys-store-stores/commit/8e28a91eba23255fe080c97b828a10bcc9700426))
* add shelving implementation and test ([8a27c64](https://github.com/pervasive-cats/toys-store-stores/commit/8a27c641b580f13faa9e618b4d82bd692553b689))
* add shelving ops implementation and test ([0a026b6](https://github.com/pervasive-cats/toys-store-stores/commit/0a026b6ad1b1469007e7800b80af824162c5f676))
* add sql table for stores ([6658eb7](https://github.com/pervasive-cats/toys-store-stores/commit/6658eb76e2f40c0bc3e1d2cbce1a53594792416f))
* add store implementation ([dd56466](https://github.com/pervasive-cats/toys-store-stores/commit/dd564669437074a5b66ccf873596c9d0cbbaf73e))
* add store test implementation ([850f1bf](https://github.com/pervasive-cats/toys-store-stores/commit/850f1bf1b0840cc0567529c3054f43595cb661dd))
* update gitignore to ignore file with .semanticdb extension ([314311f](https://github.com/pervasive-cats/toys-store-stores/commit/314311f7df043622f57c98ed19ad03b25656d6df))

# [1.0.0-beta.3](https://github.com/pervasive-cats/toys-store-stores/compare/v1.0.0-beta.2...v1.0.0-beta.3) (2023-03-12)


### Bug Fixes

* fix implementation of broker actor ([07e8396](https://github.com/pervasive-cats/toys-store-stores/commit/07e8396f1735d98e1c08d94497990472bfc11547))
* improved message broker actor ([6b87015](https://github.com/pervasive-cats/toys-store-stores/commit/6b870150f94eb14485b206d0512b758a7652efee))


### Features

* add domain events in serializer ([048558d](https://github.com/pervasive-cats/toys-store-stores/commit/048558d5170efd537b058a3ad407b50a4cc12c9a))
* add domain events in serializer ([84a1984](https://github.com/pervasive-cats/toys-store-stores/commit/84a19844e7046b07a0eccfd6b9cde07ba5708ceb))
* add message broker actor and command ([e58d2ff](https://github.com/pervasive-cats/toys-store-stores/commit/e58d2ff9656a99c7ebc38e824d1bbb098ce95093))
* add message broker test implementation ([9675b2d](https://github.com/pervasive-cats/toys-store-stores/commit/9675b2d98eeee3de4794bb1ee872928489e84a21))
* add remove to repository and test ([03ce9a2](https://github.com/pervasive-cats/toys-store-stores/commit/03ce9a25217b13a981777d818c0cd0b2d4707404))
* add repository and test ([72550f9](https://github.com/pervasive-cats/toys-store-stores/commit/72550f92bb84039d8d0eb183f5fb686018a701a0))
* add serializers for value objects ([03030ba](https://github.com/pervasive-cats/toys-store-stores/commit/03030ba4aff981d99d0b4efd653ca9e1e8349ab2))
* improve repository test ([3475d4c](https://github.com/pervasive-cats/toys-store-stores/commit/3475d4c74c25153faa51d9856c026363f8af4643))
* improved message actor broker test ([9355928](https://github.com/pervasive-cats/toys-store-stores/commit/9355928a31c917bf456f1f698822df8a24d14290))

# [1.0.0-beta.2](https://github.com/pervasive-cats/toys-store-stores/compare/v1.0.0-beta.1...v1.0.0-beta.2) (2023-02-22)


### Bug Fixes

* updated catalog item put in place test ([8dcc149](https://github.com/pervasive-cats/toys-store-stores/commit/8dcc14914ba5eb9aacf1765a34099989ca576eca))


### Features

* add AntiTheftSystemAlarmTriggered implementation and test ([afc4018](https://github.com/pervasive-cats/toys-store-stores/commit/afc4018d585141f096ba3e1b89a41f93f6d979f6))
* add catalog item and test ([419d9e2](https://github.com/pervasive-cats/toys-store-stores/commit/419d9e2b75d47ee1b7f8c1cdb139cdf4c8013012))
* add catalog item lifted implementation and test ([37b38d8](https://github.com/pervasive-cats/toys-store-stores/commit/37b38d823ab98ad64d847494583ff8c2addd7039))
* add Catalog Item Lifting Registered implementation and test ([c1c6dd3](https://github.com/pervasive-cats/toys-store-stores/commit/c1c6dd31d244ccbf791f03ffade7953e1f574d86))
* add catalog item put in place implementation and test ([a047159](https://github.com/pervasive-cats/toys-store-stores/commit/a0471594b7e46b36ae5bb428f4a234097cb1dafb))
* add count test ([77f999d](https://github.com/pervasive-cats/toys-store-stores/commit/77f999d2fb6b7efdadd6c612dec27a8501d30097))
* add item deleted implementation and test ([776b30d](https://github.com/pervasive-cats/toys-store-stores/commit/776b30dc5bbb9dc9984c42bc72b79db04bae8cb6))
* add item implementation and test ([3186718](https://github.com/pervasive-cats/toys-store-stores/commit/3186718aafcb0b5293caeeba27d3f3fb86853541))
* add item inserted in drop system implementation and test ([21231d5](https://github.com/pervasive-cats/toys-store-stores/commit/21231d5b1c0dd58b78a4efed532215fc60f00bb9))
* add item put in place implementation and test ([0e58d65](https://github.com/pervasive-cats/toys-store-stores/commit/0e58d6518940de9648870a82ed23aec01b4c8e15))
* add item returned implementation and test ([e6e88da](https://github.com/pervasive-cats/toys-store-stores/commit/e6e88daaac6500cf58727903cf7f1b36be4dd728))
* add itemid implementation and test ([02de4dc](https://github.com/pervasive-cats/toys-store-stores/commit/02de4dcad875608021b59039135a0b9a23050366))
* add items row id implementation and test ([e6e9cb6](https://github.com/pervasive-cats/toys-store-stores/commit/e6e9cb6920d5ebf09ea08af4854b6e6c8d417949))
* add items row test ([9bbf45b](https://github.com/pervasive-cats/toys-store-stores/commit/9bbf45bd7fdd08d9fbebe3e79dc0836086975001))
* add itemsRow and count  implementation ([1caaf98](https://github.com/pervasive-cats/toys-store-stores/commit/1caaf98c68bfe68310751e5354258953fd5e732e))
* add shelf id implementation and test ([1dfda3a](https://github.com/pervasive-cats/toys-store-stores/commit/1dfda3a8b6e88744bd68aa1e1db39358fbdd29ae))
* add shelving group id implementation and test ([09bbe95](https://github.com/pervasive-cats/toys-store-stores/commit/09bbe95e53548bb3e14ef55375141f5e455dad65))
* add shelving id implementation and test ([7aad065](https://github.com/pervasive-cats/toys-store-stores/commit/7aad065b55fd1dd04a7b6393a0fba927bc6128ca))

# 1.0.0-beta.1 (2023-01-23)


### Features

* add entities interfaces for store aggregate ([e87257d](https://github.com/pervasive-cats/toys-store-stores/commit/e87257db1e2299bf76f94827e366cda730a81ecd))
* add events interfaces for store aggregate ([85a7fbc](https://github.com/pervasive-cats/toys-store-stores/commit/85a7fbc55a3ce379d7bdb3e870cc79e0c67bf2cb))
* add services interfaces for store aggregate ([75880bf](https://github.com/pervasive-cats/toys-store-stores/commit/75880bf38d579bfd88df5935e7bc8d0f0df970cb))
* add valueobjects interfaces for store aggregate ([e343894](https://github.com/pervasive-cats/toys-store-stores/commit/e34389451134fc247d404f652a372a5bb6713ea4))
