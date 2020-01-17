package com.blockdevs.blockdevs


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tangem.CardManager
import com.tangem.common.extensions.toHexString
import com.tangem.tangem_sdk_new.DefaultCardManagerDelegate
import com.tangem.tangem_sdk_new.NfcLifecycleObserver
import com.tangem.tangem_sdk_new.nfc.NfcManager
import com.tangem.tasks.ScanEvent
import com.tangem.tasks.TaskError
import com.tangem.tasks.TaskEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.kethereum.keccakshortcut.keccak


class MainActivity : AppCompatActivity() {

    // Initialize SDK Functions to interact with the Tangem Cards
    private val nfcManager = NfcManager()
    private val cardManagerDelegate : DefaultCardManagerDelegate =  DefaultCardManagerDelegate(nfcManager.reader)
    private val cardManager = CardManager(nfcManager.reader, cardManagerDelegate)



    // Lazy initialization
    private lateinit var cardId: String


    // Storing data that needs safe calls or null checks
    // Protection agains NullPointerException for those who knows java
    private var wallet: String? = String()
    private var wallet_public_key : ByteArray? = null



    // Descriptions
    private val cardIsCancelled = "User cancelled!"



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

                                // 1. Convert the wallet public key to eth wallet address
                                /**
                                 * Based from the Tangem Android app -- made a kotlin implementation using kethereum
                                 * and tangem core sdk
                                 */

                                wallet_public_key = (taskEvent.data as ScanEvent.OnReadEvent).card.walletPublicKey!!
                                // declare length of public key byte array
                                var lenPk : Int =  wallet_public_key !!.size
                                if (lenPk < 2) {
                                    throw IllegalArgumentException("Uncompress public key length is invalid");
                                }

                                // Trim key
                                val cleanKey = ByteArray(lenPk - 1)

                                for (i in 0 until cleanKey.size) {
                                    cleanKey[i] =  wallet_public_key !!.get(i + 1)
                                }

                                //  Hash it
                                val r: ByteArray = cleanKey.keccak()

                                // Declare address as byte array
                                val address = ByteArray(20)

                                for (i in 0..19) {
                                    address[i] = r[i + 12]
                                }

                                wallet = String.format("0x%s", address.toHexString());


                            }

                            is ScanEvent.OnVerifyEvent -> {
                                // Handle card verification and display data
                                runOnUiThread {
                                    // display text
                                    status?.text = "Hi, "
                                    txt_wallet?.text = wallet
                                    btn_sign.isEnabled = true
                                    txt_wallet2?.text = "Loading balance.."
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
                        // Call on async task to connect to the ETH
                        EthGetBalance(wallet, this).execute()
                    }
                }


                // CALL CONTRACT
                btn_sign?.setOnClickListener { _ ->
                    EthReadContract(this).execute()


                }
            }

        }
    }

    // Creates sample hashed transactions to sign
        private fun createSampleHashes(): Array<ByteArray> {
            val hash1 = ByteArray(32) { 1 }
            val hash2 = ByteArray(32) { 2 }
            return arrayOf(hash1, hash2)
        }

}





