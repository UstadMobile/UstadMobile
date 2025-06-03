package com.ustadmobile.core.domain.credentials.passkey

/**
 * The passkey userHandle (e.g. PublicKeyCredentialUserEntityJSON.id) will be Base64 encoded byte
 * array containing
 *  a) The PersonPasskey.personPasskeyUid (64bit long UID) - the UID of the passkey as stored in the
 *     database.
 *  b) The Learning Space URL (encoded using string.toByteArray())
 *
 * As per https://w3c.github.io/webauthn/#dictionary-user-credential-params the userHandle MUST
 * NOT contain any personally identifiable information (like usernames, email, phone etc).
 *
 * This user handle allows the app to verify the passkey on the server because it includes a) the
 * learning space url and b) the uid of the passkey itself. The server can then authenticate a
 * credential manager response (including determining the related user).
 */
interface EncodeUserHandleUseCase {

    operator fun invoke(personPasskeyUid:Long): String

}