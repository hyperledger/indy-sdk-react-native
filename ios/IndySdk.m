//
// Copyright 2019 ABSA Group Limited
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


#import <Indy/Indy.h>

#if __has_include(<React/RCTViewManager.h>)
#import <React/RCTViewManager.h>
#elif __has_include("RCTViewManager.h")
#import "RCTViewManager.h"
#else
#import "React/RCTViewManager.h" // Required when used as a Pod in a Swift project
#endif

// import RCTEventDispatcher
#if __has_include(<React/RCTEventDispatcher.h>)
#import <React/RCTEventDispatcher.h>
#elif __has_include("RCTEventDispatcher.h")
#import "RCTEventDispatcher.h"
#else
#import "React/RCTEventDispatcher.h" // Required when used as a Pod in a Swift project
#endif


@interface RCT_EXTERN_MODULE(IndySdk, NSObject)

RCT_EXTERN_METHOD(sampleMethod: (NSString *)stringArgument numberArgument:(nonnull NSNumber *)numberArgument
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

// wallet
RCT_EXTERN_METHOD(createWallet: (NSString *)config credentials:(NSString *)credentials
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(openWallet: (NSString *)config credentials:(NSString *)credentials
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(closeWallet: (nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(deleteWallet: (NSString *)config credentials:(NSString *)credentials
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(exportWallet: (nonnull NSNumber *)walletHandle exportConfig:(NSString *)exportConfig
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(importWallet: (NSString *)config credentials:(NSString *)credentials importConfig:(NSString *)importConfig
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

// did
RCT_EXTERN_METHOD(createAndStoreMyDid: (NSString *)didJson walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(keyForDid: (NSString *)did poolHandle:(nonnull NSNumber *)poolHandle walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(keyForLocalDid: (NSString *)did walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(storeTheirDid: (NSString *)identityJSON walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

// pairwise
RCT_EXTERN_METHOD(createPairwise: (NSString *)theirDid myDid:(NSString *)myDid metadata:(NSString *)metadata walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getPairwise: (NSString *)theirDid walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

// crypto
RCT_EXTERN_METHOD(createKey: (NSString *)key
                  walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(cryptoAnonCrypt: (NSString *)message theirKey:(NSString *)theirKey
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(cryptoAnonDecrypt: (NSArray *)encryptedMessage myKey:(NSString *)myKey walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(cryptoAuthCrypt: (NSString *)message
                  myKey:(NSString *)myKey
                  theirKey:(NSString *)theirKey
                  walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(cryptoAuthDecrypt: (NSArray *)encryptedMessage myKey:(NSString *)myKey walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(cryptoSign: (nonnull NSNumber *)wh
                  signerVk: (NSString *)signerVk
                  message: (NSArray *)message
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(cryptoVerify: (NSString *)signerVk
                  message: (NSArray *)message
                  signature: (NSArray *)signature
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(packMessage: (nonnull NSNumber *)wh
                  message: (NSArray *)message
                  receiverKeys: (NSString *)receiverKeys
                  senderVk: (NSString *)senderVk
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(unpackMessage: (nonnull NSNumber *)wh
                  jwe: (NSArray *)jwe
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

// pool
RCT_EXTERN_METHOD(createPoolLedgerConfig: (NSString *)name poolConfig:(NSString *)poolConfig
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(openLedger: (NSString *)name poolConfig:(nullable NSString *)poolConfig
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(setProtocolVersion: (nonnull NSNumber *)protocolVersion
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(closePoolLedger: (nonnull NSNumber *)poolHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

// ledger

RCT_EXTERN_METHOD(submitRequest: (NSString *)requestJSON poolHandle:(nonnull NSNumber *)poolHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetSchemaRequest: (NSString *)submitterDid id:(NSString *)id
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(parseGetSchemaResponse: (NSString *)getSchemaResponse
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetCredDefRequest: (NSString *)submitterDid id:(NSString *)id
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(parseGetCredDefResponse: (NSString *)getCredDefResponse
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetRevocRegDefRequest: (NSString *)submitterDid id:(NSString *)id
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(parseGetRevocRegDefResponse: (NSString *)getCredDefResponse
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetRevocRegDeltaRequest: (NSString *)submitterDid id:(NSString *)id from:(nonnull NSNumber *)from to:(nonnull NSNumber *)to
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(parseGetRevocRegDeltaResponse: (NSString *)getRevocRegDeltaResponse
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetRevocRegRequest: (NSString *)submitterDid id:(NSString *)id timestamp:(nonnull NSNumber *)timestamp
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(parseGetRevocRegResponse: (NSString *)getRevocRegResponse
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetAttribRequest: (nullable NSString *)submitterDid
                  targetDid:(NSString *)targetDid
                  raw:(nullable NSString *)raw
                  hash:(nullable NSString *)hash
                  enc:(nullable NSString *)enc
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(buildGetNymRequest: (nullable NSString *)submitterDid
                  targetDid:(NSString *)targetDid
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(parseGetNymResponse: (NSString *)response
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

// anoncreds

RCT_EXTERN_METHOD(proverCreateMasterSecret: (NSString *)masterSecretId walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverCreateCredentialReq: (NSString *)credOfferJSON
                  credentialDefJSON:(NSString *)credentialDefJSON
                  proverDID:(NSString *)proverDID
                  masterSecretID:(NSString *)masterSecretID
                  walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverStoreCredential: (NSString *)credJson
                  credID:(NSString *)credID
                  credReqMetadataJSON:(NSString *)credReqMetadataJSON
                  credDefJSON:(NSString *)credDefJSON
                  revRegDefJSON:(NSString *)revRegDefJSON walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverGetCredentials: (NSString *)filter walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverSearchCredentials: (nonnull NSNumber *)walletHandle
                  queryJSON: (NSString *)queryJSON
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(proverFetchCredentials: (nonnull NSNumber *)searchHandle
                  count: (nonnull NSNumber *)count
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(proverCloseCredentialsSearch: (nonnull NSNumber *)searchHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )


RCT_EXTERN_METHOD(proverGetCredential: (NSString *)credId walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverDeleteCredential: (NSString *)credId walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverGetCredentialsForProofReq: (NSString *)proofReqJSON walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(proverSearchCredentialsForProofReq: (nonnull NSNumber *)walletHandle
                  proofReqJSON: (NSString *)proofReqJSON
                  extraQueryJSON: (NSString *)extraQueryJSON
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(proverFetchCredentialsForProofReq: (nonnull NSNumber *)searchHandle
                  itemReferent: (NSString *)itemReferent
                  count: (nonnull NSNumber *)count
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(proverCloseCredentialsSearchForProofReq: (nonnull NSNumber *)searchHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(proverCreateProofForRequest: (NSString *)proofReqJSON
                  requestedCredentialsJSON:(NSString *)requestedCredentialsJSON
                  masterSecretID:(NSString *)masterSecretID
                  schemasJSON:(NSString *)schemasJSON
                  credentialDefsJSON:(NSString *)credentialDefsJSON
                  revocStatesJSON:(NSString *)revocStatesJSON
                  walletHandle:(nonnull NSNumber *)walletHandle
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(verifierVerifyProof: (NSString *)proofReqJSON
                  proofJSON:(NSString *)proofJSON
                  schemasJSON:(NSString *)schemasJSON
                  credentialDefsJSON:(NSString *)credentialDefsJSON
                  revocRegDefsJSON:(NSString *)revocRegDefsJSON
                  revocRegsJSON:(NSString *)revocRegsJSON
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(generateNonce: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(generateWalletKey: (NSString *) configJson
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(createRevocationState: (nonnull NSNumber *)blobStorageReaderHandle
                  revRegDef:(NSString *)revRegDef
                  revRegDelta:(NSString *)revRegDelta
                  timestamp:(nonnull NSNumber *)timestamp
                  credRevId:(NSString *)credRevId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

// non_secrets
RCT_EXTERN_METHOD(addWalletRecord: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  value: (NSString *)value
                  tags: (NSString *)tags
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(updateWalletRecordValue: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  value: (NSString *)value
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(updateWalletRecordTags: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  tags: (NSString *)tags
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(addWalletRecordTags: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  tags: (NSString *)tags
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(deleteWalletRecordTags: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  tags: (NSString *)tags
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(deleteWalletRecord: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(getWalletRecord: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  id: (NSString *)id
                  options: (NSString *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(openWalletSearch: (nonnull NSNumber *)wh
                  type: (NSString *)type
                  query: (NSString *)query
                  options: (NSString *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(fetchWalletSearchNextRecords: (nonnull NSNumber *)wh
                  sh: (nonnull NSNumber *)sh
                  count: (nonnull NSNumber *)count
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(closeWalletSearch: (nonnull NSNumber *)sh
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

// Blob Storage
RCT_EXTERN_METHOD(openBlobStorageReader: (NSString *)type
                  config: (NSString *)config
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

@end
