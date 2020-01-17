package com.blockdevs.blockdevs

import android.os.AsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import org.kethereum.rpc.HttpEthereumRPC
import org.komputing.khex.extensions.clean0xPrefix
import org.komputing.khex.extensions.hexToByteArray


class EthReadContract(private var activity: MainActivity?) : AsyncTask<Void, Void, String>() {
    // Initialize wallet balance
    private lateinit var walletbalance: String

    // Infura API
    // Note: Will delete this eventually. Make your own infura.io account if you want.

    // Main net
    //private val web3 = HttpEthereumRPC("https://mainnet.infura.io/v3/a34cf048dac0496da080d1195ec2895c")
    // Ropsten
    //private val web3 = HttpEthereumRPC("https://ropsten.infura.io/v3/a34cf048dac0496da080d1195ec2895c")
    // Rinkeby
    private val rpc = HttpEthereumRPC("https://rinkeby.infura.io/v3/a34cf048dac0496da080d1195ec2895c")



    // CONTRACT 0x692a70D2e424a56D2C6C27aA97D1a86395877b3A
    // CONTRACT OWNER 0xCA35b7d915458EF540aDe6068dFe2F44E8fa733c

    override fun doInBackground(vararg params: Void?): String? {


        // TODO:  CONVERT TO ASCII!
        // RETURN: Hello World?
        walletbalance = String(rpc.getStorageAt("0x692a70D2e424a56D2C6C27aA97D1a86395877b3A", "0", "latest")!!.clean0xPrefix().hexToByteArray())

        return walletbalance
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // Run balance on thread
        activity?.txt_wallet2?.text = result
    }




}
