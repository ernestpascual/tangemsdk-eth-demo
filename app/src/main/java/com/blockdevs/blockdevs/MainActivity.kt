package com.blockdevs.blockdevs

import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tangem.CardManager
import com.tangem.tangem_sdk_new.DefaultCardManagerDelegate
import com.tangem.tangem_sdk_new.NfcLifecycleObserver
import com.tangem.tangem_sdk_new.nfc.NfcManager
import com.tangem.tasks.ScanEvent
import com.tangem.tasks.TaskError
import com.tangem.tasks.TaskEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
//import sun.jvm.hotspot.utilities.IntArray
//import sun.jvm.hotspot.utilities.IntArray




class MainActivity : AppCompatActivity() {

    // Initialize SDK Functions to interact with the Tangem Cards
    private val nfcManager = NfcManager()
    private val cardManagerDelegate : DefaultCardManagerDelegate =  DefaultCardManagerDelegate(nfcManager.reader)
    private val cardManager = CardManager(nfcManager.reader, cardManagerDelegate)


    // Lazy initialization
    private lateinit var cardId: String
    private lateinit var wallet_balance: String

    // Storing data that needs safe calls or null checks
    // Protection agains NullPointerException for those who knows java
    private var wallet: String? = String()
    private var wallet_public_key : ByteArray? = null


    // Descriptions
    private val cardIsCancelled = "User cancelled!"


    // TODO: Set up server location
    // TODO: Alternately, you can just do a GET request from the REST API Server
    var server = Server("https://horizon.stellar.org")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Interact with Android NFC
        nfcManager.setCurrentActivity(this)
        cardManagerDelegate.activity = this
        lifecycle.addObserver(NfcLifecycleObserver(nfcManager))

        // Trigger scan card method on button click
        read_card?.setOnClickListener { _ ->
            cardManager.scanCard { taskEvent ->
                when (taskEvent) {
                    is TaskEvent.Event -> {
                        when (taskEvent.data) {
                            is ScanEvent.OnReadEvent -> {

                                /**
                                 * cardID is used in various SDK Tasks and functions.
                                 * It returns a string that you can store on a variable (declared within the class)
                                 */
                                cardId = (taskEvent.data as ScanEvent.OnReadEvent).card.cardId


                                /**
                                 * In getting stellar wallet address:
                                 * 1.) Convert the wallet public key (in BytesArray) to KeyPair from the stellar SDK
                                 * 2.) Use the getAccountId() method from the stellar SDK to obtain the wallet address
                                 */

                                // 1.) Convert the wallet public key to KeyPair
                                var keypair: KeyPair =
                                    KeyPair.fromPublicKey((taskEvent.data as ScanEvent.OnReadEvent).card.walletPublicKey!!)

                                // 2.) Use getAccountId() to get the wallet address and store it if you like
                                wallet = keypair.getAccountId()

                                // Referencing the public key to use for signing
                                wallet_public_key = (taskEvent.data as ScanEvent.OnReadEvent).card.walletPublicKey!!

                            }

                            is ScanEvent.OnVerifyEvent -> {
                                // Handle card verification

                                // Display data
                                runOnUiThread {
                                    // display text
                                    status?.text = "Hi, " + cardId + "!"
                                    txt_wallet?.text = wallet
                                    btn_sign.isEnabled = true
                                }
                            }
                        }
                    }
                    is TaskEvent.Completion -> {
                        if (taskEvent.error != null) {
                            if (taskEvent.error is TaskError.UserCancelledError) {
                                // Handle case when user cancelled manually
                                runOnUiThread {
                                    status?.text = cardIsCancelled
                                }
                            }
                            // Handle other errors

                        }
                        // Handle completion

                        // TODO: Connect to the Stellar Network and check balance
                        /*
                        val balances = server.accounts().account(wallet).getBalances()
                        for (balance in balances) {
                            if (balance.assetType.equals("native", ignoreCase = true)) {
                                wallet_balance = balance.balance
                                runOnUiThread {
                                    // display text
                                    txt_wallet2?.text = wallet_balance
                                }
                            }
                        }
                        */

                    }
                }

                // Used wallet public key to sign
                btn_sign?.setOnClickListener { _ ->

                }
            }
            /**
             * In signing using cardManager:
             * It can take an array of BytesArray -- multisigniture
             */
            cardManager.sign(
                arrayOf(wallet_public_key!!),
                cardId
            ) {
                when (it) {
                    is TaskEvent.Completion -> {
                        if (it.error != null) runOnUiThread {
                            status?.text = it.error!!::class.simpleName
                        }
                    }
                    is TaskEvent.Event -> runOnUiThread {
                        status?.text = cardId + " used to sign sample hashes."
                    }
                }
            }
        }
    }

    // Creates sample hashes to demo signing to try
        private fun createSampleHashes(): Array<ByteArray> {
            val hash1 = ByteArray(32) { 1 }
            val hash2 = ByteArray(32) { 2 }
            return arrayOf(hash1, hash2)
        }
}

