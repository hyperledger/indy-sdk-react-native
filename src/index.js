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
 *
 */

import { NativeModules } from 'react-native'
import { Buffer } from 'buffer'

export type CredOffer = {
  schema_id: string,
  cred_def_id: string,
  // Fields below can depend on Cred Def type
  nonce: string,
  key_correctness_proof: any, // <key_correctness_proof>
}

export type CredDefId = string
export type CredDef = {
  id: string, // identifier of credential definition
  schemaId: string, // identifier of stored in ledger schema
  type: string, // type of the credential definition. CL is the only supported type now.
  tag: string, // allows to distinct between credential definitions for the same issuer and schema
  value: {
    // Dictionary with Credential Definition's data:
    primary: any, // primary credential public key,
    revocation: ?any, // revocation credential public key
  },
  ver: string, // Version of the Credential Definition json
}

export type CredId = string

// Credential json received from issuer
export type Cred = {
  schema_id: string,
  cred_def_id: string,
  rev_reg_def_id: ?string,
  values: {
    // "attr1" : {"raw": "value1", "encoded": "value1_as_int" },
    // "attr2" : {"raw": "value1", "encoded": "value1_as_int" }
    [string]: { raw: any, endcoded: any },
  }, // <see credValues above>,
  // Fields below can depend on Cred Def type
  signature: any, // <signature>
  signature_correctness_proof: any, // <signature_correctness_proof>
}

export type Credential = {
  referent: CredId, // cred_id in the wallet
  attrs?: {
    // "key1":"raw_value1",
    // "key2":"raw_value2"
    [string]: any,
  },
  schema_id: string,
  cred_def_id: string,
  rev_reg_id?: string, // Optional<string>,
  cred_rev_id?: string, // Optional<string>
}

export type MasterSecretId = string

export type RequestedAttribute = string

export type ProofRequest = {
  name: string,
  version: string,
  nonce: string,
  requested_attributes: {
    // set of requested attributes
    [attributeReferent: string]: {
      name: string,
    }, // <attr_referent>: <attr_info>
  },
  requested_predicates: {
    // set of requested predicates
    [attributePredicate: string]: {}, // <predicate_referent>: <predicate_info>
  },
  // Optional<<non_revoc_interval>>
  // If specified prover must proof non-revocation
  // for date in this interval for each attribute
  // (can be overridden on attribute level)
  non_revoked?: any,
}

export type Proof = {
  requested_proof: {
    revealed_attrs: {
      // requested_attr1_id: {sub_proof_index: number, raw: string, encoded: string},
      // requested_attr4_id: {sub_proof_index: number: string, encoded: string},
      [attributeReferent: string]: {
        raw: any,
      },
    },
    unrevealed_attrs: {
      // requested_attr3_id: {sub_proof_index: number}
      [attributeReferent: string]: {},
    },
    self_attested_attrs: {
      // requested_attr2_id: self_attested_value,
      [attributeReferent: string]: {},
    },
    requested_predicates: {
      // requested_predicate_1_referent: {sub_proof_index: int},
      // requested_predicate_2_referent: {sub_proof_index: int},
      [attributePredicate: string]: {},
    },
  },
  proof: {
    proofs: [], // [ <credential_proof>, <credential_proof>, <credential_proof> ],
    aggregated_proof: any, // <aggregated_proof>
  },
  identifiers: [], // [{schema_id, cred_def_id, Optional<rev_reg_id>, Optional<timestamp>}]
}

export type RequestedCredentials = {
  self_attested_attributes: {
    // "self_attested_attribute_referent": string
    [attributeReferent: string]: string,
  },
  requested_attributes: {
    // "requested_attribute_referent_1": {"cred_id": string, "timestamp": Optional<number>, revealed: <bool> }},
    // "requested_attribute_referent_2": {"cred_id": string, "timestamp": Optional<number>, revealed: <bool> }}
    [attributeReferent: string]: {},
  },
  requested_predicates: {
    // "requested_predicates_referent_1": {"cred_id": string, "timestamp": Optional<number> }},
    [string]: {},
  },
}

export type SchemaId = string
export type Schema = {
  id: SchemaId,
  name: string,
  // TODO It would be better to remove `status` attribute, because is part of our business domain and not Indy.
  // This custom attribute shows state of the credential request (when waiting for documents approval).
  status?: string,
}

/**
 * Json - all schemas json participating in the proof request
 *  {
 *    <schema1_id>: <schema1_json>,
 *    <schema2_id>: <schema2_json>,
 *    <schema3_id>: <schema3_json>,
 *  }
 */
