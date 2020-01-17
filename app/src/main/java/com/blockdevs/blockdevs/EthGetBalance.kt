package com.blockdevs.blockdevs

import android.os.AsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import org.kethereum.model.Address
import org.kethereum.rpc.HttpEthereumRPC
import java.math.BigInteger


class EthGetBalance(val walletaddress: String?, private var activity: MainActivity?) : AsyncTask<Void, Void, String>() {
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


    // Ether value
    private val divider : BigInteger = 1000000000000000000.toBigInteger()


    override fun doInBackground(vararg params: Void?): String? {
        // Convert to Ether
        walletbalance = rpc.getBalance(Address(walletaddress!!))!!.toBigDecimal().divide(divider.toBigDecimal()).toString()

        // return wallet balance
        return walletbalance
    }


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // Run balance on thread
        activity?.txt_wallet2?.text = result
    }




}
