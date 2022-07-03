/**
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reactlibrary;

import android.annotation.TargetApi;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.ErrorCode;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateSchemaResult;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateAndStoreCredentialDefResult;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateCredentialResult;
import org.hyperledger.indy.sdk.blob_storage.BlobStorageReader;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.crypto.CryptoResults;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.non_secrets.WalletSearch;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@TargetApi(24)
public class IndySdkModule extends ReactContextBaseJavaModule {

    private static final String TAG = "IndySdk";
    private final ReactApplicationContext reactContext;

    private static Map<Integer, Wallet> walletMap = new ConcurrentHashMap<>();
    private static Map<String, Integer> walletIdToHandleMap = new ConcurrentHashMap<>();
    private static Map<Integer, Pool> poolMap = new ConcurrentHashMap<>();
    private static Map<String, Integer> poolNameToHandleMap = new ConcurrentHashMap<>();
    private static Map<Integer, WalletSearch> searchMap = new ConcurrentHashMap<>();
    private static Map<Integer, WalletSearch> searchCredential = new ConcurrentHashMap<>();
    private Map<Integer, CredentialsSearchForProofReq> credentialsForProofReqSearchMap;
    private Map<Integer, CredentialsSearch> credentialsSearchMap;
    // Java wrapper does not expose credentialSearchHandle
    private int credentialSearchIterator = 0;

    public IndySdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.credentialsForProofReqSearchMap = new ConcurrentHashMap<>();
        this.credentialsSearchMap = new ConcurrentHashMap<>();
    }

    @Override
    public String getName() {
        return "IndySdk";
    }

    // wallet

    @ReactMethod
    public void createWallet(String configJson, String credentialsJson, Promise promise) {
        try {
            Wallet.createWallet(configJson, credentialsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void openWallet(String configJson, String credentialsJson, Promise promise) {
        // Retrieve wallet id
        Gson gson = new Gson();
        JsonObject config = gson.fromJson(configJson, JsonObject.class);
        String walletId = config.get("id").getAsString();

        // If wallet is already opened, return open wallet
        if (walletIdToHandleMap.containsKey(walletId)) {
            promise.resolve(walletIdToHandleMap.get(walletId));
        }

        try {
            Wallet wallet = Wallet.openWallet(configJson, credentialsJson).get();
            walletIdToHandleMap.put(walletId, wallet.getWalletHandle());
            walletMap.put(wallet.getWalletHandle(), wallet);
            promise.resolve(wallet.getWalletHandle());
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void closeWallet(int walletHandle, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            wallet.closeWallet().get();
            walletMap.remove(walletHandle);

            // Remove wallet id mapping
            for (Map.Entry<String, Integer> entry : walletIdToHandleMap.entrySet()) {
                if (entry.getValue().equals(walletHandle)) {
                    walletIdToHandleMap.remove(entry.getKey());
                    break;
                }
            }

            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void deleteWallet(String configJson, String credentialsJson, Promise promise) {
        try {
            Wallet.deleteWallet(configJson, credentialsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void exportWallet(int walletHandle, String exportConfig, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Wallet.exportWallet(wallet, exportConfig).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void importWallet(String config, String credentials, String importConfig, Promise promise) {
        try {
            Wallet.importWallet(config, credentials, importConfig).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // did

    @ReactMethod
    public void listMyDidsWithMeta(int walletHandle, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String listDidsWithMetaJson = Did.getListMyDidsWithMeta(wallet).get();
            promise.resolve(listDidsWithMetaJson);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void setDidMetadata(int walletHandle, String did, String metadataJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Did.setDidMetadata(wallet, did, metadataJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void createAndStoreMyDid(int walletHandle, String didJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            DidResults.CreateAndStoreMyDidResult createMyDidResult = Did.createAndStoreMyDid(wallet, didJson).get();
            String myDid = createMyDidResult.getDid();
            String myVerkey = createMyDidResult.getVerkey();
            WritableArray response = new WritableNativeArray();
            response.pushString(myDid);
            response.pushString(myVerkey);
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void keyForDid(int poolHandle, int walletHandle, String did, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Pool pool = poolMap.get(poolHandle);
            String receivedKey = Did.keyForDid(pool, wallet, did).get();
            promise.resolve(receivedKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void keyForLocalDid(int walletHandle, String did, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String receivedKey = Did.keyForLocalDid(wallet, did).get();
            promise.resolve(receivedKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // pairwise
    @ReactMethod
    public void createPairwise(int walletHandle, String theirDid, String myDid, String metadata, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Pairwise.createPairwise(wallet, theirDid, myDid, metadata).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void getPairwise(int walletHandle, String theirDid, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String receivedKey = Pairwise.getPairwise(wallet, theirDid).get();
            promise.resolve(receivedKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // crypto

    private byte[] readableArrayToBuffer(ReadableArray arr) {
        byte[] buffer = new byte[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            buffer[i] = (byte) arr.getInt(i);
        }
        return buffer;
    }

    @ReactMethod
    public void createKey(int walletHandle, String key, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String verkey = Crypto.createKey(wallet, key).get();
            promise.resolve(verkey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoAnonCrypt(String theirKey, ReadableArray message, Promise promise) {
        try {
            byte[] buffer = readableArrayToBuffer(message);
            byte[] encryptedData = Crypto.anonCrypt(theirKey, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : encryptedData) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoAnonDecrypt(int walletHandle, String recipientVk, ReadableArray encryptedMessage,
            Promise promise) {
        try {
            byte[] encryptedMessageBytes = readableArrayToBuffer(encryptedMessage);
            Wallet wallet = walletMap.get(walletHandle);
            byte[] decryptedData = Crypto.anonDecrypt(wallet, recipientVk, encryptedMessageBytes).get();
            promise.resolve(decryptedData);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    @Deprecated
    public void cryptoAuthCrypt(int walletHandle, String senderVk, String recipientVk, ReadableArray message,
            Promise promise) {
        try {
            byte[] buffer = readableArrayToBuffer(message);
            Wallet wallet = walletMap.get(walletHandle);
            byte[] encryptedData = Crypto.authCrypt(wallet, senderVk, recipientVk, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : encryptedData) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    @Deprecated
    public void cryptoAuthDecrypt(int walletHandle, String recipientVk, ReadableArray encryptedMessage,
            Promise promise) {
        try {
            byte[] encryptedMessageBytes = readableArrayToBuffer(encryptedMessage);
            Wallet wallet = walletMap.get(walletHandle);
            CryptoResults.AuthDecryptResult decryptedResult = Crypto
                    .authDecrypt(wallet, recipientVk, encryptedMessageBytes).get();
            String theirKey = decryptedResult.getVerkey();

            WritableArray decryptedData = new WritableNativeArray();
            for (byte b : decryptedResult.getDecryptedMessage()) {
                decryptedData.pushInt(b);
            }

            WritableArray response = new WritableNativeArray();
            response.pushString(theirKey);
            response.pushArray(decryptedData);
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoSign(int walletHandle, String signerVk, ReadableArray messageRaw, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            byte[] buffer = readableArrayToBuffer(messageRaw);
            byte[] signature = Crypto.cryptoSign(wallet, signerVk, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : signature) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoVerify(String signerVk, ReadableArray messageRaw, ReadableArray signatureRaw, Promise promise) {
        try {
            byte[] messageBuf = readableArrayToBuffer(messageRaw);
            byte[] sigBuf = readableArrayToBuffer(signatureRaw);
            boolean valid = Crypto.cryptoVerify(signerVk, messageBuf, sigBuf).get();

            promise.resolve(valid);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void packMessage(int walletHandle, ReadableArray message, ReadableArray receiverKeys, String senderVk,
            Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            byte[] buffer = readableArrayToBuffer(message);

            String[] keys = new String[receiverKeys.size()];
            for (int i = 0; i < receiverKeys.size(); i++) {
                keys[i] = receiverKeys.getString(i);
            }
            Gson gson = new Gson();
            String receiverKeysJson = gson.toJson(keys);

            byte[] jwe = Crypto.packMessage(wallet, receiverKeysJson, senderVk, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : jwe) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void unpackMessage(int walletHandle, ReadableArray jwe, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            byte[] buffer = readableArrayToBuffer(jwe);
            byte[] res = Crypto.unpackMessage(wallet, buffer).get();

            WritableArray result = new WritableNativeArray();
            for (byte b : res) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // pool

    @ReactMethod
    public void setProtocolVersion(int protocolVersion, Promise promise) {
        try {
            Pool.setProtocolVersion(protocolVersion).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void createPoolLedgerConfig(String configName, String poolConfig, Promise promise) {
        try {
            Pool.createPoolLedgerConfig(configName, poolConfig).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void openPoolLedger(final String configName, final String poolConfig, final Promise promise) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (poolNameToHandleMap.get(configName) != null) {
                        promise.resolve(poolNameToHandleMap.get(configName));

                    } else {
                        Pool pool = Pool.openPoolLedger(configName, poolConfig).get();
                        poolMap.put(pool.getPoolHandle(), pool);
                        poolNameToHandleMap.put(configName, pool.getPoolHandle());
                        promise.resolve(pool.getPoolHandle());
                    }
                } catch (Exception e) {
                    IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
                    promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
                }
            }
        }, "openPoolLedger").start();
    }

    @ReactMethod
    public void closePoolLedger(final int handle, final Promise promise) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Pool pool = poolMap.get(handle);
                    pool.closePoolLedger().get();
                    poolMap.remove(handle);

                    // Remove pool id mapping
                    for (Map.Entry<String, Integer> entry : poolNameToHandleMap.entrySet()) {
                        if (entry.getValue().equals(handle)) {
                            poolNameToHandleMap.remove(entry.getKey());
                            break;
                        }
                    }

                    promise.resolve(null);
                } catch (Exception e) {
                    IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
                    promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
                }
            }
        }, "closePoolLedger").start();
    }

    // ledger

    @ReactMethod
    public void submitRequest(final int poolHandle, final String requestJson, final Promise promise) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Pool pool = poolMap.get(poolHandle);
                    String response = Ledger.submitRequest(pool, requestJson).get();
                    promise.resolve(response);
                } catch (Exception e) {
                    IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
                    promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
                }
            }

        }, "submitRequest").start();
    }

    @ReactMethod
    public void signRequest(int walletHandle, String submitterDid, String requestJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String request = Ledger.signRequest(wallet, submitterDid, requestJson).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildSchemaRequest(String submitterDid, String data, Promise promise) {
        try {
            String request = Ledger.buildSchemaRequest(submitterDid, data).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetSchemaRequest(String submitterDid, String id, Promise promise) {
        try {
            String request = Ledger.buildGetSchemaRequest(submitterDid, id).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetSchemaResponse(String getSchemaResponse, Promise promise) {
        try {
            LedgerResults.ParseResponseResult ledgerResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildCredDefRequest(String submitterDid, String data, Promise promise) {
        try {
            String request = Ledger.buildCredDefRequest(submitterDid, data).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetCredDefRequest(String submitterDid, String id, Promise promise) {
        try {
            String request = Ledger.buildGetCredDefRequest(submitterDid, id).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetCredDefResponse(String getCredDefResponse, Promise promise) {
        try {
            LedgerResults.ParseResponseResult ledgerResult = Ledger.parseGetCredDefResponse(getCredDefResponse).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetRevocRegDefRequest(String submitterDid, String revocRegDefId, Promise promise) {
        try {
            String request = Ledger.buildGetRevocRegDefRequest(submitterDid, revocRegDefId).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetRevocRegDefResponse(String response, Promise promise) {
        try {
            LedgerResults.ParseResponseResult ledgerResult = Ledger.parseGetRevocRegDefResponse(response).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetRevocRegDeltaRequest(
            String submitterDid,
            String revocRegDefId,
            int from,
            int to,
            Promise promise) {
        try {
            String request = Ledger.buildGetRevocRegDeltaRequest(submitterDid, revocRegDefId, from, to).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetRevocRegDeltaResponse(String getRevocRegDeltaResponse, Promise promise) {
        try {
            LedgerResults.ParseRegistryResponseResult ledgerResult = Ledger
                    .parseGetRevocRegDeltaResponse(getRevocRegDeltaResponse).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            result.pushInt((int) ledgerResult.getTimestamp());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetRevocRegRequest(
            String submitterDid,
            String revocRegDefId,
            int timestamp,
            Promise promise) {
        try {
            String request = Ledger.buildGetRevocRegRequest(submitterDid, revocRegDefId, timestamp).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetRevocRegResponse(String getRevocRegResponse, Promise promise) {
        try {
            LedgerResults.ParseRegistryResponseResult ledgerResult = Ledger
                    .parseGetRevocRegResponse(getRevocRegResponse).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            result.pushInt((int) ledgerResult.getTimestamp());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetAttribRequest(String submitterDid, String targetDid, String raw, String hash, String enc,
            Promise promise) {
        try {
            String request = Ledger.buildGetAttribRequest(submitterDid, targetDid, raw, hash, enc).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetNymRequest(String submitterDid, String targetDid, Promise promise) {
        try {
            String request = Ledger.buildGetNymRequest(submitterDid, targetDid).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetNymResponse(String response, Promise promise) {
        try {
            String parsedResponse = Ledger.parseGetNymResponse(response).get();
            promise.resolve(parsedResponse);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void appendTxnAuthorAgreementAcceptanceToRequest(String requestJson, String text, String version,
            String taaDigest, String mechanism, int time, Promise promise) {
        try {
            String request = Ledger
                    .appendTxnAuthorAgreementAcceptanceToRequest(requestJson, text, version, taaDigest, mechanism, time)
                    .get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetTxnAuthorAgreementRequest(String submitterDid, String data, Promise promise) {
        try {
            String request = Ledger.buildGetTxnAuthorAgreementRequest(submitterDid, data).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // anoncreds

    @ReactMethod
    public void issuerCreateSchema(String issuerDid, String name, String version, String attrs, Promise promise) {
        try {
            IssuerCreateSchemaResult schemaResult = Anoncreds.issuerCreateSchema(issuerDid, name, version, attrs).get();
            WritableArray response = new WritableNativeArray();
            response.pushString(schemaResult.getSchemaId());
            response.pushString(schemaResult.getSchemaJson());
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void issuerCreateAndStoreCredentialDef(int walletHandle, String issuerDid, String schemaJson, String tag,
            String signatureType, String configJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            IssuerCreateAndStoreCredentialDefResult schemaResult = Anoncreds
                    .issuerCreateAndStoreCredentialDef(wallet, issuerDid, schemaJson, tag, signatureType, configJson)
                    .get();
            WritableArray response = new WritableNativeArray();
            response.pushString(schemaResult.getCredDefId());
            response.pushString(schemaResult.getCredDefJson());
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void issuerCreateCredential(int walletHandle, String credOffer, String credReq, String credvalues,
            String revRegId, int blobStorageReaderHandle, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            IssuerCreateCredentialResult createCredResult = Anoncreds
                    .issuerCreateCredential(wallet, credOffer, credReq, credvalues, revRegId, blobStorageReaderHandle)
                    .get();
            WritableArray response = new WritableNativeArray();
            response.pushString(createCredResult.getCredentialJson());
            response.pushString(createCredResult.getRevocId());
            response.pushString(createCredResult.getRevocRegDeltaJson());
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void issuerCreateCredentialOffer(int walletHandle, String credDefId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String response = Anoncreds.issuerCreateCredentialOffer(wallet, credDefId).get();
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverCreateMasterSecret(int walletHandle, String masterSecretId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String outputMasterSecretId = Anoncreds.proverCreateMasterSecret(wallet, masterSecretId).get();
            promise.resolve(outputMasterSecretId);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverCreateCredentialReq(int walletHandle, String proverDid, String credentialOfferJson,
            String credentialDefJson, String masterSecretId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult = Anoncreds
                    .proverCreateCredentialReq(wallet, proverDid, credentialOfferJson, credentialDefJson,
                            masterSecretId)
                    .get();
            WritableArray response = new WritableNativeArray();
            response.pushString(credentialRequestResult.getCredentialRequestJson());
            response.pushString(credentialRequestResult.getCredentialRequestMetadataJson());
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverStoreCredential(int walletHandle, String credId, String credReqMetadataJson, String credJson,
            String credDefJson, String revRegDefJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String newCredId = Anoncreds
                    .proverStoreCredential(wallet, credId, credReqMetadataJson, credJson, credDefJson, revRegDefJson)
                    .get();
            promise.resolve(newCredId);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverDeleteCredential(int walletHandle, String credId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Anoncreds.proverDeleteCredential(wallet, credId).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverGetCredential(int walletHandle, String credId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String credential = Anoncreds.proverGetCredential(wallet, credId).get();
            promise.resolve(credential);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverGetCredentials(int walletHandle, String filter, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String credentials = Anoncreds.proverGetCredentials(wallet, filter).get();
            promise.resolve(credentials);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverSearchCredentials(int walletHandle, String queryJson, Promise promise) {
        try {
            int searchHandle = credentialSearchIterator++;
            Wallet wallet = walletMap.get(walletHandle);
            CredentialsSearch search = CredentialsSearch.open(wallet, queryJson)
                    .get();
            credentialsSearchMap.put(searchHandle, search);
            promise.resolve([searchHandle, search.totalCount()]);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverFetchCredentials(int searchHandle, int count, Promise promise) {
        try {
            CredentialsSearch search = credentialsSearchMap.get(searchHandle);
            String recordsJson = search.fetchNextCredentials(count).get();
            promise.resolve(recordsJson);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverCloseCredentialsSearch(int searchHandle, Promise promise) {
        try {
            CredentialsSearch search = credentialsSearchMap.get(searchHandle);
            search.close();
            credentialsSearchMap.remove(searchHandle);
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverGetCredentialsForProofReq(
            int walletHandle,
            String proofRequest,
            Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String credentials = Anoncreds.proverGetCredentialsForProofReq(
                    wallet,
                    proofRequest).get();
            promise.resolve(credentials);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverSearchCredentialsForProofReq(int walletHandle, String proofRequest, String extraQuery,
            Promise promise) {
        try {
            int searchHandle = credentialSearchIterator++;
            Wallet wallet = walletMap.get(walletHandle);
            CredentialsSearchForProofReq search = CredentialsSearchForProofReq.open(wallet, proofRequest, extraQuery)
                    .get();
            credentialsForProofReqSearchMap.put(searchHandle, search);
            promise.resolve(searchHandle);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverFetchCredentialsForProofReq(int searchHandle, String itemReferent, int count, Promise promise) {
        try {
            CredentialsSearchForProofReq search = credentialsForProofReqSearchMap.get(searchHandle);
            String recordsJson = search.fetchNextCredentials(itemReferent, count).get();
            promise.resolve(recordsJson);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverCloseCredentialsSearchForProofReq(int searchHandle, Promise promise) {
        try {
            CredentialsSearchForProofReq search = credentialsForProofReqSearchMap.get(searchHandle);
            search.close();
            credentialsForProofReqSearchMap.remove(searchHandle);
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverCreateProof(
            int walletHandle,
            String proofRequest,
            String requestedCredentials,
            String masterSecret,
            String schemas,
            String credentialDefs,
            String revocStates,
            Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String proofJson = Anoncreds.proverCreateProof(
                    wallet,
                    proofRequest,
                    requestedCredentials,
                    masterSecret,
                    schemas,
                    credentialDefs,
                    revocStates).get();
            promise.resolve(proofJson);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void verifierVerifyProof(
            String proofRequest,
            String proof,
            String schemas,
            String credentialDefs,
            String revocRegDefs,
            String revocRegs,
            Promise promise) {
        try {
            Boolean verified = Anoncreds
                    .verifierVerifyProof(proofRequest, proof, schemas, credentialDefs, revocRegDefs, revocRegs).get();

            promise.resolve(verified);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void generateNonce(Promise promise) {
        try {
            String nonce = Anoncreds.generateNonce().get();
            promise.resolve(nonce);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void generateWalletKey(String configJson, Promise promise) {
        try {
            String walletKey = Wallet.generateWalletKey(configJson).get();
            promise.resolve(walletKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void createRevocationState(
            int blobStorageReaderHandle,
            String revRegDef,
            String revRegDelta,
            int timestamp,
            String credRevId,
            Promise promise) {
        try {
            String result = Anoncreds
                    .createRevocationState(blobStorageReaderHandle, revRegDef, revRegDelta, timestamp, credRevId).get();
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // blob_storage

    @ReactMethod
    public void openBlobStorageReader(String type, String tailsWriterConfig, Promise promise) {
        try {
            BlobStorageReader response = BlobStorageReader.openReader(type, tailsWriterConfig).get();
            promise.resolve(response.getBlobStorageReaderHandle());
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // non_secrets

    @ReactMethod
    public void addWalletRecord(int walletHandle, String type, String id, String value, String tagsJson,
            Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.add(wallet, type, id, value, tagsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void updateWalletRecordValue(int walletHandle, String type, String id, String value, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.updateValue(wallet, type, id, value).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void updateWalletRecordTags(int walletHandle, String type, String id, String tagsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.updateTags(wallet, type, id, tagsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void addWalletRecordTags(int walletHandle, String type, String id, String tagsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.addTags(wallet, type, id, tagsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void deleteWalletRecordTags(int walletHandle, String type, String id, String tagNamesJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.deleteTags(wallet, type, id, tagNamesJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void deleteWalletRecord(int walletHandle, String type, String id, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.delete(wallet, type, id).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void getWalletRecord(int walletHandle, String type, String id, String optionsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String record = WalletRecord.get(wallet, type, id, optionsJson).get();
            promise.resolve(record);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void openWalletSearch(int walletHandle, String type, String queryJson, String optionsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletSearch search = WalletSearch.open(wallet, type, queryJson, optionsJson).get();
            searchMap.put(search.getSearchHandle(), search);
            promise.resolve(search.getSearchHandle());
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void fetchWalletSearchNextRecords(int walletHandle, int walletSearchHandle, int count, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletSearch search = searchMap.get(walletSearchHandle);
            String recordsJson = WalletSearch.searchFetchNextRecords(wallet, search, count).get();
            promise.resolve(recordsJson);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void closeWalletSearch(int walletSearchHandle, Promise promise) {
        try {
            WalletSearch search = searchMap.get(walletSearchHandle);
            WalletSearch.closeSearch(search);
            searchMap.remove(walletSearchHandle);
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    class IndySdkRejectResponse {
        private String name = "IndyError";
        private int indyCode;
        private String indyName;
        private String message;
        private String indyCurrentErrorJson;
        private String indyMessage;
        private String indyBacktrace;

        private IndySdkRejectResponse(Throwable e) {
            // Indy bridge exposed API should return consistently only numeric code
            // When we don't get IndyException and Indy SDK error code we return zero as
            // default
            indyCode = 0;

            if (e instanceof ExecutionException) {
                Throwable cause = e.getCause();
                if (cause instanceof IndyException) {
                    IndyException indyException = (IndyException) cause;
                    indyCode = indyException.getSdkErrorCode();

                    ErrorCode errorCode = ErrorCode.valueOf(indyCode);
                    indyName = errorCode.toString();
                    message = indyName;
                    // TODO: we can't extract indyCurrentErrorJson directly from indyError
                    // So we would need to extract it ourelf as done here
                    // https://github.com/hyperledger/indy-sdk/blob/bafa3bbcca2f7ef4cf5ae2aca01b1dbf7286b924/wrappers/java/src/main/java/org/hyperledger/indy/sdk/IndyException.java#L71-L83
                    indyMessage = indyException.getSdkMessage();
                    indyBacktrace = indyException.getSdkBacktrace();
                } else {
                    Log.e(TAG, "Unhandled non IndyException", e);
                }
            } else {
                Log.e(TAG, "Unhandled non ExecutionException", e);
            }
        }

        public String getCode() {
            return String.valueOf(indyCode);
        }

        public String getMessage() {
            return message;
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
}