export type Schemas = {
  [SchemaId]: Schema,
}

/**
 * Json - all credential definitions json participating in the proof request
 *  {
 *    "cred_def1_id": <credential_def1_json>,
 *    "cred_def2_id": <credential_def2_json>,
 *    "cred_def3_id": <credential_def3_json>,
 *  }
 */
export type CredentialDefs = {
  [CredDefId]: CredDef,
}

/**
 * Json - all revocation states json participating in the proof request
 *  {
 *    "rev_reg_def1_id": {
 *        "timestamp1": <rev_state1>,
 *        "timestamp2": <rev_state2>,
 *    },
 *    "rev_reg_def2_id": {
 *        "timestamp3": <rev_state3>
 *    },
 *    "rev_reg_def3_id": {
 *        "timestamp4": <rev_state4>
 *    },
 *  }
 */
export type RevStates = {
  [key: string]: {
    [key: string]: unknown,
  },
}

/**
 * Json - Request data json
 */
export type LedgerRequest = {}

export type LedgerRequestResult = {}

export type CredReq = {
  prover_did: string,
  cred_def_id: string,
  // Fields below can depend on Cred Def type
  blinded_ms?: any, // <blinded_master_secret>,
  blinded_ms_correctness_proof?: any, // <blinded_ms_correctness_proof>,
  nonce?: string,
}

export type CredValues = Record<string, CredValue>

interface CredValue {
  raw: string;
  encoded: string; // Raw value as number in string
}

/**
 * Credential request metadata json for further processing of received form Issuer credential.
 */
export type CredReqMetadata = {}

export type RevRegId = string
export type CredRevocId = string
export type RevocRegDef = {
  id: RevRegId,
  revocDefType: 'CL_ACCUM',
  tag: string,
  credDefId: CredDefId,
  value: {
    issuanceType: 'ISSUANCE_BY_DEFAULT' | 'ISSUANCE_ON_DEMAND',
    maxCredNum: number,
    tailsHash: string,
    tailsLocation: string,
    publicKeys: string[],
  },
  ver: string,
}
export type RevocRegDefs = {
  [revRegId: string]: RevocRegDef,
}
export type RevocRegDelta = Record<string, unknown>
export type TailsWriterConfig = {
  base_dir: string,
  uri_pattern: string,
}

export type Did = string

export type Verkey = string

export type WalletHandle = number
export type PoolHandle = number
export type BlobReaderHandle = number

export type WalletRecord = {
  id: string,
  type: string,
  value: string,
  tags: {},
}

export type WalletSearchHandle = number

export type WalletRecrods = {
  totalCount?: string,
  records?: WalletRecord[],
}

const nymRoleValues = {
  TRUSTEE: 0,
  STEWARD: 2,
  TRUST_ANCHOR: 101,
  ENDORSER: 101,
  NETWORK_MONITOR: 201,
}

export type NymRole = $Keys<typeof nymRoleValues>

export type GetNymResponse = {
  did: Did,
  verkey: Verkey,
  role: NymRole,
}

const { IndySdk } = NativeModules

const indy = {
  // wallet

  createWallet(config: Object, credentials: Object): Promise<void> {
    return IndySdk.createWallet(JSON.stringify(config), JSON.stringify(credentials))
  },

  openWallet(config: Object, credentials: Object): Promise<WalletHandle> {
    return IndySdk.openWallet(JSON.stringify(config), JSON.stringify(credentials))
  },

  closeWallet(wh: WalletHandle): Promise<void> {
    return IndySdk.closeWallet(wh)
  },

  deleteWallet(config: Object, credentials: Object): Promise<void> {
    return IndySdk.deleteWallet(JSON.stringify(config), JSON.stringify(credentials))
  },

  exportWallet(wh: WalletHandle, exportConfig: Object): Promise<void> {
    return IndySdk.exportWallet(wh, JSON.stringify(exportConfig))
  },

  importWallet(config: Object, credentials: Object, importConfig: Object): Promise<void> {
    return IndySdk.importWallet(JSON.stringify(config), JSON.stringify(credentials), JSON.stringify(importConfig))
  },

  // did

  /**
   * Value of passed `wh` parameter will be ignored in Android version of Indy native bridge,
   * because it keeps wallet as a private attribute.
   */
  createAndStoreMyDid(wh: WalletHandle, did: Object): Promise<[Did, Verkey]> {
    if (Platform.OS === 'ios') {
      return IndySdk.createAndStoreMyDid(JSON.stringify(did), wh)
    }
    return IndySdk.createAndStoreMyDid(wh, JSON.stringify(did))
  },

  keyForDid(poolHandle: PoolHandle, wh: WalletHandle, did: Did): Promise<Verkey> {
    if (Platform.OS === 'ios') {
      return IndySdk.keyForDid(did, poolHandle, wh)
    }
    return IndySdk.keyForDid(poolHandle, wh, did)
  },

  keyForLocalDid(wh: WalletHandle, did: Did): Promise<Verkey> {
    if (Platform.OS === 'ios') {
      return IndySdk.keyForLocalDid(did, wh)
    }
    return IndySdk.keyForLocalDid(wh, did)
  },

  storeTheirDid(wh: WalletHandle, identity: {}) {
    if (Platform.OS === 'ios') {
      return IndySdk.storeTheirDid(JSON.stringify(identity), wh)
    }
    throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
  },

  async listMyDidsWithMeta(wh) {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    return IndySdk.listMyDidsWithMeta(wh)
  },

  async setDidMetadata(wh, did, metadata) {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    return IndySdk.setDidMetadata(wh, did, metadata)
  },

  // pairwise

  createPairwise(wh: WalletHandle, theirDid: Did, myDid: Did, metadata: string = ''): Promise<void> {
    if (Platform.OS === 'ios') {
      return IndySdk.createPairwise(theirDid, myDid, metadata, wh)
    }
    return IndySdk.createPairwise(wh, theirDid, myDid, metadata)
  },

  async getPairwise(wh: WalletHandle, theirDid: Did): Promise<Object> {
    if (Platform.OS === 'ios') {
      return JSON.parse(await IndySdk.getPairwise(theirDid, wh))
    }
    return JSON.parse(await IndySdk.getPairwise(wh, theirDid))
  },

  // crypto

  async createKey(wh: WalletHandle, key: Object): Promise<string> {
    if (Platform.OS === 'ios') {
      return IndySdk.createKey(JSON.stringify(key), wh)
    }
    return IndySdk.createKey(wh, JSON.stringify(key))
  },

  async cryptoAnonCrypt(recipientVk: Verkey, messageRaw: Buffer): Promise<Buffer> {
    if (Platform.OS === 'ios') {
      return IndySdk.cryptoAnonCrypt(messageRaw, recipientVk)
    }

    return Buffer.from(await IndySdk.cryptoAnonCrypt(recipientVk, Array.from(messageRaw)))
  },

  async cryptoAnonDecrypt(wh: WalletHandle, recipientVk: Verkey, encryptedMsg: Buffer): Promise<Buffer> {
    if (Platform.OS === 'ios') {
      return IndySdk.cryptoAnonDecrypt(encryptedMsg, recipientVk, wh)
    }
    return Buffer.from(await IndySdk.cryptoAnonDecrypt(wh, recipientVk, Array.from(encryptedMsg)))
  },

  async cryptoAuthCrypt(wh: WalletHandle, senderVk: Verkey, recipientVk: Verkey, messageRaw: Buffer): Promise<Buffer> {
    if (Platform.OS === 'ios') {
      return IndySdk.cryptoAuthCrypt(messageRaw, senderVk, recipientVk, wh)
    }
    return Buffer.from(await IndySdk.cryptoAuthCrypt(wh, senderVk, recipientVk, Array.from(messageRaw)))
  },

  async cryptoAuthDecrypt(wh: WalletHandle, recipientVk: Verkey, encryptedMsgRaw: Buffer): Promise<[Verkey, Buffer]> {
    if (Platform.OS === 'ios') {
      return IndySdk.cryptoAuthDecrypt(encryptedMsgRaw, recipientVk, wh)
    }
    const [verkey, msg] = await IndySdk.cryptoAuthDecrypt(recipientVk, Array.from(encryptedMsgRaw))
    return [verkey, Buffer.from(msg)]
  },

  async cryptoSign(wh: WalletHandle, signerVk: string, message: Buffer): Promise<Buffer> {
    return Buffer.from(await IndySdk.cryptoSign(wh, signerVk, Array.from(message)))
  },

  async cryptoVerify(signerVk: string, message: Buffer, signature: Buffer): Promise<Boolean> {
    return IndySdk.cryptoVerify(signerVk, Array.from(message), Array.from(signature))
  },

  async packMessage(
    wh: WalletHandle,
    message: Buffer,
    receiverKeys: Verkey[],
    senderVk: string | null
  ): Promise<Buffer> {
    if (Platform.OS == 'ios') {
      return Buffer.from(await IndySdk.packMessage(wh, Array.from(message), JSON.stringify(receiverKeys), senderVk))
    }
    return Buffer.from(await IndySdk.packMessage(wh, Array.from(message), receiverKeys, senderVk))
  },

  async unpackMessage(wh: WalletHandle, jwe: Buffer): Promise<Buffer> {
    return Buffer.from(await IndySdk.unpackMessage(wh, Array.from(jwe)))
  },

  // pool

  createPoolLedgerConfig(poolName: string, poolConfig: {}): Promise<void> {
    return IndySdk.createPoolLedgerConfig(poolName, JSON.stringify(poolConfig))
  },

  openPoolLedger(poolName: string, poolConfig: {} | undefined): Promise<PoolHandle> {
    if (Platform.OS === 'ios') {
      if (poolConfig === undefined) {
        return IndySdk.openLedger(poolName, null)
      }
      return IndySdk.openLedger(poolName, JSON.stringify(poolConfig))
    }
    if (poolConfig === undefined) {
      return IndySdk.openPoolLedger(poolName, null)
    }
    return IndySdk.openPoolLedger(poolName, JSON.stringify(poolConfig))
  },

  setProtocolVersion(protocolVersion: number): Promise<void> {
    return IndySdk.setProtocolVersion(protocolVersion)
  },

  closePoolLedger(ph: PoolHandle): Promise<void> {
    return IndySdk.closePoolLedger(ph)
  },

  // ledger

  async submitRequest(poolHandle: PoolHandle, request: LedgerRequest): Promise<LedgerRequestResult> {
    if (Platform.OS === 'ios') {
      return JSON.parse(await IndySdk.submitRequest(JSON.stringify(request), poolHandle))
    }
    return JSON.parse(await IndySdk.submitRequest(poolHandle, JSON.stringify(request)))
  },

  async signRequest(wh: WalletHandle, submitterDid: Did, request: LedgerRequest): Promise<LedgerRequest> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    return JSON.parse(await IndySdk.signRequest(wh, submitterDid, JSON.stringify(request)))
  },

  async buildSchemaRequest(submitterDid: Did, data: string): Promise<LedgerRequest> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }

    return JSON.parse(await IndySdk.buildSchemaRequest(submitterDid, JSON.stringify(data)))
  },

  async buildGetSchemaRequest(submitterDid: Did, id: string): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetSchemaRequest(submitterDid, id))
  },

  async buildGetAttribRequest(
    submitterDid: Did | null,
    targetDid: Did,
    raw: string | null,
    hash: string | null,
    enc: string | null
  ): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetAttribRequest(submitterDid, targetDid, raw, hash, enc))
  },

  async buildGetNymRequest(submitterDid: Did | null, targetDid: Did): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetNymRequest(submitterDid, targetDid))
  },

  async parseGetNymResponse(response: LedgerRequestResult): Promise<GetNymResponse> {
    return JSON.parse(await IndySdk.parseGetNymResponse(JSON.stringify(response)))
  },

  async parseGetSchemaResponse(getSchemaResponse: LedgerRequestResult): Promise<[SchemaId, Schema]> {
    const [id, schema] = await IndySdk.parseGetSchemaResponse(JSON.stringify(getSchemaResponse))
    return [id, JSON.parse(schema)]
  },

  async buildCredDefRequest(submitterDid: Did, credDef: CredDef): Promise<LedgerRequest> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    return JSON.parse(await IndySdk.buildCredDefRequest(submitterDid, JSON.stringify(credDef)))
  },

  async buildGetCredDefRequest(submitterDid: Did, id: string): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetCredDefRequest(submitterDid, id))
  },

  async parseGetCredDefResponse(getCredDefResponse: LedgerRequestResult): Promise<[CredDefId, CredDef]> {
    const [credDefId, credDef] = await IndySdk.parseGetCredDefResponse(JSON.stringify(getCredDefResponse))
    return [credDefId, JSON.parse(credDef)]
  },

  async buildGetRevocRegDefRequest(submitterDid: Did | null, revocRegDefId: string): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetRevocRegDefRequest(submitterDid, revocRegDefId))
  },

  async parseGetRevocRegDefResponse(getRevocRegResponse: LedgerRequestResult): Promise<[RevRegId, RevocRegDef]> {
    const [revocRegDefId, revocRegDef] = await IndySdk.parseGetRevocRegDefResponse(JSON.stringify(getRevocRegResponse))
    return [revocRegDefId, JSON.parse(revocRegDef)]
  },

  async buildGetRevocRegDeltaRequest(
    submitterDid: Did | null,
    revocRegDefId: string,
    from: number = 0,
    to: number = new Date().getTime()
  ): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetRevocRegDeltaRequest(submitterDid, revocRegDefId, from, to))
  },

  async parseGetRevocRegDeltaResponse(
    getRevocRegDeltaResponse: LedgerRequestResult
  ): Promise<[RevRegId, RevocRegDelta, number]> {
    const [revocRegId, revocRegDelta, timestamp] = await IndySdk.parseGetRevocRegDeltaResponse(
      JSON.stringify(getRevocRegDeltaResponse)
    )
    return [revocRegId, JSON.parse(revocRegDelta), timestamp]
  },

  async buildGetRevocRegRequest(
    submitterDid: Did | null,
    revocRegDefId: string,
    timestamp: number
  ): Promise<LedgerRequest> {
    return JSON.parse(await IndySdk.buildGetRevocRegRequest(submitterDid, revocRegDefId, timestamp))
  },

  async parseGetRevocRegResponse(getRevocRegResponse: LedgerRequestResult): Promise<[RevRegId, RevocReg, number]> {
    const [revocRegId, revocReg, ledgerTimestamp] = await IndySdk.parseGetRevocRegResponse(
      JSON.stringify(getRevocRegResponse)
    )
    return [revocRegId, JSON.parse(revocReg), ledgerTimestamp]
  },

  async proverCreateMasterSecret(wh: WalletHandle, masterSecretId: ?MasterSecretId): Promise<MasterSecretId> {
    if (Platform.OS === 'ios') {
      return IndySdk.proverCreateMasterSecret(masterSecretId, wh)
    }
    return IndySdk.proverCreateMasterSecret(wh, masterSecretId)
  },

  async proverCreateCredentialReq(
    wh: WalletHandle,
    proverDid: Did,
    credOffer: CredOffer,
    credDef: CredDef,
    masterSecretId: MasterSecretId
  ): Promise<[CredReq, CredReqMetadata]> {
    if (Platform.OS === 'ios') {
      const [credReq, credReqMetadata] = await IndySdk.proverCreateCredentialReq(
        JSON.stringify(credOffer),
        JSON.stringify(credDef),
        proverDid,
        masterSecretId,
        wh
      )
      return [JSON.parse(credReq), JSON.parse(credReqMetadata)]
    }
    const [credReq, credReqMetadata] = await IndySdk.proverCreateCredentialReq(
      wh,
      proverDid,
      JSON.stringify(credOffer),
      JSON.stringify(credDef),
      masterSecretId
    )
    return [JSON.parse(credReq), JSON.parse(credReqMetadata)]
  },

  proverStoreCredential(
    wh: WalletHandle,
    credId: CredId | null,
    credReqMetadata: CredReqMetadata,
    cred: Cred,
    credDef: CredDef,
    revRegDef: RevocRegDef | null
  ): Promise<CredId> {
    if (Platform.OS === 'ios') {
      return IndySdk.proverStoreCredential(
        JSON.stringify(cred),
        credId,
        JSON.stringify(credReqMetadata),
        JSON.stringify(credDef),
        revRegDef && JSON.stringify(revRegDef),
        wh
      )
    }
    return IndySdk.proverStoreCredential(
      wh,
      credId,
      JSON.stringify(credReqMetadata),
      JSON.stringify(cred),
      JSON.stringify(credDef),
      revRegDef && JSON.stringify(revRegDef)
    )
  },

  async proverGetCredential(wh: WalletHandle, credId: CredId): Promise<Credential> {
    if (Platform.OS === 'ios') {
      return JSON.parse(await IndySdk.proverGetCredential(credId, wh))
    }
    return JSON.parse(await IndySdk.proverGetCredential(wh, credId))
  },

  async proverDeleteCredential(wh: WalletHandle, credId: CredId): Promise<void> {
    if (Platform.OS === 'ios') {
      return await IndySdk.proverDeleteCredential(credId, wh)
    }
    return await IndySdk.proverDeleteCredential(wh, credId)
  },

  // NOTE: This method is deprecated because immediately returns all fetched credentials. Use proverSearchCredentials() to fetch records by small batches.
  async proverGetCredentials(wh: WalletHandle, filter: {} = {}): Promise<Credential[]> {
    if (Platform.OS === 'ios') {
      return JSON.parse(await IndySdk.proverGetCredentials(JSON.stringify(filter), wh))
    }
    return JSON.parse(await IndySdk.proverGetCredentials(wh, JSON.stringify(filter)))
  },

  async proverSearchCredentials(wh: WalletHandle, filter: {} = {}): Promise<[WalletSearchHandle, number]> {
    return await IndySdk.proverSearchCredentials(wh, JSON.stringify(filter))
  },

  async proverFetchCredentials(sh: WalletSearchHandle, count: number): Promise<Credential[]> {
    return JSON.parse(await IndySdk.proverFetchCredentials(sh, count))
  },

  async proverCloseCredentialsSearch(sh: WalletSearchHandle) {
    await IndySdk.proverCloseCredentialsSearch(sh)
  },

  // TODO Add return flow type.
  // It needs little investigation, because is doesn't seem to be same format as Credential stored in wallet.
  // NOTE: This method is deprecated because immediately returns all fetched credentials. proverSearchCredentialsForProofReq to fetch records by small batches.
  async proverGetCredentialsForProofReq(wh: WalletHandle, proofRequest: ProofRequest) {
    if (Platform.OS == 'ios') {
      return JSON.parse(await IndySdk.proverGetCredentialsForProofReq(JSON.stringify(proofRequest), wh))
    }
    return JSON.parse(await IndySdk.proverGetCredentialsForProofReq(wh, JSON.stringify(proofRequest)))
  },

  async proverSearchCredentialsForProofReq(wh: WalletHandle, proofRequest: ProofRequest, extraQuery: {} | null) {
    //The NodeJS IndySDK wrapper differs from the Java and iOS wrappers in this call--it allows extraQuery to be a wql query object or null.
    return await IndySdk.proverSearchCredentialsForProofReq(
      wh,
      JSON.stringify(proofRequest),
      JSON.stringify(extraQuery ?? {})
    )
  },

  async proverFetchCredentialsForProofReq(sh: number, itemReferent: string, count: number) {
    return JSON.parse(await IndySdk.proverFetchCredentialsForProofReq(sh, itemReferent, count))
  },

  async proverCloseCredentialsSearchForProofReq(sh: number) {
    return await IndySdk.proverCloseCredentialsSearchForProofReq(sh)
  },

  async proverCreateProof(
    wh: WalletHandle,
    proofReq: ProofRequest,
    requestedCredentials: RequestedCredentials,
    masterSecretName: MasterSecretId,
    schemas: Schemas,
    credentialDefs: CredentialDefs,
    revStates: RevStates = {}
  ): Promise<Proof> {
    if (Platform.OS == 'ios') {
      return JSON.parse(
        await IndySdk.proverCreateProofForRequest(
          JSON.stringify(proofReq),
          JSON.stringify(requestedCredentials),
          masterSecretName,
          JSON.stringify(schemas),
          JSON.stringify(credentialDefs),
          JSON.stringify(revStates),
          wh
        )
      )
    }
    return JSON.parse(
      await IndySdk.proverCreateProof(
        wh,
        JSON.stringify(proofReq),
        JSON.stringify(requestedCredentials),
        masterSecretName,
        JSON.stringify(schemas),
        JSON.stringify(credentialDefs),
        JSON.stringify(revStates)
      )
    )
  },

  async verifierVerifyProof(
    proofRequest: IndyProofRequest,
    proof: Proof,
    schemas: Schemas,
    credentialDefs: CredentialDefs,
    revRegDefs: RevocRegDefs,
    revStates: RevStates
  ): Promise<boolean> {
    return IndySdk.verifierVerifyProof(
      JSON.stringify(proofRequest),
      JSON.stringify(proof),
      JSON.stringify(schemas),
      JSON.stringify(credentialDefs),
      JSON.stringify(revRegDefs),
      JSON.stringify(revStates)
    )
  },

  async generateNonce() {
    return IndySdk.generateNonce()
  },

  async generateWalletKey(config?: { seed?: string } = {}) {
    return IndySdk.generateWalletKey(JSON.stringify(config))
  },

  async appendTxnAuthorAgreementAcceptanceToRequest(
    request: LedgerRequest,
    text: string,
    version: string,
    taaDigest: string,
    mechanism: string,
    time: number
  ): Promise<LedgerRequest> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    return JSON.parse(
      await IndySdk.appendTxnAuthorAgreementAcceptanceToRequest(
        JSON.stringify(request),
        text,
        version,
        taaDigest,
        mechanism,
        time
      )
    )
  },

  async buildGetTxnAuthorAgreementRequest(submitterDid: Did, data: string): Promise<LedgerRequest> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    return JSON.parse(await IndySdk.buildGetTxnAuthorAgreementRequest(submitterDid, data))
  },

  // non_secrets

  async addWalletRecord(wh: WalletHandle, type: string, id: string, value: string, tags: {}): Promise<void> {
    return IndySdk.addWalletRecord(wh, type, id, value, JSON.stringify(tags))
  },

  async updateWalletRecordValue(wh: WalletHandle, type: string, id: string, value: string): Promise<void> {
    return IndySdk.updateWalletRecordValue(wh, type, id, value)
  },

  async updateWalletRecordTags(wh: WalletHandle, type: string, id: string, tags: {}): Promise<void> {
    return IndySdk.updateWalletRecordTags(wh, type, id, JSON.stringify(tags))
  },

  async addWalletRecordTags(wh: WalletHandle, type: string, id: string, tags: {}): Promise<void> {
    return IndySdk.addWalletRecordTags(wh, type, id, JSON.stringify(tags))
  },

  async deleteWalletRecordTags(wh: WalletHandle, type: string, id: string, tagNames: []): Promise<void> {
    return IndySdk.deleteWalletRecordTags(wh, type, id, JSON.stringify(tagNames))
  },

  async deleteWalletRecord(wh: WalletHandle, type: string, id: string): Promise<void> {
    return IndySdk.deleteWalletRecord(wh, type, id)
  },

  async getWalletRecord(wh: WalletHandle, type: string, id: string, options: {}): Promise<WalletRecord> {
    return JSON.parse(await IndySdk.getWalletRecord(wh, type, id, JSON.stringify(options)))
  },

  async openWalletSearch(wh: WalletHandle, type: string, query: {}, options: {}): Promise<number> {
    return IndySdk.openWalletSearch(wh, type, JSON.stringify(query), JSON.stringify(options))
  },

  async fetchWalletSearchNextRecords(wh: WalletHandle, sh: WalletSearchHandle, count: number): Promise<WalletRecords> {
    return JSON.parse(await IndySdk.fetchWalletSearchNextRecords(wh, sh, count))
  },

  async closeWalletSearch(sh: WalletSearchHandle): Promise<void> {
    return IndySdk.closeWalletSearch(sh)
  },

  // Anoncreds

  async issuerCreateSchema(did: Did, name: string, version: string, attributes: string[]): Promise<[SchemaId, Schema]> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }

    const [schemaId, schema] = await IndySdk.issuerCreateSchema(did, name, version, JSON.stringify(attributes))
    return [schemaId, JSON.parse(schema)]
  },

  async issuerCreateAndStoreCredentialDef(
    wh: WalletHandle,
    issuerDid: Did,
    schema: Schema,
    tag: string,
    signatureType: string,
    config: {}
  ): Promise<[CredDefId, CredDef]> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }

    const [credDefId, credDef] = await IndySdk.issuerCreateAndStoreCredentialDef(
      wh,
      issuerDid,
      JSON.stringify(schema),
      tag,
      signatureType,
      JSON.stringify(config)
    )
    return [credDefId, JSON.parse(credDef)]
  },

  async issuerCreateCredentialOffer(wh: WalletHandle, credDefId: CredDefId): Promise<CredOffer> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }

    return JSON.parse(await IndySdk.issuerCreateCredentialOffer(wh, credDefId))
  },

  async issuerCreateCredential(
    wh: WalletHandle,
    credOffer: CredOffer,
    credReq: CredReq,
    credvalues: CredValues,
    revRegId: RevRegId,
    blobStorageReaderHandle: BlobReaderHandle
  ): Promise<[Credential, CredRevocId, RevocRegDelta]> {
    if (Platform.OS === 'ios') {
      throw new Error(`Unsupported operation! Platform: ${Platform.OS}`)
    }
    const [credJson, revocId, revocRegDelta] = await IndySdk.issuerCreateCredential(
      wh,
      JSON.stringify(credOffer),
      JSON.stringify(credReq),
      JSON.stringify(credvalues),
      revRegId,
      blobStorageReaderHandle
    )
    return [JSON.parse(credJson), revocId, JSON.parse(revocRegDelta)]
  },

  async createRevocationState(
    blobStorageReaderHandle: BlobReaderHandle,
    revRegDef: RevocRegDef,
    revRegDelta: RevocRegDelta,
    timestamp: number,
    credRevId: string
  ): Promise<any> {
    return JSON.parse(
      await IndySdk.createRevocationState(
        blobStorageReaderHandle,
        JSON.stringify(revRegDef),
        JSON.stringify(revRegDelta),
        timestamp,
        credRevId
      )
    )
  },

  // blob_storage

  async openBlobStorageReader(type: string, tailsWriterConfig: TailsWriterConfig): Promise<BlobReaderHandle> {
    return JSON.parse(await IndySdk.openBlobStorageReader(type, JSON.stringify(tailsWriterConfig)))
  },
}

const indyErrors = {
  100: 'CommonInvalidParam1',
  101: 'CommonInvalidParam2',
  102: 'CommonInvalidParam3',
  103: 'CommonInvalidParam4',
  104: 'CommonInvalidParam5',
  105: 'CommonInvalidParam6',
  106: 'CommonInvalidParam7',
  107: 'CommonInvalidParam8',
  108: 'CommonInvalidParam9',
  109: 'CommonInvalidParam10',
  110: 'CommonInvalidParam11',
  111: 'CommonInvalidParam12',
  112: 'CommonInvalidState',
  113: 'CommonInvalidStructure',
  114: 'CommonIOError',
  115: 'CommonInvalidParam13',
  116: 'CommonInvalidParam14',
  200: 'WalletInvalidHandle',
  201: 'WalletUnknownTypeError',
  202: 'WalletTypeAlreadyRegisteredError',
  203: 'WalletAlreadyExistsError',
  204: 'WalletNotFoundError',
  205: 'WalletIncompatiblePoolError',
  206: 'WalletAlreadyOpenedError',
  207: 'WalletAccessFailed',
  208: 'WalletInputError',
  209: 'WalletDecodingError',
  210: 'WalletStorageError',
  211: 'WalletEncryptionError',
  212: 'WalletItemNotFound',
  213: 'WalletItemAlreadyExists',
  214: 'WalletQueryError',
  300: 'PoolLedgerNotCreatedError',
  301: 'PoolLedgerInvalidPoolHandle',
  302: 'PoolLedgerTerminated',
  303: 'LedgerNoConsensusError',
  304: 'LedgerInvalidTransaction',
  305: 'LedgerSecurityError',
  306: 'PoolLedgerConfigAlreadyExistsError',
  307: 'PoolLedgerTimeout',
  308: 'PoolIncompatibleProtocolVersion',
  309: 'LedgerNotFound',
  400: 'AnoncredsRevocationRegistryFullError',
  401: 'AnoncredsInvalidUserRevocId',
  404: 'AnoncredsMasterSecretDuplicateNameError',
  405: 'AnoncredsProofRejected',
  406: 'AnoncredsCredentialRevoked',
  407: 'AnoncredsCredDefAlreadyExistsError',
  500: 'UnknownCryptoTypeError',
  600: 'DidAlreadyExistsError',
  700: 'PaymentUnknownMethodError',
  701: 'PaymentIncompatibleMethodsError',
  702: 'PaymentInsufficientFundsError',
  703: 'PaymentSourceDoesNotExistError',
  704: 'PaymentOperationNotSupportedError',
  705: 'PaymentExtraFundsError',
  706: 'TransactionNotAllowedError',
}

function wrapIndyCallWithErrorHandling(func) {
  return async (...args) => {
    // Wrap try/catch around indy func
    try {
      return await func(...args)
    } catch (e) {
      // Try to parse e.message as it should be a
      // JSON string for Indy errors (at least on Android)
      // Parsing could also go wrong. In this case we just
      // want to throw the native side error
      let parse
      try {
        parse = JSON.parse(e.message)
      } catch {
        throw e
      }

      if (Platform.OS === 'ios') {
        const { indyCode, message } = parse
        if (!isNaN(indyCode) && indyErrors.hasOwnProperty(indyCode)) {
          const indyName = indyErrors[indyCode]
          const indyErrorFromIOS = {
            name: 'IndyError',
            message,
            indyCode: indyCode,
            indyName: indyName,
            indyCurrentErrorJson: null,
          }
          throw indyErrorFromIOS
        }
      }

      throw parse
    }
  }
}

// This adds indy error handling to all methods to
// transform the string messages into JSON error objects
const indyWithErrorHandling = Object.fromEntries(
  Object.entries(indy).map(([funcName, funcImpl]) => [funcName, wrapIndyCallWithErrorHandling(funcImpl)])
)

export default indyWithErrorHandling
